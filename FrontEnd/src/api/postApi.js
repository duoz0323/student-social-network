import { POST_ENDPOINTS } from './apiEndpoints.js';
import { httpClient } from './httpClient.js';
import { compactParams, requestData } from './requestData.js';

function appendValue(formData, key, value) {
  if (value !== undefined && value !== null && value !== '') formData.append(key, value);
}

function createPostForm(payload) {
  const formData = new FormData();
  appendValue(formData, 'content', payload.content?.trim());
  appendValue(formData, 'hashtag', payload.hashtag?.trim());
  for (const file of payload.mediaFiles ?? []) formData.append('mediaFiles', file);
  return formData;
}

function updatePostForm(payload) {
  const formData = new FormData();
  appendValue(formData, 'content', payload.content?.trim());
  appendValue(formData, 'hashtag', payload.hashtag?.trim());
  for (const id of payload.keepMediaIds ?? []) formData.append('keepMediaIds', id);
  for (const file of payload.newMediaFiles ?? []) formData.append('newMediaFiles', file);
  return formData;
}

export const postApi = Object.freeze({
  create: (payload, signal) => requestData(httpClient.post(POST_ENDPOINTS.root, createPostForm(payload), { signal })),
  getDetail: (postId, signal) => requestData(httpClient.get(POST_ENDPOINTS.detail(postId), { signal })),
  getSaved: (params, signal) => requestData(httpClient.get(POST_ENDPOINTS.saved, { params: compactParams(params), signal })),
  getLiked: (params, signal) => requestData(httpClient.get(POST_ENDPOINTS.liked, { params: compactParams(params), signal })),
  update: (postId, payload, signal) => requestData(httpClient.put(POST_ENDPOINTS.detail(postId), updatePostForm(payload), { signal })),
  remove: (postId, signal) => requestData(httpClient.delete(POST_ENDPOINTS.detail(postId), { signal })),
  like: (postId, signal) => requestData(httpClient.post(POST_ENDPOINTS.likes(postId), undefined, { signal })),
  unlike: (postId, signal) => requestData(httpClient.delete(POST_ENDPOINTS.likes(postId), { signal })),
  save: (postId, signal) => requestData(httpClient.post(POST_ENDPOINTS.saves(postId), undefined, { signal })),
  unsave: (postId, signal) => requestData(httpClient.delete(POST_ENDPOINTS.saves(postId), { signal })),
  report: (postId, payload, signal) => requestData(httpClient.post(POST_ENDPOINTS.reports(postId), payload, { signal })),
  getComments: (postId, params, signal) => requestData(httpClient.get(POST_ENDPOINTS.comments(postId), { params: compactParams(params), signal })),
  createComment: (postId, content, signal) => requestData(httpClient.post(POST_ENDPOINTS.comments(postId), { content }, { signal })),
  getReplies: (commentId, params, signal) => requestData(httpClient.get(POST_ENDPOINTS.replies(commentId), { params: compactParams(params), signal })),
  createReply: (commentId, content, signal) => requestData(httpClient.post(POST_ENDPOINTS.replies(commentId), { content }, { signal })),
  deleteComment: (commentId, signal) => requestData(httpClient.delete(POST_ENDPOINTS.comment(commentId), { signal })),
  suggestHashtags: (keyword, signal) => requestData(httpClient.get(POST_ENDPOINTS.hashtagSuggestions, { params: compactParams({ keyword }), signal })),
});
