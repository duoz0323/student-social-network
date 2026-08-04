export function toWebSocketUrl(apiBaseUrl) {
  const url = new URL(apiBaseUrl);
  url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:';
  url.pathname = `${url.pathname.replace(/\/$/, '')}/ws`;
  url.search = '';
  url.hash = '';
  return url.toString();
}

/**
 * Quản lý duy nhất một STOMP client và phân phối payload cho nhiều consumer theo destination.
 */
export function createRealtimeConnectionManager({
  ClientClass,
  reconnectTimeMode,
  brokerURL,
  getAccessToken,
}) {
  let client = null;
  let desiredActive = false;
  let connected = false;
  let lifecycle = Promise.resolve();
  let nextSubscriptionId = 1;
  let lifecycleHandlers = {};
  const callbacksByDestination = new Map();
  const brokerSubscriptions = new Map();
  const connectionListeners = new Set();
  const allowedSendDestination = '/app/messaging/typing';

  function notifyConnectionState(nextConnected) {
    connected = nextConnected;
    [...connectionListeners].forEach((listener) => {
      try {
        listener(nextConnected);
      } catch {
        // Một consumer lỗi không được làm gián đoạn lifecycle của connection dùng chung.
      }
    });
  }

  function dispatch(destination, message) {
    let payload;
    try {
      payload = JSON.parse(message.body);
    } catch {
      // Payload sai contract bị bỏ qua; từng module tự reconciliation bằng REST.
      return;
    }

    const callbacks = [...(callbacksByDestination.get(destination)?.values() ?? [])];
    callbacks.forEach((callback) => {
      try {
        callback(payload);
      } catch {
        // Callback lỗi được cô lập để các callback khác trên cùng destination vẫn nhận event.
      }
    });
  }

  function subscribeBroker(destination) {
    if (!connected || brokerSubscriptions.has(destination)) return;
    if (!callbacksByDestination.get(destination)?.size) return;
    brokerSubscriptions.set(
      destination,
      client.subscribe(destination, (message) => dispatch(destination, message)),
    );
  }

  function clearBrokerSubscriptions(unsubscribe = false) {
    if (unsubscribe) {
      brokerSubscriptions.forEach((subscription) => subscription.unsubscribe());
    }
    brokerSubscriptions.clear();
  }

  function buildClient() {
    return new ClientClass({
      brokerURL,
      reconnectDelay: 1_000,
      reconnectTimeMode,
      maxReconnectDelay: 30_000,
      connectionTimeout: 10_000,
      heartbeatIncoming: 10_000,
      heartbeatOutgoing: 10_000,
      debug: () => {
        // Không log STOMP frame vì CONNECT frame chứa Access Token.
      },
      beforeConnect: () => {
        const accessToken = getAccessToken();
        if (!accessToken) throw new Error('Không có Access Token cho STOMP CONNECT.');
        client.connectHeaders = { Authorization: `Bearer ${accessToken}` };
      },
      onConnect: () => {
        clearBrokerSubscriptions();
        notifyConnectionState(true);
        callbacksByDestination.forEach((_callbacks, destination) => subscribeBroker(destination));
      },
      onWebSocketClose: () => {
        clearBrokerSubscriptions();
        notifyConnectionState(false);
      },
      onStompError: () => {
        lifecycleHandlers.onAuthenticationError?.();
      },
    });
  }

  function removeSubscription(tokenOrDestination, callback) {
    const destination = typeof tokenOrDestination === 'string'
      ? tokenOrDestination
      : tokenOrDestination?.destination;
    const callbacks = callbacksByDestination.get(destination);
    if (!callbacks) return false;

    let removed = false;
    if (typeof tokenOrDestination === 'object' && tokenOrDestination?.id != null) {
      removed = callbacks.delete(tokenOrDestination.id);
    } else if (typeof callback === 'function') {
      [...callbacks.entries()].forEach(([id, registeredCallback]) => {
        if (registeredCallback === callback) {
          callbacks.delete(id);
          removed = true;
        }
      });
    }

    if (callbacks.size === 0) {
      callbacksByDestination.delete(destination);
      brokerSubscriptions.get(destination)?.unsubscribe();
      brokerSubscriptions.delete(destination);
    }
    return removed;
  }

  return Object.freeze({
    activate() {
      desiredActive = true;
      if (!client) client = buildClient();
      lifecycle = lifecycle.then(() => {
        if (desiredActive && !client.active) client.activate();
      });
      return client;
    },

    async reconnect() {
      desiredActive = true;
      if (!client) client = buildClient();
      lifecycle = lifecycle.then(async () => {
        if (client.active) await client.deactivate();
        clearBrokerSubscriptions();
        if (desiredActive) client.activate();
      });
      await lifecycle;
    },

    async deactivate() {
      desiredActive = false;
      lifecycle = lifecycle.then(async () => {
        if (desiredActive) return;
        clearBrokerSubscriptions(true);
        if (client?.active) await client.deactivate();
        else notifyConnectionState(false);
      });
      await lifecycle;
    },

    subscribe(destination, callback) {
      if (typeof destination !== 'string' || !destination.trim()) {
        throw new TypeError('STOMP destination không hợp lệ.');
      }
      if (typeof callback !== 'function') {
        throw new TypeError('STOMP subscription callback phải là function.');
      }
      const normalizedDestination = destination.trim();
      const callbacks = callbacksByDestination.get(normalizedDestination) ?? new Map();
      const token = Object.freeze({
        destination: normalizedDestination,
        id: nextSubscriptionId,
      });
      nextSubscriptionId += 1;
      callbacks.set(token.id, callback);
      callbacksByDestination.set(normalizedDestination, callbacks);
      subscribeBroker(normalizedDestination);
      return token;
    },

    unsubscribe(tokenOrDestination, callback) {
      return removeSubscription(tokenOrDestination, callback);
    },

    send(destination, payload) {
      if (destination !== allowedSendDestination) {
        throw new TypeError('STOMP SEND destination không được phép.');
      }
      if (!connected || !client?.active) return false;
      try {
        client.publish({ destination, body: JSON.stringify(payload) });
        return true;
      } catch {
        // Typing là best-effort; lỗi serialize/publish không được ảnh hưởng composer.
        return false;
      }
    },

    addConnectionListener(listener) {
      if (typeof listener !== 'function') throw new TypeError('Connection listener phải là function.');
      connectionListeners.add(listener);
      listener(connected);
      return () => connectionListeners.delete(listener);
    },

    setLifecycleHandlers(nextHandlers = {}) {
      lifecycleHandlers = nextHandlers;
    },

    isActive() {
      return Boolean(client?.active);
    },

    isConnected() {
      return connected;
    },

    async whenIdle() {
      await lifecycle;
    },
  });
}
