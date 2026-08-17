import { useEffect, useState } from 'react';
import { History } from 'lucide-react';
import { adminApi } from '../../../api/index.js';
import DataTable from '../../../components/common/DataTable.jsx';
import { LoadingState } from '../../../components/common/StateBlock.jsx';
import { formatDateTime } from '../../../utils/formatters.js';
import { ADMIN_ACTION_OPTIONS, getAdminActionLabel } from '../constants/adminActionLabels.js';
import AdminPageHeader from '../components/AdminPageHeader.jsx';

export default function AdminActionsPage() {
  const [actionType, setActionType] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [result, setResult] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  useEffect(() => {
    const controller = new AbortController();
    adminApi.getActions({ actionType, page: page - 1, size: pageSize }, controller.signal)
      .then((response) => { setResult(response); setError(''); })
      .catch((requestError) => { if (requestError.code !== 'ERR_CANCELED') setError(requestError.message); })
      .finally(() => setLoading(false));
    return () => controller.abort();
  }, [actionType, page, pageSize]);
  return <section className="space-y-5"><AdminPageHeader
    icon={History}
    title="Lịch sử quản trị"
    description="Theo dõi các thao tác quản trị đã thực hiện trên hệ thống."
    actions={<select value={actionType} onChange={(event) => { setActionType(event.target.value); setPage(1); }} className="h-11 rounded-xl border border-zinc-300 bg-white px-4 text-sm font-medium text-zinc-800 outline-none transition focus:border-zinc-900 focus:ring-2 focus:ring-zinc-100">
      <option value="">Tất cả thao tác</option>
      {ADMIN_ACTION_OPTIONS.map((option) => (
        <option key={option.value} value={option.value}>{option.label}</option>
      ))}
    </select>}
  />
    {error && <p className="rounded-xl bg-red-50 p-3 text-red-700">{error}</p>}
    {loading ? <LoadingState /> : <DataTable rows={result.content} pagination={{ currentPage: page, totalPages: result.totalPages, onPageChange: setPage,
      totalItems: result.totalElements, pageSize, onPageSizeChange: (size) => { setPageSize(size); setPage(1); } }} columns={[
      { key: 'actionLabel', label: 'Thao tác', sortType: 'text', render: (row) => getAdminActionLabel(row.actionType, row.actionLabel) }, { key: 'admin', label: 'Quản trị viên', sortType: 'text', sortValue: (row) => row.admin?.displayName, render: (row) => row.admin?.displayName },
      { key: 'target', label: 'Đối tượng', sortType: 'text', sortValue: (row) => row.target?.displayText, render: (row) => row.target?.displayText },
      { key: 'note', label: 'Ghi chú', sortType: 'text' },
      { key: 'createdAt', label: 'Thời gian', sortType: 'date', render: (row) => formatDateTime(row.createdAt) },
    ]} />}
  </section>;
}
