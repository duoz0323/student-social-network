import { httpClient } from '../../../api/httpClient.js';
import { compactParams, requestData } from '../../../api/requestData.js';

const base = '/api/v1/conversations';

/** REST là nguồn sự thật; service không nhận senderId từ UI. */
export const messagingApi = Object.freeze({
  getConversations: (params, signal) => requestData(httpClient.get(base, { params: compactParams(params), signal })),
  getUnreadCount: (signal) => requestData(httpClient.get(`${base}/unread-count`, { signal })),
  openDirectConversation: (recipientUserId, signal) => requestData(httpClient.put(`${base}/direct/${recipientUserId}`, undefined, { signal })),
  getMessages: (conversationId, params, signal) => requestData(httpClient.get(`${base}/${conversationId}/messages`, { params: compactParams(params), signal })),
  sendMessage: (conversationId, payload, signal) => requestData(httpClient.post(`${base}/${conversationId}/messages`, payload, { signal })),
  sendImageMessage: (conversationId, formData, signal) => requestData(httpClient.post(`${base}/${conversationId}/messages`, formData, { signal })),
  getAttachmentAccess: (attachmentId, signal) => requestData(httpClient.get(`/api/v1/message-attachments/${attachmentId}/access`, { signal })),
  markRead: (conversationId, lastReadMessageId, signal) => requestData(httpClient.put(`${base}/${conversationId}/read`, { lastReadMessageId }, { signal })),
});
