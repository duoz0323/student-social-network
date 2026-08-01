export function sameUserId(firstId, secondId) {
  return String(firstId) === String(secondId);
}

export function normalizeFollowUser(user) {
  return {
    ...user,
    id: user.userId,
    // Backend là nguồn sự thật cho quan hệ giữa người xem hiện tại và từng user trong danh sách.
    followedByCurrentUser: Boolean(user.followedByCurrentUser),
  };
}

export function updateFollowStateInLists(lists, userId, followedByCurrentUser) {
  const updateList = (users) => users.map((user) => (
    sameUserId(user.id, userId)
      ? { ...user, followedByCurrentUser }
      : user
  ));

  return {
    followers: updateList(lists.followers),
    following: updateList(lists.following),
  };
}

export function resolveCurrentFollowState(explicitState, follows, currentUserId, targetUserId) {
  if (typeof explicitState === 'boolean') return explicitState;
  return follows.some((follow) => (
    sameUserId(follow.followerId, currentUserId)
    && sameUserId(follow.followingId, targetUserId)
  ));
}
