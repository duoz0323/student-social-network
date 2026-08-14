import { useEffect, useState } from 'react';
import { Search } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { adminApi } from '../../../api/index.js';
import DataTable from '../../../components/common/DataTable.jsx';
import { LoadingState } from '../../../components/common/StateBlock.jsx';
import AdminPostAnalytics from '../components/AdminPostAnalytics.jsx';

export default function AdminPostsPage() {
  const navigate = useNavigate();
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(100);
  const [result, setResult] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const controller = new AbortController();
    const timer = setTimeout(() => {
      adminApi.getPosts({ keyword: query.trim(), page: page - 1, size: pageSize }, controller.signal)
        .then((response) => { setResult(response); setError(''); })
        .catch((requestError) => { if (requestError.code !== 'ERR_CANCELED') setError(requestError.message); })
        .finally(() => setLoading(false));
    }, 250);
    return () => { clearTimeout(timer); controller.abort(); };
  }, [query, page, pageSize]);

  return (
    <section className="admin-page-with-analytics h-[calc(100dvh-2rem)] min-h-0 sm:h-[calc(100dvh-3rem)] lg:h-[calc(100dvh-4rem)] 2xl:h-[calc(100dvh-6rem)]">
      <div className="flex h-full min-h-0 min-w-0 flex-col">
        <div className="mb-4 flex shrink-0 items-center gap-3 rounded-xl border bg-white p-3"><Search size={16} />
          <input value={query} onChange={(event) => { setQuery(event.target.value); setPage(1); }} placeholder="Tìm nội dung bài viết..." className="flex-1 outline-none" />
          <span className="text-sm text-gray-500">Tổng: {result.totalElements}</span>
        </div>
        {error && <p className="mb-4 rounded-xl bg-red-50 p-3 text-red-700">{error}</p>}
        <div className="min-h-0 flex-1 [&>div]:h-full [&>div]:max-h-none">
          {loading ? <LoadingState /> : <DataTable rows={result.content} onRowDoubleClick={(row) => navigate(`/admin/posts/${row.postId}`)} pagination={{ currentPage: page, totalPages: result.totalPages, onPageChange: setPage,
            totalItems: result.totalElements, pageSize, onPageSizeChange: (size) => { setPageSize(size); setPage(1); } }} columns={[
            { key: 'authorDisplayName', label: 'Tác giả' },
            { key: 'contentPreview', label: 'Nội dung', className: 'max-w-sm', render: (row) => <p className="truncate">{row.contentPreview}</p> },
            { key: 'status', label: 'Trạng thái' },
            { key: 'pendingReportCount', label: 'Báo cáo chờ' },
          ]} />}
        </div>
      </div>
      <div className="admin-page-analytics h-full min-h-0 overflow-hidden">
        <AdminPostAnalytics />
      </div>
    </section>
  );
}
