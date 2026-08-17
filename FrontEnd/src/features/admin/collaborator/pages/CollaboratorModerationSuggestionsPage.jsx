import { useEffect, useMemo, useState } from 'react';
import { ClipboardCheck } from 'lucide-react';
import { useSearchParams } from 'react-router-dom';
import DataTable from '../../../../components/common/DataTable.jsx';
import { LoadingState } from '../../../../components/common/StateBlock.jsx';
import { formatDateTime } from '../../../../utils/formatters.js';
import SuggestionStatusBadge from '../../moderation/SuggestionStatusBadge.jsx';
import { getSuggestionReasonLabel } from '../../moderation/moderationSuggestion.js';
import { collaboratorApi } from '../services/collaboratorApi.js';
import AdminPageHeader from '../../components/AdminPageHeader.jsx';

export default function CollaboratorModerationSuggestionsPage() {
  const [searchParams] = useSearchParams();
  const highlightedId = searchParams.get('highlight');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [result, setResult] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const controller = new AbortController();
    collaboratorApi.getOwnModerationSuggestions({ status, page: page - 1, size: pageSize }, controller.signal)
      .then((response) => { setResult(response); setError(''); })
      .catch((requestError) => { if (requestError.code !== 'ERR_CANCELED') setError(requestError.message || 'Không thể tải đề xuất.'); })
      .finally(() => setLoading(false));
    return () => controller.abort();
  }, [page, pageSize, status]);

  const columns = useMemo(() => [
    { key: 'suggestionId', label: 'Mã', render: (row) => <span className={String(row.suggestionId) === highlightedId ? 'rounded bg-zinc-200 px-2 py-1 font-bold text-zinc-900' : ''}>#{row.suggestionId}</span> },
    { key: 'postSummary', label: 'Bài viết', className: 'max-w-sm', render: (row) => <p className="line-clamp-2">{row.postSummary || `Bài viết #${row.postId}`}</p> },
    { key: 'reason', label: 'Lý do', render: (row) => getSuggestionReasonLabel(row.reason) },
    { key: 'status', label: 'Trạng thái', render: (row) => <SuggestionStatusBadge status={row.status} /> },
    { key: 'createdAt', label: 'Ngày gửi', render: (row) => formatDateTime(row.createdAt) },
    { key: 'reviewedAt', label: 'Ngày xử lý', render: (row) => row.reviewedAt ? formatDateTime(row.reviewedAt) : '—' },
  ], [highlightedId]);

  return (
    <section className="space-y-4">
      <AdminPageHeader
        icon={ClipboardCheck}
        title="Đề xuất của tôi"
        description="Theo dõi kết quả các nội dung bạn đã chuyển cho kiểm duyệt viên xem xét."
      />
      <div className="flex flex-wrap items-center gap-3 rounded-xl border border-[var(--app-border)] bg-[var(--app-surface)] p-3"><label className="text-sm font-semibold" htmlFor="suggestion-status">Trạng thái</label><select id="suggestion-status" className="rounded-lg border border-[var(--app-border)] bg-transparent px-3 py-2 text-sm" value={status} onChange={(event) => { setLoading(true); setStatus(event.target.value); setPage(1); }}><option value="">Tất cả</option><option value="PENDING">Chờ xử lý</option><option value="ACCEPTED">Đã chấp nhận</option><option value="REJECTED">Đã từ chối</option></select><span className="ml-auto text-sm text-[var(--app-muted)]">Tổng: {result.totalElements}</span></div>
      {error ? <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700">{error}</p> : null}
      {loading ? <LoadingState /> : <DataTable columns={columns} rows={result.content} emptyText="Chưa có đề xuất kiểm duyệt" pagination={{ currentPage: page, totalPages: result.totalPages, onPageChange: setPage, totalItems: result.totalElements, pageSize, onPageSizeChange: (size) => { setPageSize(size); setPage(1); } }} />}
    </section>
  );
}
