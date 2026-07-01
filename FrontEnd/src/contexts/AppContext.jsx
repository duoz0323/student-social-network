/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useMemo, useState } from 'react';
import { initialData } from '../data/mockData.js';
import { normalizeText } from '../utils/formatters.js';

const AppContext = createContext(null);
const SESSION_KEY = 'unishare.react.session';
const DATA_KEY = 'unishare.react.mock-data';
const SESSION_DURATION_MS = 1000 * 60 * 60 * 8;

function makeId(prefix) {
  return `${prefix}-${Date.now()}`;
}

function isEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

function isPhoneNumber(value) {
  return /^\+?\d{9,15}$/.test(value);
}

function normalizeIdentifier(identifier) {
  const cleanIdentifier = identifier.trim();
  if (isEmail(cleanIdentifier)) {
    return { type: 'email', email: normalizeText(cleanIdentifier), phoneNumber: null };
  }
  if (isPhoneNumber(cleanIdentifier)) {
    return { type: 'phone', email: null, phoneNumber: cleanIdentifier.replace(/\s+/g, '') };
  }
  return null;
}

function readStoredData() {
  try {
    const raw = localStorage.getItem(DATA_KEY);
    return raw ? JSON.parse(raw) : initialData;
  } catch {
    localStorage.removeItem(DATA_KEY);
    return initialData;
  }
}

function sanitizeDataForStorage(data) {
  return {
    ...data,
    currentUserId: null,
    demoAccounts: data.demoAccounts.map((account) => {
      const safeAccount = { ...account };
      delete safeAccount.passwordDemo;
      return safeAccount;
    }),
  };
}

function readStoredSession(users) {
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) return null;

    const session = JSON.parse(raw);
    const user = users.find((item) => item.id === session.userId);
    const validSession =
      typeof session.userId === 'string' &&
      typeof session.role === 'string' &&
      Number(session.expiresAt) > Date.now() &&
      user &&
      user.status === 'ACTIVE';

    if (!validSession) {
      localStorage.removeItem(SESSION_KEY);
      return null;
    }

    return session;
  } catch {
    localStorage.removeItem(SESSION_KEY);
    return null;
  }
}

function saveSession(user) {
  const session = {
    userId: user.id,
    role: user.role,
    expiresAt: Date.now() + SESSION_DURATION_MS,
  };
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

function clearSession() {
  localStorage.removeItem(SESSION_KEY);
}

function toViewUser(user) {
  if (!user) return null;
  const profile = user.profile ?? {};
  return {
    ...user,
    displayName: profile.displayName ?? '',
    avatarUrl: profile.avatarUrl ?? '',
    birthDate: profile.dateOfBirth ?? null,
    bio: profile.bio ?? '',
    profileCompletedAt: profile.profileCompletedAt ?? null,
  };
}

async function sha256(value) {
  // SHA-256 chi phuc vu mock Frontend de khong luu mat khau ro trong localStorage; Backend that phai dung BCrypt.
  const bytes = new TextEncoder().encode(value);
  const hashBuffer = await crypto.subtle.digest('SHA-256', bytes);
  return Array.from(new Uint8Array(hashBuffer))
    .map((item) => item.toString(16).padStart(2, '0'))
    .join('');
}

function persistData(data) {
  localStorage.setItem(DATA_KEY, JSON.stringify(sanitizeDataForStorage(data)));
}

export function AppProvider({ children }) {
  const [data, setDataState] = useState(() => readStoredData());
  const [currentUserId, setCurrentUserId] = useState(() => readStoredSession(readStoredData().users)?.userId ?? null);
  const [sessionExpired, setSessionExpired] = useState(false);

  function setData(updater) {
    setDataState((prev) => {
      const next = typeof updater === 'function' ? updater(prev) : updater;
      persistData(next);
      return next;
    });
  }

  const currentUser = toViewUser(data.users.find((user) => user.id === currentUserId) ?? null);

  const value = useMemo(() => {
    const viewUsers = data.users.map(toViewUser);
    const viewData = { ...data, users: viewUsers };
    const publicPosts = data.posts.filter((post) => post.status === 'PUBLISHED');

    function getRawUserById(userId) {
      return data.users.find((user) => user.id === userId) ?? null;
    }

    function getUserById(userId) {
      return toViewUser(getRawUserById(userId));
    }

    function getPostById(postId, includeHidden = false) {
      return data.posts.find((post) => post.id === postId && (includeHidden || post.status === 'PUBLISHED')) ?? null;
    }

    async function login(identifier, password) {
      const normalized = normalizeIdentifier(identifier);
      if (!normalized) {
        return { ok: false, message: 'Email hoac so dien thoai khong hop le.' };
      }

      const passwordHash = await sha256(password);
      const account = data.demoAccounts.find((item) => {
        const emailMatches = normalized.email && item.email === normalized.email;
        const phoneMatches = normalized.phoneNumber && item.phoneNumber === normalized.phoneNumber;
        return (emailMatches || phoneMatches) && (item.passwordHash === passwordHash || item.passwordDemo === password);
      });

      if (!account) {
        return { ok: false, message: 'Email/so dien thoai hoac mat khau khong dung.' };
      }

      const user = getRawUserById(account.userId);
      if (!user || account.status === 'BLOCKED' || user.status === 'BLOCKED') {
        return { ok: false, message: 'Tai khoan da bi khoa, vui long lien he quan tri vien.' };
      }

      saveSession(user);
      setCurrentUserId(user.id);
      setSessionExpired(false);
      return { ok: true, role: user.role, profileCompleted: Boolean(user.profile?.profileCompletedAt) };
    }

    async function register(payload) {
      const normalized = normalizeIdentifier(payload.identifier);
      if (!normalized) {
        return { ok: false, message: 'Email hoac so dien thoai khong hop le.' };
      }

      const duplicated = data.demoAccounts.some(
        (account) =>
          (normalized.email && account.email === normalized.email) ||
          (normalized.phoneNumber && account.phoneNumber === normalized.phoneNumber),
      );

      if (duplicated) {
        return { ok: false, message: 'Email hoac so dien thoai da ton tai.' };
      }

      const userId = makeId('user');
      const accountId = makeId('account');
      const passwordHash = await sha256(payload.password);
      const user = {
        id: userId,
        email: normalized.email,
        phoneNumber: normalized.phoneNumber,
        role: 'USER',
        status: 'ACTIVE',
        profile: {
          displayName: null,
          avatarUrl: null,
          dateOfBirth: null,
          bio: null,
          profileCompletedAt: null,
        },
        followerCount: 0,
        followingCount: 0,
      };

      const account = {
        id: accountId,
        email: normalized.email,
        phoneNumber: normalized.phoneNumber,
        passwordHash,
        role: 'USER',
        status: 'ACTIVE',
        userId,
      };

      setData((prev) => ({
        ...prev,
        users: [...prev.users, user],
        demoAccounts: [...prev.demoAccounts, account],
      }));
      saveSession(user);
      setCurrentUserId(userId);
      setSessionExpired(false);
      return { ok: true };
    }

    function logout() {
      clearSession();
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
        users: prev.users.map((user) =>
          user.id === currentUserId
            ? {
                ...user,
                profile: {
                  ...user.profile,
                  displayName: payload.displayName ?? user.profile?.displayName ?? null,
                  avatarUrl: payload.avatarUrl ?? user.profile?.avatarUrl ?? null,
                  dateOfBirth: payload.dateOfBirth ?? payload.birthDate ?? user.profile?.dateOfBirth ?? null,
                  bio: payload.bio ?? user.profile?.bio ?? null,
                },
              }
            : user,
        ),
      }));
    }

    function completeOnboarding(payload) {
      const displayName = payload.displayName.trim();
      if (!displayName) {
        return { ok: false, message: 'Ten hien thi la bat buoc de hoan tat ho so.' };
      }

      setData((prev) => ({
        ...prev,
        users: prev.users.map((user) =>
          user.id === currentUserId
            ? {
                ...user,
                profile: {
                  ...user.profile,
                  displayName,
                  avatarUrl: payload.avatarUrl || null,
                  dateOfBirth: payload.dateOfBirth || null,
                  bio: payload.bio.trim() || null,
                  profileCompletedAt: new Date().toISOString(),
                },
              }
            : user,
        ),
      }));
      return { ok: true };
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

    function handleFutureSocialAuth() {
      return { ok: false, message: 'Tinh nang dang duoc phat trien.' };
    }

    return {
      data: viewData,
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
      completeOnboarding,
      submitReport,
      setUserStatus,
      setPostStatus,
      setReportStatus,
      handleFutureSocialAuth,
    };
  }, [currentUser, currentUserId, data, sessionExpired]);

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useApp() {
  const context = useContext(AppContext);
  if (!context) throw new Error('useApp must be used inside AppProvider');
  return context;
}
