import { useCallback, useEffect, useState } from 'react';
import { ArrowLeft, Eye, EyeOff, MessageCircle, ShieldAlert, ThumbsUp } from 'lucide-react';
import { useNavigate, useParams } from 'react-router-dom';
import { adminApi } from '../../../api/index.js';
import Button from '../../../components/common/Button.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import { formatDateTime } from '../../../utils/formatters.js';
import HideReportedPostDialog from '../components/HideReportedPostDialog.jsx';
import AdminReportedPostCard from '../components/AdminReportedPostCard.jsx';
import { getAdminPostHideReasonLabel } from '../constants/adminPostHideReasons.js';
import { useAdminToast } from '../hooks/useAdminToast.js';
import { toAdminReportPostView } from '../utils/adminReportPost.js';
import { useAuth } from '../../auth/hooks/useAuth.js';
import { ADMIN_PERMISSIONS } from '../constants/adminRbac.js';

const POST_STATUS_LABELS = {
  PUBLISHED: 'Đang hiển thị',
  HIDDEN: 'Đã ẩn',
  DELETED: 'Đã xóa',
};

export default function AdminPostDetailPage() {
  const { postId } = useParams();
  const navigate = useNavigate();
  const { showToast } = useAdminToast();
  const auth = useAuth();
  const [postDetail, setPostDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionSubmitting, setActionSubmitting] = useState(false);
  const [hideDialogOpen, setHideDialogOpen] = useState(false);

  const loadPost = useCallback(async (signal) => {
    setLoading(true);
    try {
      const detail = await adminApi.getPost(postId, signal);
      if (!signal.aborted) {
        setPostDetail(detail);
        setError('');
      }
    } catch (requestError) {
      if (requestError.code !== 'ERR_CANCELED') {
        setPostDetail(null);
        setError(requestError.message || 'Không thể tải chi tiết bài viết.');
      }
    } finally {
      if (!signal.aborted) setLoading(false);
    }
  }, [postId]);

  useEffect(() => {
    const controller = new AbortController();
    const timer = window.setTimeout(() => loadPost(controller.signal), 0);
    return () => {
      window.clearTimeout(timer);
      controller.abort();
    };
  }, [loadPost]);

  async function hidePost(reasonCode) {
    if (actionSubmitting) return;
    setActionSubmitting(true);
    try {
      const updatedStatus = await adminApi.hidePost(postId, reasonCode);
      // Response trạng thái là nguồn dữ liệu mới nhất sau transaction ẩn bài của Backend.
      setPostDetail((current) => ({ ...current, ...updatedStatus }));
      setError('');
      setHideDialogOpen(false);
      showToast('Ẩn bài viết thành công!');
    } catch (requestError) {
      setError(requestError.message || 'Không thể ẩn bài viết.');
      showToast(requestError.message || 'Không thể ẩn bài viết.', { type: 'error' });
      throw requestError;
    } finally {
      setActionSubmitting(false);
    }
  }

  async function restorePost() {
    if (actionSubmitting) return;
    setActionSubmitting(true);
    try {
      const updatedStatus = await adminApi.restorePost(postId);
      setPostDetail((current) => ({ ...current, ...updatedStatus }));
      setError('');
      showToast('Khôi phục bài viết thành công!');
    } catch (requestError) {
      setError(requestError.message || 'Không thể khôi phục bài viết.');
      showToast(requestError.message || 'Không thể khôi phục bài viết.', { type: 'error' });
    } finally {
      setActionSubmitting(false);
    }
  }

  if (loading) return <LoadingState />;

  if (!postDetail) {
    return (
      <section>
        <Button variant="ghost" onClick={() => navigate('/admin/posts')}><ArrowLeft size={16} /> Quay lại</Button>
        <EmptyState title="Không tìm thấy bài viết" description={error || 'Bài viết không tồn tại.'} />
      </section>
    );
  }

  // Chuẩn hóa response Admin để dùng lại thẻ hiển thị bài viết, kể cả bài đã ẩn hoặc xóa.
  const postView = toAdminReportPostView(postDetail);
  const statusLabel = POST_STATUS_LABELS[postDetail.status] || postDetail.status;

  return (
    <section className="mx-auto flex h-[calc(100vh-4rem)] min-h-0 max-w-5xl flex-col overflow-hidden lg:h-[calc(100vh-6rem)]">
      <div className="shrink-0">
        <Button variant="ghost" onClick={() => navigate('/admin/posts')}><ArrowLeft size={16} /> Quay lại danh sách</Button>
      </div>

      <div className="mt-4 flex min-h-0 flex-1 flex-col overflow-hidden rounded-[22px] border border-zinc-200 bg-white p-5 shadow-sm sm:p-6">
        <header className="flex shrink-0 flex-wrap items-start justify-between gap-3">
          <div>
            <p className="text-sm font-semibold text-zinc-500">Chi tiết bài viết</p>
            <h1 className="mt-1 text-2xl font-bold text-zinc-950">Bài viết #{postDetail.postId}</h1>
          </div>
          <div className="flex flex-wrap items-center justify-end gap-3">
            <span className="rounded-full bg-zinc-100 px-3 py-1 text-xs font-bold text-zinc-700">{statusLabel}</span>
            {postDetail.status === 'PUBLISHED' && auth.hasPermission(ADMIN_PERMISSIONS.POST_HIDE) ? (
              <Button variant="danger" disabled={actionSubmitting} onClick={() => setHideDialogOpen(true)}>
                <EyeOff size={16} /> Ẩn bài viết
              </Button>
            ) : null}
            {postDetail.status === 'HIDDEN' && auth.hasPermission(ADMIN_PERMISSIONS.POST_RESTORE) ? (
              <Button variant="secondary" disabled={actionSubmitting} onClick={restorePost}>
                <Eye size={16} /> {actionSubmitting ? 'Đang khôi phục...' : 'Khôi phục'}
              </Button>
            ) : null}
          </div>
        </header>

        {error ? <p className="mt-4 rounded-xl bg-red-50 p-3 text-red-700">{error}</p> : null}

        <div className="mt-5 grid min-h-0 flex-1 gap-6 overflow-y-auto [scrollbar-width:none] lg:grid-cols-[minmax(0,1.3fr)_minmax(280px,.7fr)] [&::-webkit-scrollbar]:hidden">
          <div>
            {postView ? <AdminReportedPostCard post={postView} /> : <EmptyState title="Bài viết không có nội dung hiển thị" />}
          </div>

          <aside className="space-y-5 rounded-2xl border border-zinc-200 bg-zinc-50 p-5">
            <section>
              <h2 className="font-bold text-zinc-950">Tác giả</h2>
              <dl className="mt-3 space-y-3">
                <DetailItem label="Tên hiển thị" value={postDetail.author?.displayName || 'Chưa cập nhật'} />
                <DetailItem label="Email" value={postDetail.author?.email || '—'} />
                <DetailItem label="Trạng thái tài khoản" value={postDetail.author?.accountStatus || '—'} />
              </dl>
            </section>

            <section className="border-t border-zinc-200 pt-5">
              <h2 className="font-bold text-zinc-950">Thống kê</h2>
              <div className="mt-3 grid grid-cols-2 gap-3">
                <Metric icon={ThumbsUp} label="Lượt thích" value={postDetail.likeCount} />
                <Metric icon={MessageCircle} label="Bình luận" value={postDetail.commentCount} />
                <Metric icon={ShieldAlert} label="Báo cáo chờ" value={postDetail.pendingReportCount} />
                <Metric icon={ShieldAlert} label="Tổng báo cáo" value={postDetail.totalReportCount} />
              </div>
            </section>

            <section className="border-t border-zinc-200 pt-5">
              <h2 className="font-bold text-zinc-950">Thông tin hệ thống</h2>
              <dl className="mt-3 space-y-3">
                <DetailItem label="Ngày đăng" value={formatDateTime(postDetail.createdAt)} />
                <DetailItem label="Cập nhật gần nhất" value={formatDateTime(postDetail.updatedAt)} />
                {postDetail.status === 'HIDDEN' ? (
                  <>
                    <DetailItem label="Ẩn lúc" value={formatDateTime(postDetail.hiddenAt)} />
                    <DetailItem label="Lý do ẩn" value={getAdminPostHideReasonLabel(postDetail.hiddenReason)} />
                    <DetailItem label="Admin xử lý" value={postDetail.hiddenBy?.displayName || '—'} />
                  </>
                ) : null}
                {postDetail.status === 'DELETED' ? <DetailItem label="Xóa lúc" value={formatDateTime(postDetail.deletedAt)} /> : null}
              </dl>
            </section>
          </aside>
        </div>
      </div>
      {hideDialogOpen ? <HideReportedPostDialog onClose={() => setHideDialogOpen(false)} onConfirm={hidePost} /> : null}
    </section>
  );
}

function DetailItem({ label, value }) {
  return (
    <div>
      <dt className="text-xs font-semibold uppercase tracking-wide text-zinc-400">{label}</dt>
      <dd className="mt-1 break-words text-sm text-zinc-700">{value}</dd>
    </div>
  );
}

function Metric({ icon: Icon, label, value }) {
  return (
    <div className="rounded-xl border border-zinc-200 bg-white p-3">
      <Icon className="text-zinc-400" size={16} aria-hidden="true" />
      <strong className="mt-2 block text-lg text-zinc-950">{Number(value) || 0}</strong>
      <span className="text-xs text-zinc-500">{label}</span>
    </div>
  );
}
