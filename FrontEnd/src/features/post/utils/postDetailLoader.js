/**
 * Post là dữ liệu bắt buộc; lỗi tải bình luận không được làm mất toàn bộ trang chi tiết.
 */
export async function loadPostDetailData(postRequest, commentsRequest) {
  const [postResult, commentsResult] = await Promise.allSettled([postRequest, commentsRequest]);

  if (postResult.status === 'rejected') throw postResult.reason;

  return {
    post: postResult.value,
    comments: commentsResult.status === 'fulfilled'
      ? (commentsResult.value?.content ?? [])
      : [],
    commentsError: commentsResult.status === 'rejected' ? commentsResult.reason : null,
  };
}
