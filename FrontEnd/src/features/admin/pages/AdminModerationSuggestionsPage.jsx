import { useEffect, useState } from 'react';
import { CheckCircle2, ChevronLeft, XCircle } from 'lucide-react';
import { useNavigate, useParams } from 'react-router-dom';
import { adminApi } from '../../../api/index.js';
import Button from '../../../components/common/Button.jsx';
import DataTable from '../../../components/common/DataTable.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import { formatDateTime } from '../../../utils/formatters.js';
import { ADMIN_PERMISSIONS } from '../constants/adminRbac.js';
import { useAdminToast } from '../hooks/useAdminToast.js';
import SuggestionStatusBadge from '../moderation/SuggestionStatusBadge.jsx';
import SuggestionActor from '../moderation/SuggestionActor.jsx';
import { getSuggestionReasonLabel } from '../moderation/moderationSuggestion.js';
import { useAuth } from '../../auth/hooks/useAuth.js';

export default function AdminModerationSuggestionsPage() {
  const { suggestionId } = useParams();
  const navigate = useNavigate();
  const auth = useAuth();
  const { showToast } = useAdminToast();
  const [status, setStatus] = useState('PENDING');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [result, setResult] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const request = suggestionId
      ? adminApi.getModerationSuggestion(suggestionId)
      : adminApi.getModerationSuggestions({ status, page: page - 1, size: pageSize });
    request
      .then((response) => { if (suggestionId) setDetail(response); else setResult(response); setError(''); })
      .catch((requestError) => setError(requestError.message || 'Không thể tải chi tiết đề xuất.'))
      .finally(() => setLoading(false));
  }, [page, pageSize, status, suggestionId]);

  async function review(decision) {
    if (!detail || submitting) return;
    setSubmitting(true);
    try {
      const response = decision === 'ACCEPTED'
        ? await adminApi.acceptModerationSuggestion(detail.suggestionId)
        : await adminApi.rejectModerationSuggestion(detail.suggestionId);
      setDetail(response);
      showToast(decision === 'ACCEPTED' ? 'Đã chấp nhận đề xuất kiểm duyệt.' : 'Đã từ chối đề xuất kiểm duyệt.');
    } catch (requestError) {
      showToast(requestError.message || 'Không thể xử lý đề xuất.', { type: 'error' });
    } finally { setSubmitting(false); }
  }

  if (loading) return <LoadingState />;
  if (suggestionId) return (
    <section className="mx-auto max-w-3xl space-y-4">
      <Button variant="ghost" onClick={() => navigate('/admin/moderation-suggestions')}><ChevronLeft size={17} /> Danh sách đề xuất</Button>
      {!detail ? <EmptyState title="Không tìm thấy đề xuất" description={error || 'Đề xuất không tồn tại.'} /> : <div className="rounded-2xl border border-[var(--app-border)] bg-[var(--app-surface)] p-6 shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-3"><div><p className="text-sm text-[var(--app-muted)]">Đề xuất #{detail.suggestionId}</p><h1 className="mt-1 text-2xl font-bold">Kiểm tra đề xuất cộng tác viên</h1></div><SuggestionStatusBadge status={detail.status} /></div>
        <div className="mt-6 grid gap-4 rounded-xl border border-[var(--app-border)] p-4 sm:grid-cols-2">
          <div><p className="mb-2 text-xs font-semibold uppercase text-[var(--app-muted)]">Người đề xuất</p><SuggestionActor actor={detail.suggester} fallbackRole="COLLABORATOR" emptyText="Không xác định được người đề xuất" /></div>
          <div><p className="mb-2 text-xs font-semibold uppercase text-[var(--app-muted)]">Người xử lý</p><SuggestionActor actor={detail.reviewer} /></div>
        </div>
        <dl className="mt-6 grid gap-4 sm:grid-cols-2"><div><dt className="text-xs font-semibold uppercase text-[var(--app-muted)]">Bài viết</dt><dd className="mt-1">#{detail.postId}</dd></div><div><dt className="text-xs font-semibold uppercase text-[var(--app-muted)]">Lý do</dt><dd className="mt-1">{getSuggestionReasonLabel(detail.reason)}</dd></div><div><dt className="text-xs font-semibold uppercase text-[var(--app-muted)]">Ngày gửi</dt><dd className="mt-1">{formatDateTime(detail.createdAt)}</dd></div><div><dt className="text-xs font-semibold uppercase text-[var(--app-muted)]">Ngày xử lý</dt><dd className="mt-1">{detail.reviewedAt ? formatDateTime(detail.reviewedAt) : '—'}</dd></div></dl>
        <div className="mt-5 rounded-xl bg-[var(--app-surface-soft)] p-4"><p className="text-xs font-semibold uppercase text-[var(--app-muted)]">Nội dung bài viết</p><p className="mt-2 whitespace-pre-wrap text-sm leading-6">{detail.postSummary || 'Không có nội dung chữ.'}</p></div>
        {detail.description ? <div className="mt-4 rounded-xl border border-[var(--app-border)] p-4"><p className="text-xs font-semibold uppercase text-[var(--app-muted)]">Mô tả của cộng tác viên</p><p className="mt-2 whitespace-pre-wrap text-sm">{detail.description}</p></div> : null}
        {detail.status === 'PENDING' && auth.hasPermission(ADMIN_PERMISSIONS.MODERATION_SUGGESTION_REVIEW) ? <div className="mt-6 flex flex-wrap justify-end gap-3"><Button variant="dangerSoft" loading={submitting} onClick={() => review('REJECTED')}><XCircle size={17} /> Từ chối</Button><Button loading={submitting} onClick={() => review('ACCEPTED')}><CheckCircle2 size={17} /> Chấp nhận</Button></div> : null}
        <p className="mt-4 text-xs text-[var(--app-muted)]">Chấp nhận đề xuất ghi nhận kết quả đánh giá; không tự động ẩn bài viết. Nếu cần xử lý bài, hãy mở module Bài viết/Báo cáo theo quyền được cấp.</p>
      </div>}
    </section>
  );

  return (
    <section className="space-y-4"><header><h1 className="text-2xl font-bold">Đề xuất kiểm duyệt</h1><p className="mt-1 text-sm text-[var(--app-muted)]">Xem rõ người đề xuất, vai trò và phản hồi nội dung do cộng tác viên gửi.</p></header><div className="flex flex-wrap items-center gap-3 rounded-xl border border-[var(--app-border)] bg-[var(--app-surface)] p-3"><label className="text-sm font-semibold" htmlFor="moderation-status">Trạng thái</label><select id="moderation-status" className="rounded-lg border border-[var(--app-border)] bg-transparent px-3 py-2 text-sm" value={status} onChange={(event) => { setLoading(true); setStatus(event.target.value); setPage(1); }}><option value="">Tất cả</option><option value="PENDING">Chờ xử lý</option><option value="ACCEPTED">Đã chấp nhận</option><option value="REJECTED">Đã từ chối</option></select><span className="ml-auto text-sm text-[var(--app-muted)]">Tổng: {result.totalElements}</span></div>{error ? <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700">{error}</p> : null}<DataTable rows={result.content} emptyText="Không có đề xuất phù hợp" onRowDoubleClick={(row) => { setLoading(true); navigate(`/admin/moderation-suggestions/${row.suggestionId}`); }} pagination={{ currentPage: page, totalPages: result.totalPages, onPageChange: setPage, totalItems: result.totalElements, pageSize, onPageSizeChange: (size) => { setPageSize(size); setPage(1); } }} columns={[{ key: 'suggestionId', label: 'Mã', render: (row) => `#${row.suggestionId}` }, { key: 'suggester', label: 'Người đề xuất / Vai trò', className: 'min-w-56', render: (row) => <SuggestionActor actor={row.suggester} fallbackRole="COLLABORATOR" emptyText="Không xác định" /> }, { key: 'postSummary', label: 'Bài viết', className: 'max-w-sm', render: (row) => <p className="line-clamp-2">{row.postSummary || `Bài viết #${row.postId}`}</p> }, { key: 'reason', label: 'Lý do', render: (row) => getSuggestionReasonLabel(row.reason) }, { key: 'status', label: 'Trạng thái', render: (row) => <SuggestionStatusBadge status={row.status} /> }, { key: 'createdAt', label: 'Ngày gửi', render: (row) => formatDateTime(row.createdAt) }]} /></section>
  );
}
