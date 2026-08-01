import { Client, ReconnectionTimeMode } from '@stomp/stompjs';
import { apiConfig } from '../api/apiConfig.js';
import { tokenManager } from '../api/tokenManager.js';
import { createRealtimeConnectionManager, toWebSocketUrl } from './realtimeSocketCore.js';

// Module singleton bảo đảm mỗi tab chỉ tạo đúng một STOMP client dùng chung.
export const realtimeSocket = createRealtimeConnectionManager({
  ClientClass: Client,
  reconnectTimeMode: ReconnectionTimeMode.EXPONENTIAL,
  brokerURL: toWebSocketUrl(apiConfig.baseURL),
  getAccessToken: () => tokenManager.getAccessToken(),
});
