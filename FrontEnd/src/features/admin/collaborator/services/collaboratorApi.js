import { COLLABORATOR_ENDPOINTS } from '../../../../api/apiEndpoints.js';
import { httpClient } from '../../../../api/httpClient.js';
import { compactParams, requestData } from '../../../../api/requestData.js';
import { createPostForm, updatePostForm } from '../../../post/locations/locationMultipart.js';

/** Service duy nhất của UI Collaborator; component không thao tác Axios trực tiếp. */
export const collaboratorApi = Object.freeze({
  getIdentity: (signal) => requestData(httpClient.get(COLLABORATOR_ENDPOINTS.identity, { signal })),
  getDashboard: (days, signal) => requestData(httpClient.get(COLLABORATOR_ENDPOINTS.dashboard, { params: { days }, signal })),
  getPosts: (params, signal) => requestData(httpClient.get(COLLABORATOR_ENDPOINTS.posts, { params: compactParams(params), signal })),
  getPost: (id, signal) => requestData(httpClient.get(COLLABORATOR_ENDPOINTS.post(id), { signal })),
  createPost: (payload, signal) => requestData(httpClient.post(COLLABORATOR_ENDPOINTS.posts, createPostForm(payload), { signal })),
  updatePost: (id, payload, signal) => requestData(httpClient.put(COLLABORATOR_ENDPOINTS.post(id), updatePostForm(payload), { signal })),
  deletePost: (id, signal) => requestData(httpClient.delete(COLLABORATOR_ENDPOINTS.post(id), { signal })),
  getAnalytics: (id, range, signal) => requestData(httpClient.get(COLLABORATOR_ENDPOINTS.analytics(id), { params: { range }, signal })),
  getHashtags: (params, signal) => requestData(httpClient.get(COLLABORATOR_ENDPOINTS.hashtags, { params: compactParams(params), signal })),
  like: (id, signal) => requestData(httpClient.put(COLLABORATOR_ENDPOINTS.like(id), undefined, { signal })),
  unlike: (id, signal) => requestData(httpClient.delete(COLLABORATOR_ENDPOINTS.like(id), { signal })),
  comment: (id, content, signal) => requestData(httpClient.post(COLLABORATOR_ENDPOINTS.comments(id), { content }, { signal })),
  reply: (id, content, signal) => requestData(httpClient.post(COLLABORATOR_ENDPOINTS.replies(id), { content }, { signal })),
  repost: (id, signal) => requestData(httpClient.put(COLLABORATOR_ENDPOINTS.repost(id), undefined, { signal })),
  unrepost: (id, signal) => requestData(httpClient.delete(COLLABORATOR_ENDPOINTS.repost(id), { signal })),
});
