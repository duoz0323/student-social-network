import { useCallback, useEffect, useState } from 'react';
import { ArrowLeft, CircleX, EyeOff } from 'lucide-react';
import { useNavigate, useParams } from 'react-router-dom';
import { adminApi } from '../../../api/index.js';
import Button from '../../../components/common/Button.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import { formatDateTime } from '../../../utils/formatters.js';
import HideReportedPostDialog from '../components/HideReportedPostDialog.jsx';
import AdminReportedPostCard from '../components/AdminReportedPostCard.jsx';
import { getAdminReportDetailStatusLabel, getAdminReportReasonLabel } from '../constants/adminReportLabels.js';
import { useAdminToast } from '../hooks/useAdminToast.js';
import { toAdminReportPostView, toReportPostFallback } from '../utils/adminReportPost.js';

export default function AdminReportDetailPage() {
  const { caseId } = useParams();
  const navigate = useNavigate();
  const { showToast } = useAdminToast();
  const [moderationCase, setModerationCase] = useState(null);
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [postError, setPostError] = useState('');
  const [hideDialogOpen, setHideDialogOpen] = useState(false);

  const load = useCallback(async (signal) => {
    setLoading(true);
    try {
      const detail = await adminApi.getModerationCase(caseId, signal);
      setModerationCase(detail);
      setError('');
      try {
        const postDetail = await adminApi.getPost(detail.reportedPost?.postId, signal);
        setPost(toAdminReportPostView(postDetail));
        setPostError('');
      } catch (requestError) {
        if (requestError.code !== 'ERR_CANCELED') {
          setPost(toReportPostFallback(detail));
          setPostError(requestError.message || 'Không thể tải bài viết hiện tại; đang hiển thị snapshot gần nhất.');
        }
      }
    } catch (requestError) {
      if (requestError.code !== 'ERR_CANCELED') {
        setModerationCase(null);
        setError(requestError.message || 'Không thể tải hồ sơ kiểm duyệt.');
      }
    } finally {
      if (!signal?.aborted) setLoading(false);
    }
  }, [caseId]);

  useEffect(() => {
    const controller = new AbortController();
    const timer = window.setTimeout(() => load(controller.signal), 0);
    return () => { window.clearTimeout(timer); controller.abort(); };
  }, [load]);

  async function resolveNoViolation() {
    if (submitting) return;
    setSubmitting(true);
    try {
      await adminApi.resolveCaseNoViolation(caseId);
      await Promise.all([load(), adminApi.getModerationCases({ postId: moderationCase.reportedPost.postId, page: 0, size: 1 })]);
      showToast('Đã kết luận bài viết không vi phạm.');
    } catch (requestError) {
      setError(requestError.message);
      showToast(requestError.message || 'Không thể giải quyết hồ sơ kiểm duyệt.', { type: 'error' });
    } finally {
      setSubmitting(false);
    }
  }

  async function hidePost(reasonCode) {
    setSubmitting(true);
    try {
      const result = await adminApi.resolveCaseAction(caseId, {
        action: 'HIDE_POST',
        reasonCode,
      });
      await Promise.all([load(), adminApi.getModerationCases({ postId: moderationCase.reportedPost.postId, page: 0, size: 1 })]);
      setHideDialogOpen(false);
      showToast(result.accountBlocked
        ? `Đã ẩn bài và khóa tài khoản do vi phạm lần ${result.authorViolationCount}.`
        : `Đã ẩn bài. Tác giả đã vi phạm ${result.authorViolationCount}/3 lần.`);
    } catch (requestError) {
      setError(requestError.message);
      showToast(requestError.message || 'Không thể xử lý bài viết.', { type: 'error' });
      throw requestError;
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <LoadingState />;
  if (!moderationCase) return <EmptyState title="Không tìm thấy hồ sơ kiểm duyệt" description={error || 'Hồ sơ không tồn tại.'} />;
  const isOpen = moderationCase.status === 'OPEN';

  return (
    <section className="mx-auto flex h-[calc(100vh-4rem)] min-h-0 max-w-6xl flex-col overflow-hidden lg:h-[calc(100vh-6rem)]">
      <div className="shrink-0">
        <Button variant="ghost" onClick={() => navigate('/admin/reports')}><ArrowLeft size={16} /> Quay lại</Button>
      </div>
      {error && <p className="my-4 rounded-xl bg-red-50 p-3 text-red-700">{error}</p>}
      <div className="mt-4 flex min-h-0 flex-1 flex-col overflow-hidden rounded-[22px] border border-zinc-200 bg-white p-5 shadow-sm sm:p-6">
        <div className="flex shrink-0 flex-wrap items-start justify-between gap-3">
          <div><h1 className="text-2xl font-bold">Hồ sơ kiểm duyệt #{moderationCase.caseId}</h1><p className="mt-1 text-sm text-zinc-500">{moderationCase.reportCount} báo cáo · {moderationCase.distinctReporterCount} người báo cáo</p></div>
          <span className="rounded-full bg-zinc-100 px-3 py-1 text-xs font-bold">{getAdminReportDetailStatusLabel(moderationCase.status)}</span>
        </div>

        <div className="mt-5 grid min-h-0 flex-1 gap-8 overflow-y-auto [scrollbar-width:none] lg:grid-cols-[minmax(0,1.1fr)_minmax(320px,.9fr)] [&::-webkit-scrollbar]:hidden">
          <div>
            {postError && <p className="mb-3 rounded-lg bg-amber-50 p-3 text-sm text-amber-800">{postError}</p>}
            {post ? <AdminReportedPostCard post={post} /> : <EmptyState title="Không có nội dung bài viết" />}
            <div className="mt-6">
              <h2 className="font-bold">Thống kê lý do</h2>
              <div className="mt-2 flex flex-wrap gap-2">{moderationCase.reasons.map((item) => <span key={item.reason} className="rounded-full bg-violet-50 px-3 py-1 text-sm text-violet-700">{getAdminReportReasonLabel(item.reason)} ({item.count})</span>)}</div>
            </div>
          </div>

          <div>
            <h2 className="font-bold">Các báo cáo trong hồ sơ</h2>
            <div className="mt-3 space-y-2">
              {moderationCase.reports.map((report) => (
                <article key={report.reportId} className="rounded-xl border border-zinc-200 px-3 py-2.5">
                  <div className="flex flex-wrap items-center justify-between gap-x-3 gap-y-1">
                    <strong className="text-sm">{report.reporter?.displayName || `User #${report.reporter?.userId}`}</strong>
                    <span className="text-xs text-zinc-400">{formatDateTime(report.createdAt)}</span>
                  </div>
                  <div className="mt-1 flex flex-wrap items-baseline gap-x-2 gap-y-1 text-sm">
                    <span className="font-medium text-violet-700">{getAdminReportReasonLabel(report.reason)}</span>
                    <span className="text-zinc-500">{report.description || 'Không có mô tả.'}</span>
                  </div>
                </article>
              ))}
            </div>
          </div>
        </div>

        <div className="mt-4 shrink-0 border-t pt-4">
          {moderationCase.resolution?.resolvedBy && <p className="text-sm text-zinc-500">Xử lý bởi {moderationCase.resolution.resolvedBy.displayName || `Admin #${moderationCase.resolution.resolvedBy.adminId}`} lúc {formatDateTime(moderationCase.resolution.resolvedAt)}</p>}
          {isOpen && <div className="flex flex-wrap justify-end gap-3">
            <Button variant="secondary" disabled={submitting} onClick={resolveNoViolation}><CircleX size={16} /> Không vi phạm</Button>
            <Button disabled={submitting} onClick={() => setHideDialogOpen(true)}><EyeOff size={16} /> Có vi phạm / Ẩn bài</Button>
          </div>}
        </div>
      </div>
      {hideDialogOpen && <HideReportedPostDialog onClose={() => setHideDialogOpen(false)} onConfirm={hidePost} />}
    </section>
  );
}
