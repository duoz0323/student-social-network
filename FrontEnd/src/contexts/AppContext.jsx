/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useMemo, useState } from 'react';
import { initialData } from '../data/mockData.js';
import { normalizeText } from '../utils/formatters.js';

const AppContext = createContext(null);

function makeId(prefix) {
  return `${prefix}-${Date.now()}`;
}

export function AppProvider({ children }) {
  const [data, setData] = useState(initialData);
  const [currentUserId, setCurrentUserId] = useState(initialData.currentUserId);
  const [sessionExpired, setSessionExpired] = useState(false);

  const currentUser = data.users.find((user) => user.id === currentUserId) ?? null;

  const value = useMemo(() => {
    const publicPosts = data.posts.filter((post) => post.status === 'PUBLISHED');

    function getUserById(userId) {
      return data.users.find((user) => user.id === userId) ?? null;
    }

    function getPostById(postId, includeHidden = false) {
      return data.posts.find((post) => post.id === postId && (includeHidden || post.status === 'PUBLISHED')) ?? null;
    }

    function login(identifier, password) {
      const account = data.demoAccounts.find(
        (item) =>
          (item.email === normalizeText(identifier) || item.phoneNumber === identifier.trim()) &&
          item.passwordDemo === password,
      );

      if (!account) {
        return { ok: false, message: 'Email/so dien thoai hoac mat khau khong dung.' };
      }

      if (account.status === 'BLOCKED') {
        return { ok: false, message: 'Tai khoan da bi khoa, vui long lien he quan tri vien.' };
      }

      setCurrentUserId(account.userId);
      setSessionExpired(false);
      return { ok: true, role: account.role };
    }

    function register(payload) {
      const email = normalizeText(payload.email);
      const phoneNumber = payload.phoneNumber.trim();
      const duplicated = data.demoAccounts.some((account) => account.email === email || account.phoneNumber === phoneNumber);

      if (duplicated) {
        return { ok: false, message: 'Email hoac so dien thoai da ton tai.' };
      }

      const userId = makeId('user');
      const accountId = makeId('account');

      // Display name la du lieu ho so, tam dat mac dinh de nguoi dung cap nhat sau dang ky.
      setData((prev) => ({
        ...prev,
        users: [
          ...prev.users,
          {
            id: userId,
            displayName: 'Nguoi dung moi',
            avatarUrl: '',
            bio: '',
            birthDate: null,
            role: 'USER',
            status: 'ACTIVE',
            followerCount: 0,
            followingCount: 0,
          },
        ],
        demoAccounts: [
          ...prev.demoAccounts,
          { id: accountId, email, phoneNumber, passwordDemo: payload.password, role: 'USER', status: 'ACTIVE', userId },
        ],
      }));
      return { ok: true };
    }

    function logout() {
      setCurrentUserId(null);
    }

    function createPost({ content, hashtags }) {
      const cleanContent = content.trim();
      const normalizedHashtags = hashtags
        .split(/[,\s]+/)
        .map((tag) => tag.replace('#', '').trim().toLowerCase())
        .filter(Boolean);

      if (!cleanContent) {
        return { ok: false, message: 'Bai viet can co noi dung trong MVP mock.' };
      }

      const post = {
        id: makeId('post'),
        authorId: currentUserId,
        content: cleanContent,
        imageUrls: [],
        hashtags: [...new Set(normalizedHashtags)].slice(0, 8),
        status: 'PUBLISHED',
        likeCount: 0,
        commentCount: 0,
        edited: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      setData((prev) => ({ ...prev, posts: [post, ...prev.posts] }));
      return { ok: true };
    }

    function updatePost(postId, payload) {
      setData((prev) => ({
        ...prev,
        posts: prev.posts.map((post) =>
          post.id === postId && post.authorId === currentUserId
            ? {
                ...post,
                content: payload.content.trim(),
                hashtags: payload.hashtags
                  .split(/[,\s]+/)
                  .map((tag) => tag.replace('#', '').trim().toLowerCase())
                  .filter(Boolean),
                edited: true,
                updatedAt: new Date().toISOString(),
              }
            : post,
        ),
      }));
    }

    function deletePost(postId) {
      setData((prev) => ({
        ...prev,
        posts: prev.posts.map((post) => (post.id === postId && post.authorId === currentUserId ? { ...post, status: 'DELETED' } : post)),
      }));
    }

    function toggleLike(postId) {
      const liked = data.likes.some((like) => like.postId === postId && like.userId === currentUserId);
      setData((prev) => ({
        ...prev,
        likes: liked
          ? prev.likes.filter((like) => !(like.postId === postId && like.userId === currentUserId))
          : [...prev.likes, { postId, userId: currentUserId, createdAt: new Date().toISOString() }],
        posts: prev.posts.map((post) =>
          post.id === postId ? { ...post, likeCount: Math.max(0, post.likeCount + (liked ? -1 : 1)) } : post,
        ),
      }));
    }

    function toggleSave(postId) {
      const saved = data.savedPosts.some((item) => item.postId === postId && item.userId === currentUserId);
      setData((prev) => ({
        ...prev,
        savedPosts: saved
          ? prev.savedPosts.filter((item) => !(item.postId === postId && item.userId === currentUserId))
          : [...prev.savedPosts, { postId, userId: currentUserId, createdAt: new Date().toISOString() }],
      }));
    }

    function addComment(postId, content) {
      const cleanContent = content.trim();
      if (!cleanContent) return;

      setData((prev) => ({
        ...prev,
        comments: [
          ...prev.comments,
          { id: makeId('comment'), postId, authorId: currentUserId, content: cleanContent, createdAt: new Date().toISOString() },
        ],
        posts: prev.posts.map((post) => (post.id === postId ? { ...post, commentCount: post.commentCount + 1 } : post)),
      }));
    }

    function deleteComment(commentId) {
      const target = data.comments.find((comment) => comment.id === commentId);
      if (!target || target.authorId !== currentUserId) return;

      setData((prev) => ({
        ...prev,
        comments: prev.comments.filter((comment) => comment.id !== commentId),
        posts: prev.posts.map((post) =>
          post.id === target.postId ? { ...post, commentCount: Math.max(0, post.commentCount - 1) } : post,
        ),
      }));
    }

    function toggleFollow(targetUserId) {
      if (targetUserId === currentUserId) return;
      const following = data.follows.some((follow) => follow.followerId === currentUserId && follow.followingId === targetUserId);

      setData((prev) => ({
        ...prev,
        follows: following
          ? prev.follows.filter((follow) => !(follow.followerId === currentUserId && follow.followingId === targetUserId))
          : [...prev.follows, { followerId: currentUserId, followingId: targetUserId, createdAt: new Date().toISOString() }],
        users: prev.users.map((user) => {
          if (user.id === currentUserId) return { ...user, followingCount: Math.max(0, user.followingCount + (following ? -1 : 1)) };
          if (user.id === targetUserId) return { ...user, followerCount: Math.max(0, user.followerCount + (following ? -1 : 1)) };
          return user;
        }),
      }));
    }

    function updateProfile(payload) {
      setData((prev) => ({
        ...prev,
        users: prev.users.map((user) => (user.id === currentUserId ? { ...user, ...payload } : user)),
      }));
    }

    function submitReport(postId, reason, description) {
      const existed = data.reports.some(
        (report) => report.postId === postId && report.reporterId === currentUserId && report.status === 'PENDING',
      );
      if (existed) return { ok: false, message: 'Ban da gui bao cao dang cho xu ly cho bai viet nay.' };

      setData((prev) => ({
        ...prev,
        reports: [
          ...prev.reports,
          {
            id: makeId('report'),
            postId,
            reporterId: currentUserId,
            reason,
            description: description.trim(),
            status: 'PENDING',
            createdAt: new Date().toISOString(),
          },
        ],
      }));
      return { ok: true };
    }

    function setUserStatus(userId, status) {
      setData((prev) => ({
        ...prev,
        users: prev.users.map((user) => (user.id === userId ? { ...user, status } : user)),
        demoAccounts: prev.demoAccounts.map((account) => (account.userId === userId ? { ...account, status } : account)),
      }));
    }

    function setPostStatus(postId, status) {
      setData((prev) => ({ ...prev, posts: prev.posts.map((post) => (post.id === postId ? { ...post, status } : post)) }));
    }

    function setReportStatus(reportId, status) {
      setData((prev) => ({ ...prev, reports: prev.reports.map((report) => (report.id === reportId ? { ...report, status } : report)) }));
    }

    return {
      data,
      currentUser,
      currentUserId,
      sessionExpired,
      publicPosts,
      setSessionExpired,
      getUserById,
      getPostById,
      login,
      register,
      logout,
      createPost,
      updatePost,
      deletePost,
      toggleLike,
      toggleSave,
      addComment,
      deleteComment,
      toggleFollow,
      updateProfile,
      submitReport,
      setUserStatus,
      setPostStatus,
      setReportStatus,
    };
  }, [currentUser, currentUserId, data, sessionExpired]);

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useApp() {
  const context = useContext(AppContext);
  if (!context) throw new Error('useApp must be used inside AppProvider');
  return context;
}
