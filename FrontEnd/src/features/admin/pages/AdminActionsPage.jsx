import { useEffect, useState } from 'react';
import { adminApi } from '../../../api/index.js';
import DataTable from '../../../components/common/DataTable.jsx';
import { LoadingState } from '../../../components/common/StateBlock.jsx';
import { formatDateTime } from '../../../utils/formatters.js';

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
  return <section><div className="mb-6 flex items-center gap-3"><h1 className="mr-auto text-2xl font-bold">Lịch sử quản trị</h1>
    <select value={actionType} onChange={(event) => { setActionType(event.target.value); setPage(1); }} className="rounded-lg border p-2">
      <option value="">Tất cả thao tác</option>{['BLOCK_USER','UNBLOCK_USER','UPDATE_USER_PROFILE','CREATE_HASHTAG','UPDATE_HASHTAG','DELETE_HASHTAG','CREATE_ACADEMIC_DATA','UPDATE_ACADEMIC_DATA','CHANGE_ACADEMIC_STATUS','HIDE_POST','RESTORE_POST','RESOLVE_REPORT','REJECT_REPORT','RESOLVE_MODERATION_CASE','REJECT_MODERATION_CASE','RESOLVE_PROFILE_REPORT','REJECT_PROFILE_REPORT'].map((item) => <option key={item}>{item}</option>)}
    </select></div>
    {error && <p className="mb-4 rounded-xl bg-red-50 p-3 text-red-700">{error}</p>}
    {loading ? <LoadingState /> : <DataTable rows={result.content} pagination={{ currentPage: page, totalPages: result.totalPages, onPageChange: setPage,
      totalItems: result.totalElements, pageSize, onPageSizeChange: (size) => { setPageSize(size); setPage(1); } }} columns={[
      { key: 'actionLabel', label: 'Thao tác', sortType: 'text' }, { key: 'admin', label: 'Quản trị viên', sortType: 'text', sortValue: (row) => row.admin?.displayName, render: (row) => row.admin?.displayName },
      { key: 'target', label: 'Đối tượng', sortType: 'text', sortValue: (row) => row.target?.displayText, render: (row) => row.target?.displayText },
      { key: 'note', label: 'Ghi chú', sortType: 'text' },
      { key: 'createdAt', label: 'Thời gian', sortType: 'date', render: (row) => formatDateTime(row.createdAt) },
    ]} />}
  </section>;
}
