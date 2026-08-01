import assert from 'node:assert/strict';
import test from 'node:test';
import { createRealtimeConnectionManager, toWebSocketUrl } from './realtimeSocketCore.js';

const NOTIFICATIONS = '/user/queue/notifications';
const MESSAGING = '/user/queue/messaging';

class FakeClient {
  static instances = [];

  constructor(config) {
    this.config = config;
    this.active = false;
    this.connectHeaders = {};
    this.subscriptions = [];
    this.activateCount = 0;
    this.deactivateCount = 0;
    FakeClient.instances.push(this);
  }

  activate() {
    this.config.beforeConnect();
    this.active = true;
    this.activateCount += 1;
    this.config.onConnect();
  }

  async deactivate() {
    this.active = false;
    this.deactivateCount += 1;
    this.config.onWebSocketClose();
  }

  subscribe(destination, callback) {
    const subscription = {
      destination,
      callback,
      unsubscribed: false,
      unsubscribe() {
        this.unsubscribed = true;
      },
    };
    this.subscriptions.push(subscription);
    return subscription;
  }
}

function createFixture(getAccessToken = () => 'token-1') {
  FakeClient.instances = [];
  return createRealtimeConnectionManager({
    ClientClass: FakeClient,
    reconnectTimeMode: 'EXPONENTIAL',
    brokerURL: 'ws://localhost:8080/ws',
    getAccessToken,
  });
}

function latestSubscription(client, destination) {
  return client.subscriptions.findLast((subscription) => subscription.destination === destination);
}

test('chuyển API base URL thành native ws/wss endpoint mà không hard-code domain', () => {
  assert.equal(toWebSocketUrl('http://localhost:8080'), 'ws://localhost:8080/ws');
  assert.equal(toWebSocketUrl('https://api.example.com/api'), 'wss://api.example.com/api/ws');
});

test('một tab chỉ tạo một client và subscribe đồng thời hai destination', async () => {
  const manager = createFixture();
  manager.subscribe(NOTIFICATIONS, () => {});
  manager.subscribe(MESSAGING, () => {});
  manager.activate();
  manager.activate();
  await manager.whenIdle();

  const client = FakeClient.instances[0];
  assert.equal(FakeClient.instances.length, 1);
  assert.equal(client.activateCount, 1);
  assert.deepEqual(client.subscriptions.map(({ destination }) => destination), [
    NOTIFICATIONS,
    MESSAGING,
  ]);
});

test('nhiều callback cùng destination được cô lập khi một callback lỗi', async () => {
  const manager = createFixture();
  const received = [];
  manager.subscribe(NOTIFICATIONS, () => { throw new Error('consumer failure'); });
  manager.subscribe(NOTIFICATIONS, (payload) => received.push(payload));
  manager.activate();
  await manager.whenIdle();

  const client = FakeClient.instances[0];
  assert.equal(client.subscriptions.length, 1);
  latestSubscription(client, NOTIFICATIONS).callback({ body: '{"eventId":"one"}' });
  assert.deepEqual(received, [{ eventId: 'one' }]);

  latestSubscription(client, NOTIFICATIONS).callback({ body: '{invalid-json' });
  assert.equal(received.length, 1);
});

test('unsubscribe một callback không làm mất callback khác cùng destination', async () => {
  const manager = createFixture();
  const first = [];
  const second = [];
  const firstToken = manager.subscribe(NOTIFICATIONS, (payload) => first.push(payload));
  manager.subscribe(NOTIFICATIONS, (payload) => second.push(payload));
  manager.activate();
  await manager.whenIdle();

  assert.equal(manager.unsubscribe(firstToken), true);
  latestSubscription(FakeClient.instances[0], NOTIFICATIONS).callback({ body: '{"id":2}' });
  assert.deepEqual(first, []);
  assert.deepEqual(second, [{ id: 2 }]);
});

test('unsubscribe Messaging không ảnh hưởng Notification và không đóng connection', async () => {
  const manager = createFixture();
  const notificationEvents = [];
  manager.subscribe(NOTIFICATIONS, (payload) => notificationEvents.push(payload));
  const messagingToken = manager.subscribe(MESSAGING, () => {});
  manager.activate();
  await manager.whenIdle();

  const client = FakeClient.instances[0];
  const messagingSubscription = latestSubscription(client, MESSAGING);
  manager.unsubscribe(messagingToken);

  assert.equal(messagingSubscription.unsubscribed, true);
  assert.equal(manager.isActive(), true);
  assert.equal(client.deactivateCount, 0);
  latestSubscription(client, NOTIFICATIONS).callback({ body: '{"eventId":"notification"}' });
  assert.deepEqual(notificationEvents, [{ eventId: 'notification' }]);
});

test('unsubscribe Notification không đóng connection dùng chung', async () => {
  const manager = createFixture();
  const token = manager.subscribe(NOTIFICATIONS, () => {});
  manager.activate();
  await manager.whenIdle();

  manager.unsubscribe(token);

  assert.equal(manager.isActive(), true);
  assert.equal(FakeClient.instances[0].deactivateCount, 0);
});

test('reconnect dùng token mới nhất và subscribe lại toàn bộ registry', async () => {
  let accessToken = 'token-1';
  const manager = createFixture(() => accessToken);
  manager.subscribe(NOTIFICATIONS, () => {});
  manager.subscribe(MESSAGING, () => {});
  manager.activate();
  await manager.whenIdle();
  const client = FakeClient.instances[0];
  assert.equal(client.connectHeaders.Authorization, 'Bearer token-1');

  accessToken = 'token-2';
  await manager.reconnect();

  assert.equal(client.connectHeaders.Authorization, 'Bearer token-2');
  assert.equal(client.activateCount, 2);
  assert.deepEqual(client.subscriptions.slice(-2).map(({ destination }) => destination), [
    NOTIFICATIONS,
    MESSAGING,
  ]);
  assert.equal(FakeClient.instances.length, 1);
});

test('activate sau cleanup kiểu React StrictMode không tạo hoặc đóng nhầm connection', async () => {
  const manager = createFixture();
  manager.subscribe(NOTIFICATIONS, () => {});
  manager.activate();
  const cleanup = manager.deactivate();
  manager.activate();
  await cleanup;
  await manager.whenIdle();

  assert.equal(FakeClient.instances.length, 1);
  assert.equal(manager.isActive(), true);
  assert.equal(FakeClient.instances[0].activateCount, 1);
  assert.equal(FakeClient.instances[0].deactivateCount, 0);
});

test('logout hoặc session clear deactivate connection nhưng giữ registry cho lần đăng nhập sau', async () => {
  const manager = createFixture();
  manager.subscribe(NOTIFICATIONS, () => {});
  manager.activate();
  await manager.whenIdle();

  await manager.deactivate();
  assert.equal(manager.isActive(), false);
  assert.equal(manager.isConnected(), false);

  manager.activate();
  await manager.whenIdle();
  assert.equal(manager.isActive(), true);
  assert.equal(FakeClient.instances[0].subscriptions.length, 2);
});
