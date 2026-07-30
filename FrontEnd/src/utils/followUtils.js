export function sameUserId(left, right) {
  return left != null && right != null && String(left) === String(right);
}

export function isFollowingUser(follows, currentUserId, targetUserId) {
  return (follows ?? []).some((item) => (
    sameUserId(item.followerId, currentUserId)
    && sameUserId(item.followingId, targetUserId)
  ));
}

export function resolveFollowingState(currentFollowing, follows, currentUserId, targetUserId) {
  // Trạng thái vừa hiển thị từ API được ưu tiên hơn cache dùng chung để chọn đúng Follow hoặc Unfollow.
  return typeof currentFollowing === 'boolean'
    ? currentFollowing
    : isFollowingUser(follows, currentUserId, targetUserId);
}

export function getUnfollowTargetLabel(user) {
  // Hồ sơ công khai chỉ hiển thị displayName, không suy diễn định danh từ email hoặc userId.
  const displayName = typeof user?.displayName === 'string' ? user.displayName.trim() : '';
  return displayName || 'người dùng này';
}
