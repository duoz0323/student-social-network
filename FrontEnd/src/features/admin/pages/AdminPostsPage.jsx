import { useEffect, useState } from 'react';
import { Eye, EyeOff, Search } from 'lucide-react';
import { adminApi } from '../../../api/index.js';
import Button from '../../../components/common/Button.jsx';
import DataTable from '../../../components/common/DataTable.jsx';
import { LoadingState } from '../../../components/common/StateBlock.jsx';

export default function AdminPostsPage() {
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [result, setResult] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function load(signal) {
    setLoading(true);
    try {
      setResult(await adminApi.getPosts({ keyword: query.trim(), page: page - 1, size: pageSize }, signal));
      setError('');
    } catch (requestError) { if (requestError.code !== 'ERR_CANCELED') setError(requestError.message); }
    finally { setLoading(false); }
  }

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

  async function changeStatus(post) {
    try {
      if (post.status === 'HIDDEN') await adminApi.restorePost(post.postId);
      else await adminApi.hidePost(post.postId, 'OTHER');
      await load();
    } catch (requestError) { setError(requestError.message); }
  }

  return (
    <section>
      <div className="mb-6 flex items-center gap-3 rounded-xl border bg-white p-3"><Search size={16} />
        <input value={query} onChange={(event) => { setQuery(event.target.value); setPage(1); }} placeholder="Tìm nội dung bài viết..." className="flex-1 outline-none" />
        <span className="text-sm text-gray-500">Tổng: {result.totalElements}</span>
      </div>
      {error && <p className="mb-4 rounded-xl bg-red-50 p-3 text-red-700">{error}</p>}
      {loading ? <LoadingState /> : <DataTable rows={result.content} pagination={{ currentPage: page, totalPages: result.totalPages, onPageChange: setPage,
        totalItems: result.totalElements, pageSize, onPageSizeChange: (size) => { setPageSize(size); setPage(1); } }} columns={[
        { key: 'authorDisplayName', label: 'Tác giả' },
        { key: 'contentPreview', label: 'Nội dung', className: 'max-w-sm', render: (row) => <p className="truncate">{row.contentPreview}</p> },
        { key: 'status', label: 'Trạng thái' },
        { key: 'pendingReportCount', label: 'Báo cáo chờ' },
        { key: 'action', label: '', render: (row) => <Button size="sm" variant="secondary" disabled={row.status === 'DELETED'} onClick={() => changeStatus(row)}>
          {row.status === 'HIDDEN' ? <><Eye size={14} /> Khôi phục</> : <><EyeOff size={14} /> Ẩn</>}
        </Button> },
      ]} />}
    </section>
  );
}
