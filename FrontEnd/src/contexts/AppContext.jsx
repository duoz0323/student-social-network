/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useMemo, useRef, useState } from 'react';
import { adminApi, postApi, socialApi } from '../api/index.js';
import { initialData } from '../data/mockData.js';
import { useAuth } from '../features/auth/hooks/useAuth.js';
import { toPostView } from '../features/post/utils/postViewModel.js';
import Toast from '../components/common/Toast.jsx';
import {
  invalidateUserBlockCaches,
  removeBlockedUserFromState,
} from '../features/profile/utils/userBlockState.js';

const AppContext = createContext(null);

function toViewUser(user) {
  if (!user) return null;
  const profile = user.profile ?? {};
  return {
    ...user,
    displayName: profile.displayName ?? user.displayName ?? '',
    avatarUrl: profile.avatarUrl ?? user.avatarUrl ?? '',
    birthDate: profile.dateOfBirth ?? user.birthDate ?? null,
    bio: profile.bio ?? user.bio ?? '',
    profileCompletedAt: profile.profileCompletedAt ?? user.profileCompletedAt ?? null,
  };
}

export function AppProvider({ children }) {
  const auth = useAuth();
  // Context chỉ giữ snapshot dùng chung cho tương tác và hồ sơ; các danh sách Post tự tải qua service chuyên biệt.
  const [data, setData] = useState(() => initialData);
  const [toast, setToast] = useState(null);
  const [userRelationshipRevision, setUserRelationshipRevision] = useState(0);
  const createPostInFlightRef = useRef(false);
  const currentUserId = auth.user?.id ?? null;

  const currentUser = useMemo(() => {
    const knownUser = toViewUser(data.users.find((user) => String(user.id) === String(currentUserId)));
    return knownUser ?? {
      id: currentUserId,
      role: auth.role,
      status: 'ACTIVE',
      displayName: 'Người dùng UniShare',
      avatarUrl: '',
      birthDate: null,
      bio: '',
      profileCompletedAt: auth.profileCompleted ? 'AUTHENTICATED_SESSION' : null,
    };
  }, [auth.profileCompleted, auth.role, currentUserId, data.users]);

  const value = useMemo(() => {
    const users = data.users.map(toViewUser);
    const publicPosts = data.posts.filter((post) => post.status === 'PUBLISHED');

    function getUserById(userId) {
      return users.find((user) => String(user.id) === String(userId)) ?? null;
    }

    function getPostById(postId, includeHidden = false) {
      return data.posts.find((post) => String(post.id) === String(postId)
        && (includeHidden || post.status === 'PUBLISHED')) ?? null;
    }

    async function createPost(payload) {
      // Lớp phòng vệ thứ hai: không cho bất kỳ composer nào gửi thêm POST khi request trước chưa kết thúc.
      if (createPostInFlightRef.current) {
        return { ok: false, message: 'Bài viết đang được đăng. Vui lòng chờ trong giây lát.' };
      }
      createPostInFlightRef.current = true;

      try {
        const response = await postApi.create(payload);
        setData((previous) => ({ ...previous, posts: [toPostView(response), ...previous.posts] }));
        return { ok: true, data: response };
      } catch (error) {
        return { ok: false, message: error.message };
      } finally {
        createPostInFlightRef.current = false;
      }
    }

    async function updatePost(postId, payload) {
      const current = data.posts.find((post) => String(post.id) === String(postId));
      const response = await postApi.update(postId, {
        content: payload.content,
        hashtag: payload.hashtag ?? payload.hashtags?.split(',')[0],
        keepMediaIds: current?.media?.map((item) => item.id) ?? [],
        newMediaFiles: payload.newMediaFiles ?? [],
      });
      setData((previous) => ({
        ...previous,
        posts: previous.posts.map((post) => String(post.id) === String(postId) ? toPostView(response) : post),
      }));
      return response;
    }

    async function deletePost(postId) {
      await postApi.remove(postId);
      setData((previous) => ({
        ...previous,
        posts: previous.posts.map((post) => String(post.id) === String(postId) ? { ...post, status: 'DELETED' } : post),
      }));
    }

    async function toggleLike(postId, currentLiked) {
      const post = data.posts.find((item) => String(item.id) === String(postId));
      const liked = currentLiked ?? post?.likedByCurrentUser
        ?? data.likes.some((item) => String(item.postId) === String(postId) && String(item.userId) === String(currentUserId));
      const response = liked ? await postApi.unlike(postId) : await postApi.like(postId);
      setData((previous) => ({
        ...previous,
        posts: previous.posts.map((item) => String(item.id) === String(postId)
          ? { ...item, likeCount: response.likeCount, likedByCurrentUser: response.likedByCurrentUser }
          : item),
      }));
      return response;
    }

    async function toggleSave(postId, currentSaved) {
      const post = data.posts.find((item) => String(item.id) === String(postId));
      const saved = currentSaved ?? post?.savedByCurrentUser
        ?? data.savedPosts.some((item) => String(item.postId) === String(postId) && String(item.userId) === String(currentUserId));
      const response = saved ? await postApi.unsave(postId) : await postApi.save(postId);
      setData((previous) => ({
        ...previous,
        savedPosts: response.saved
          ? [...previous.savedPosts.filter((item) => String(item.postId) !== String(postId)), { postId, userId: currentUserId }]
          : previous.savedPosts.filter((item) => String(item.postId) !== String(postId)),
        posts: previous.posts.map((item) => String(item.id) === String(postId)
          ? { ...item, savedByCurrentUser: response.saved }
          : item),
      }));
      return response;
    }

    async function addComment(postId, content) {
      const response = await postApi.createComment(postId, content.trim());
      setData((previous) => ({
        ...previous,
        comments: [...previous.comments, { ...response, id: response.commentId, authorId: response.userId }],
        posts: previous.posts.map((post) => String(post.id) === String(postId)
          ? { ...post, commentCount: post.commentCount + 1 }
          : post),
      }));
      return response;
    }

    async function deleteComment(commentId) {
      await postApi.deleteComment(commentId);
      setData((previous) => ({ ...previous, comments: previous.comments.filter((item) => String(item.id) !== String(commentId)) }));
    }

    async function toggleFollow(targetUserId) {
      const following = data.follows.some((item) => String(item.followerId) === String(currentUserId)
        && String(item.followingId) === String(targetUserId));
      const response = following ? await socialApi.unfollow(targetUserId) : await socialApi.follow(targetUserId);
      setData((previous) => ({
        ...previous,
        follows: response.followedByCurrentUser
          ? [...previous.follows, { followerId: currentUserId, followingId: targetUserId }]
          : previous.follows.filter((item) => !(String(item.followerId) === String(currentUserId)
            && String(item.followingId) === String(targetUserId))),
      }));
      return response;
    }

    function invalidateUserRelationshipData() {
      // Revision buộc các màn hình giữ state cục bộ như Post Detail/Comment tải lại từ Backend.
      invalidateUserBlockCaches();
      setUserRelationshipRevision((revision) => revision + 1);
    }

    function applyUserBlock(targetUserId) {
      // Một điểm cập nhật chung loại dữ liệu bị chặn khỏi Context và cache cursor liên quan.
      invalidateUserRelationshipData();
      setData((previous) => removeBlockedUserFromState(previous, currentUserId, targetUserId));
    }

    function showToast(message, type = 'success') {
      // Toast đặt tại Provider để không biến mất khi thao tác Block điều hướng sang route khác.
      setToast({ message, type });
    }

    async function updateProfile(payload) {
      const response = await socialApi.updateProfile({
        displayName: payload.displayName,
        dateOfBirth: payload.dateOfBirth ?? payload.birthDate,
        bio: payload.bio,
      });
      setData((previous) => ({
        ...previous,
        users: previous.users.map((user) => String(user.id) === String(currentUserId)
          ? { ...user, profile: { ...user.profile, ...response } }
          : user),
      }));
      return response;
    }

    async function submitReport(postId, reason, description) {
      try {
        return { ok: true, data: await postApi.report(postId, { reason, description: description.trim() }) };
      } catch (error) {
        return { ok: false, message: error.message };
      }
    }

    async function setUserStatus(userId, status) {
      const response = status === 'BLOCKED'
        ? await adminApi.blockUser(userId, 'OTHER')
        : await adminApi.unblockUser(userId);
      setData((previous) => ({
        ...previous,
        users: previous.users.map((user) => String(user.id) === String(userId) ? { ...user, status: response.status } : user),
      }));
      return response;
    }

    async function setPostStatus(postId, status) {
      const response = status === 'HIDDEN'
        ? await adminApi.hidePost(postId, 'OTHER')
        : await adminApi.restorePost(postId);
      setData((previous) => ({
        ...previous,
        posts: previous.posts.map((post) => String(post.id) === String(postId) ? { ...post, status: response.status } : post),
      }));
      return response;
    }

    async function setReportStatus(reportId, status) {
      const response = status === 'REJECTED'
        ? await adminApi.rejectReport(reportId, 'Báo cáo không đủ căn cứ.')
        : await adminApi.resolveReport(reportId, { resolutionNote: 'Báo cáo hợp lệ.', hidePost: false });
      setData((previous) => ({
        ...previous,
        reports: previous.reports.map((report) => String(report.id) === String(reportId) ? { ...report, status: response.status } : report),
      }));
      return response;
    }

    return {
      data: { ...data, users }, currentUser, currentUserId, userRelationshipRevision,
      publicPosts, getUserById, getPostById,
      logout: auth.logout, createPost, updatePost, deletePost, toggleLike, toggleSave, addComment,
      deleteComment, toggleFollow, applyUserBlock, invalidateUserRelationshipData,
      showToast, updateProfile, submitReport,
      setUserStatus, setPostStatus, setReportStatus,
    };
  }, [auth.logout, currentUser, currentUserId, data, userRelationshipRevision]);

  return (
    <AppContext.Provider value={value}>
      {children}
      {toast ? <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} /> : null}
    </AppContext.Provider>
  );
}

export function useApp() {
  const context = useContext(AppContext);
  if (!context) throw new Error('useApp must be used inside AppProvider');
  return context;
}
