import { useEffect, useState } from 'react';
import { Search, ShieldAlert, ShieldCheck } from 'lucide-react';
import { adminApi } from '../../../api/index.js';
import Button from '../../../components/common/Button.jsx';
import DataTable from '../../../components/common/DataTable.jsx';
import { LoadingState } from '../../../components/common/StateBlock.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';

export default function AdminUsersPage() {
  const { currentUserId } = useApp();
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [result, setResult] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function load(signal) {
    setLoading(true);
    try {
      setResult(await adminApi.getUsers({ keyword: query.trim(), page: page - 1, size: pageSize }, signal));
      setError('');
    } catch (requestError) {
      if (requestError.code !== 'ERR_CANCELED') setError(requestError.message);
    } finally { setLoading(false); }
  }

  useEffect(() => {
    const controller = new AbortController();
    const timer = setTimeout(() => {
      adminApi.getUsers({ keyword: query.trim(), page: page - 1, size: pageSize }, controller.signal)
        .then((response) => { setResult(response); setError(''); })
        .catch((requestError) => { if (requestError.code !== 'ERR_CANCELED') setError(requestError.message); })
        .finally(() => setLoading(false));
    }, 250);
    return () => { clearTimeout(timer); controller.abort(); };
  }, [query, page, pageSize]);

  async function changeStatus(user) {
    try {
      if (user.status === 'ACTIVE') await adminApi.blockUser(user.userId, 'OTHER');
      else await adminApi.unblockUser(user.userId);
      await load();
    } catch (requestError) { setError(requestError.message); }
  }

  return (
    <section>
      <div className="mb-6 flex items-center gap-3 rounded-xl border bg-white p-3">
        <Search size={16} />
        <input value={query} onChange={(event) => { setQuery(event.target.value); setPage(1); }} placeholder="Tìm theo tên hoặc email..." className="flex-1 bg-transparent outline-none" />
        <span className="text-sm text-gray-500">Tổng: {result.totalElements}</span>
      </div>
      {error && <p className="mb-4 rounded-xl bg-red-50 p-3 text-red-700">{error}</p>}
      {loading ? <LoadingState /> : (
        <DataTable rows={result.content} pagination={{ currentPage: page, totalPages: result.totalPages, onPageChange: setPage,
          totalItems: result.totalElements, pageSize, onPageSizeChange: (size) => { setPageSize(size); setPage(1); } }} columns={[
          { key: 'displayName', label: 'Người dùng', render: (row) => <div><strong>{row.displayName}</strong><small className="block text-gray-500">{row.email || row.phoneNumber}</small></div> },
          { key: 'profileCompleted', label: 'Hồ sơ', render: (row) => row.profileCompleted ? 'Đã hoàn tất' : 'Chưa hoàn tất' },
          { key: 'status', label: 'Trạng thái' },
          { key: 'action', label: '', render: (row) => <Button size="sm" variant="secondary" disabled={String(row.userId) === String(currentUserId)} onClick={() => changeStatus(row)}>
            {row.status === 'ACTIVE' ? <><ShieldAlert size={14} /> Khóa</> : <><ShieldCheck size={14} /> Mở khóa</>}
          </Button> },
        ]} />
      )}
    </section>
  );
}
