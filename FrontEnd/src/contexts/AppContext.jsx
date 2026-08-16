/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { adminApi, postApi, socialApi } from '../api/index.js';
import { initialData } from '../data/mockData.js';
import { useAuth } from '../features/auth/hooks/useAuth.js';
import { toPostView } from '../features/post/utils/postViewModel.js';
import Toast from '../components/common/Toast.jsx';
import {
  invalidateUserBlockCaches,
  removeBlockedUserFromState,
} from '../features/profile/utils/userBlockState.js';
import { resolveCurrentFollowState } from '../features/profile/utils/followListState.js';
import { publishPostActivity } from '../features/post/utils/postActivitySync.js';

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
  // Context giữ snapshot dùng chung; tự động đồng bộ profile của tài khoản thật khi đã đăng nhập.
  const [data, setData] = useState(() => initialData);
  const [myProfile, setMyProfile] = useState(null);
  const [toast, setToast] = useState(null);
  const [userRelationshipRevision, setUserRelationshipRevision] = useState(0);
  const createPostInFlightRef = useRef(false);
  const currentUserId = auth.user?.id ?? null;

  const refreshMyProfile = useCallback(async () => {
    if (!currentUserId || !auth.profileCompleted) {
      setMyProfile(null);
      return null;
    }
    try {
      const profile = await socialApi.getMyProfile();
      if (profile) {
        setMyProfile({
          id: profile.userId ?? profile.id ?? currentUserId,
          displayName: profile.displayName ?? '',
          avatarUrl: profile.avatarUrl ?? '',
          birthDate: profile.dateOfBirth ?? profile.birthDate ?? null,
          bio: profile.bio ?? '',
          profileCompletedAt: profile.profileCompletedAt ?? null,
          school: profile.school ?? null,
          faculty: profile.faculty ?? null,
          major: profile.major ?? null,
          entryYear: profile.entryYear ?? null,
          interests: profile.interests ?? [],
        });
      }
      return profile;
    } catch {
      return null;
    }
  }, [currentUserId, auth.profileCompleted]);

  useEffect(() => {
    if (currentUserId && auth.profileCompleted) {
      // Đồng bộ profile từ API khi phiên xác thực trở nên đủ điều kiện.
      // eslint-disable-next-line react-hooks/set-state-in-effect
      void refreshMyProfile();
    } else {
      setMyProfile(null);
    }
  }, [currentUserId, auth.profileCompleted, refreshMyProfile]);

  const currentUser = useMemo(() => {
    const knownUser = toViewUser(data.users.find((user) => String(user.id) === String(currentUserId)));
    const authUser = auth.user;
    return {
      id: currentUserId,
      role: auth.role,
      status: 'ACTIVE',
      displayName: myProfile?.displayName || authUser?.displayName || knownUser?.displayName || 'Người dùng UniShare',
      avatarUrl: myProfile?.avatarUrl ?? authUser?.avatarUrl ?? knownUser?.avatarUrl ?? '',
      birthDate: myProfile?.birthDate ?? authUser?.birthDate ?? knownUser?.birthDate ?? null,
      bio: myProfile?.bio ?? authUser?.bio ?? knownUser?.bio ?? '',
      profileCompletedAt: myProfile?.profileCompletedAt ?? (auth.profileCompleted ? 'AUTHENTICATED_SESSION' : null),
    };
  }, [auth.profileCompleted, auth.role, auth.user, currentUserId, data.users, myProfile]);

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

    async function getPostDetail(postId, signal) {
      // Form sửa luôn dùng response chi tiết mới nhất, không dùng snapshot rút gọn của Feed/Search.
      return toPostView(await postApi.getDetail(postId, signal));
    }

    async function createPost(payload) {
      if (createPostInFlightRef.current) {
        return { ok: false, message: 'Bài viết đang được đăng. Vui lòng chờ trong giây lát.' };
      }
      createPostInFlightRef.current = true;

      try {
        const response = await postApi.create(payload);
        setData((previous) => ({ ...previous, posts: [toPostView(response), ...previous.posts] }));
        publishPostActivity({
          postId: response.postId ?? response.id,
          viewerUserId: currentUserId,
          invalidateCacheKeys: [
            `profile-posts:${currentUserId}`,
            'feed:for-you',
            'feed:following',
          ],
        });
        return { ok: true, data: response };
      } catch (error) {
        // Giữ mã lỗi và details an toàn để composer phân biệt WARNING/BLOCK/unavailable mà không reset draft.
        return { ok: false, code: error.code, details: error.details, message: error.message };
      } finally {
        createPostInFlightRef.current = false;
      }
    }

    async function updatePost(postId, payload) {
      const current = data.posts.find((post) => String(post.id) === String(postId));
      const response = await postApi.update(postId, {
        content: payload.content,
        hashtag: payload.hashtag ?? payload.hashtags?.split(',')[0],
        keepMediaIds: payload.keepMediaIds ?? current?.media?.map((item) => item.id) ?? [],
        newMediaFiles: payload.newMediaFiles ?? [],
        locationAction: payload.locationAction,
        location: payload.location,
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

    async function toggleFollow(targetUserId, followedByCurrentUser) {
      const following = resolveCurrentFollowState(
        followedByCurrentUser,
        data.follows,
        currentUserId,
        targetUserId,
      );
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
      invalidateUserBlockCaches();
      setUserRelationshipRevision((revision) => revision + 1);
    }

    function applyUserBlock(targetUserId) {
      invalidateUserRelationshipData();
      setData((previous) => removeBlockedUserFromState(previous, currentUserId, targetUserId));
    }

    function showToast(message, type = 'success') {
      setToast({ message, type });
    }

    async function updateProfile(payload) {
      const response = await socialApi.updateProfile({
        displayName: payload.displayName,
        dateOfBirth: payload.dateOfBirth ?? payload.birthDate,
        bio: payload.bio,
        ...(Object.hasOwn(payload, 'academic') ? { academic: payload.academic } : {}),
        ...(Object.hasOwn(payload, 'interestIds') ? { interestIds: payload.interestIds } : {}),
      });
      setMyProfile((current) => ({
        ...(current ?? {}),
        id: currentUserId,
        displayName: response.displayName ?? payload.displayName,
        bio: response.bio ?? payload.bio ?? '',
        birthDate: response.dateOfBirth ?? payload.dateOfBirth ?? payload.birthDate ?? null,
        school: response.school ?? null,
        faculty: response.faculty ?? null,
        major: response.major ?? null,
        entryYear: response.entryYear ?? null,
        interests: response.interests ?? [],
      }));
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
        reports: previous.reports.map((report) => String(report.id) === String(reportId)
          ? { ...report, status: response.status }
          : report),
      }));
      return response;
    }

    return {
      data: { ...data, users }, currentUser, currentUserId, userRelationshipRevision,
      publicPosts, getUserById, getPostById, getPostDetail,
      logout: auth.logout, createPost, updatePost, deletePost, toggleLike, toggleSave, addComment,
      deleteComment, toggleFollow, applyUserBlock, invalidateUserRelationshipData,
      showToast, updateProfile, refreshMyProfile, submitReport,
      setUserStatus, setPostStatus, setReportStatus,
    };
  }, [auth.logout, currentUser, currentUserId, data, refreshMyProfile, userRelationshipRevision]);

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
