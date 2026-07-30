import { useCallback, useEffect, useState } from 'react';
import { ArrowLeft, CircleX, EyeOff } from 'lucide-react';
import { useNavigate, useParams } from 'react-router-dom';
import { adminApi } from '../../../api/index.js';
import Button from '../../../components/common/Button.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import { formatDateTime } from '../../../utils/formatters.js';
import HideReportedPostDialog from '../components/HideReportedPostDialog.jsx';
import AdminReportedPostCard from '../components/AdminReportedPostCard.jsx';
import {
  getAdminPostHideReasonFromReportReason,
  getAdminReportDetailStatusLabel,
  getAdminReportReasonLabel,
} from '../constants/adminReportLabels.js';
import { useAdminToast } from '../hooks/useAdminToast.js';
import { toAdminReportPostView, toReportPostFallback } from '../utils/adminReportPost.js';

export default function AdminReportDetailPage() {
  const { reportId } = useParams();
  const navigate = useNavigate();
  const { showToast } = useAdminToast();
  const [report, setReport] = useState(null);
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [postError, setPostError] = useState('');
  const [hideDialogOpen, setHideDialogOpen] = useState(false);

  const load = useCallback(async (signal) => {
    setLoading(true);
    try {
      const reportDetail = await adminApi.getReport(reportId, signal);
      setReport(reportDetail);
      setError('');

      const postId = reportDetail.reportedPost?.postId;
      if (!postId) {
        setPost(toReportPostFallback(reportDetail));
        setPostError('Báo cáo không chứa mã bài viết để tải nội dung hiện tại.');
        return;
      }

      try {
        const postDetail = await adminApi.getPost(postId, signal);
        setPost(toAdminReportPostView(postDetail));
        setPostError('');
      } catch (requestError) {
        if (requestError.code !== 'ERR_CANCELED') {
          setPost(toReportPostFallback(reportDetail));
          setPostError(requestError.message || 'Không thể tải chi tiết bài viết.');
        }
      }
    } catch (requestError) {
      if (requestError.code !== 'ERR_CANCELED') {
        setReport(null);
        setPost(null);
        setError(requestError.message || 'Không thể tải chi tiết báo cáo.');
      }
    } finally {
      setLoading(false);
    }
  }, [reportId]);

  useEffect(() => {
    const controller = new AbortController();
    const timer = window.setTimeout(() => load(controller.signal), 0);
    return () => {
      window.clearTimeout(timer);
      controller.abort();
    };
  }, [load]);

  async function reject() {
    try {
      await adminApi.rejectReport(reportId, 'Báo cáo không đủ căn cứ.');
      await load();
      showToast('Từ chối báo cáo thành công!');
    } catch (requestError) {
      setError(requestError.message);
      showToast(requestError.message || 'Không thể từ chối báo cáo.', { type: 'error' });
    }
  }

  async function hidePost() {
    const reasonLabel = getAdminReportReasonLabel(report.reason);
    const hideReasonCode = getAdminPostHideReasonFromReportReason(report.reason);
    try {
      await adminApi.resolveReport(reportId, {
        resolutionNote: `Ẩn bài vì ${reasonLabel}.`,
        hidePost: true,
        hideReasonCode,
      });
      await load();
      setHideDialogOpen(false);
      showToast('Xử lý báo cáo và ẩn bài viết thành công!');
    } catch (requestError) {
      setError(requestError.message);
      showToast(requestError.message || 'Không thể ẩn bài viết.', { type: 'error' });
      throw requestError;
    }
  }

  if (loading) return <LoadingState />;
  if (!report) return <EmptyState title="Không tìm thấy báo cáo" description={error || 'Báo cáo không tồn tại.'} />;
  const reportedPost = report.reportedPost;

  return (
    <section className="mx-auto max-w-5xl pb-8 lg:flex lg:h-[calc(100vh-6rem)] lg:min-h-0 lg:flex-col lg:overflow-hidden lg:pb-0">
      <Button className="shrink-0 self-start" variant="ghost" onClick={() => navigate('/admin/reports')}><ArrowLeft size={16} /> Quay lại</Button>
      {error && <p className="my-4 rounded-xl bg-red-50 p-3 text-red-700">{error}</p>}
      <div className="mt-5 rounded-[22px] border border-zinc-200 bg-white p-6 shadow-sm sm:p-8 lg:flex lg:min-h-0 lg:flex-1 lg:flex-col lg:overflow-hidden">
        <div className="grid gap-3 lg:grid-cols-[minmax(220px,0.78fr)_minmax(0,1.22fr)] lg:gap-8">
          <h1 className="text-xl font-bold text-zinc-950 sm:text-2xl">Chi tiết báo cáo #{report.reportId}</h1>
          <h2 className="self-center text-xs font-bold uppercase tracking-wide text-zinc-950 lg:text-right">
            {getAdminReportDetailStatusLabel(report.status)}
          </h2>
        </div>

        <div className="mt-7 grid items-start gap-8 lg:min-h-0 lg:flex-1 lg:grid-cols-[minmax(220px,0.78fr)_minmax(0,1.22fr)]">
          <div>
            <dl className="space-y-5">
              <div><dt className="text-xs text-zinc-400">Người báo cáo</dt><dd className="mt-1 text-sm font-medium text-zinc-950">{report.reporter?.displayName || '—'}</dd></div>
              <div><dt className="text-xs text-zinc-400">Thời gian</dt><dd className="mt-1 text-sm font-medium text-zinc-950">{formatDateTime(report.createdAt)}</dd></div>
              <div><dt className="text-xs text-zinc-400">Lý do</dt><dd className="mt-1 text-sm font-medium text-zinc-950">{getAdminReportReasonLabel(report.reason)}</dd></div>
              <div><dt className="text-xs text-zinc-400">Tác giả</dt><dd className="mt-1 text-sm font-medium text-zinc-950">{post?.author?.displayName || reportedPost?.author?.displayName || '—'}</dd></div>
            </dl>

            <h2 className="mt-7 text-sm font-bold text-zinc-950">Mô tả</h2>
            <p className="mt-2 rounded-lg bg-zinc-50 p-4 text-sm text-zinc-700">{report.description || 'Không có mô tả.'}</p>
          </div>

          <div className="min-w-0 lg:flex lg:min-h-0 lg:flex-col">
            <div className="rounded-lg border border-zinc-200 p-3 sm:p-4 lg:min-h-0 lg:flex-1 lg:overflow-y-auto lg:overscroll-contain">
              {postError ? <p className="mb-3 rounded-lg bg-amber-50 p-3 text-sm text-amber-800">{postError}</p> : null}
              {post ? (
                <AdminReportedPostCard post={post} />
              ) : (
                <EmptyState title="Không có nội dung bài viết" description="Không thể hiển thị bài viết của báo cáo này." />
              )}
            </div>

            {report.status === 'PENDING' ? (
              <div className="mt-6 flex flex-wrap justify-end gap-3 border-t border-zinc-200 pt-6">
                <Button className="gap-2 !h-10 !px-5" variant="secondary" onClick={reject}>
                  <CircleX size={16} /> Từ chối
                </Button>
                <Button className="gap-2 !h-10 !px-5" onClick={() => setHideDialogOpen(true)}>
                  <EyeOff size={16} /> Ẩn bài
                </Button>
              </div>
            ) : null}
          </div>
        </div>
      </div>
      {hideDialogOpen ? (
        <HideReportedPostDialog
          reasonLabel={getAdminReportReasonLabel(report.reason)}
          onClose={() => setHideDialogOpen(false)}
          onConfirm={hidePost}
        />
      ) : null}
    </section>
  );
}
