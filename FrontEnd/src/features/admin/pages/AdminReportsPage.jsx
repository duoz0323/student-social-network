import { useEffect, useState } from 'react';
import { Eye, Filter } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { adminApi } from '../../../api/index.js';
import Button from '../../../components/common/Button.jsx';
import DataTable from '../../../components/common/DataTable.jsx';
import { LoadingState } from '../../../components/common/StateBlock.jsx';
import { formatDateTime } from '../../../utils/formatters.js';

export default function AdminReportsPage() {
  const navigate = useNavigate();
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [result, setResult] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const controller = new AbortController();
    adminApi.getReports({ status, page: page - 1, size: pageSize }, controller.signal)
      .then((response) => { setResult(response); setError(''); })
      .catch((requestError) => { if (requestError.code !== 'ERR_CANCELED') setError(requestError.message); })
      .finally(() => setLoading(false));
    return () => controller.abort();
  }, [status, page, pageSize]);

  return (
    <section>
      <div className="mb-6 flex items-center gap-3 rounded-xl border bg-white p-3"><Filter size={16} />
        <select value={status} onChange={(event) => { setStatus(event.target.value); setPage(1); }} className="rounded-lg border p-2">
          <option value="">Tất cả trạng thái</option><option value="PENDING">Chờ xử lý</option><option value="RESOLVED">Đã xử lý</option><option value="REJECTED">Đã từ chối</option>
        </select>
        <span className="ml-auto text-sm text-gray-500">Tổng: {result.totalElements}</span>
      </div>
      {error && <p className="mb-4 rounded-xl bg-red-50 p-3 text-red-700">{error}</p>}
      {loading ? <LoadingState /> : <DataTable rows={result.content} pagination={{ currentPage: page, totalPages: result.totalPages, onPageChange: setPage,
        totalItems: result.totalElements, pageSize, onPageSizeChange: (size) => { setPageSize(size); setPage(1); } }} columns={[
        { key: 'reason', label: 'Lý do' },
        { key: 'reporter', label: 'Người báo cáo', render: (row) => row.reporter?.displayName },
        { key: 'post', label: 'Bài viết', render: (row) => <p className="max-w-xs truncate">{row.post?.contentPreview}</p> },
        { key: 'status', label: 'Trạng thái' },
        { key: 'createdAt', label: 'Thời gian', render: (row) => formatDateTime(row.createdAt) },
        { key: 'action', label: '', render: (row) => <Button size="sm" variant="secondary" onClick={() => navigate(`/admin/reports/${row.reportId}`)}><Eye size={14} /> Chi tiết</Button> },
      ]} />}
    </section>
  );
}
