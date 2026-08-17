import { useEffect, useState } from 'react';
import { Hash, Pencil, Plus, Search, Trash2 } from 'lucide-react';
import { useSearchParams } from 'react-router-dom';
import { adminApi } from '../../../api/index.js';
import DataTable from '../../../components/common/DataTable.jsx';
import { LoadingState } from '../../../components/common/StateBlock.jsx';
import { formatDateTime } from '../../../utils/formatters.js';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { useAdminToast } from '../hooks/useAdminToast.js';
import { useAuth } from '../../auth/hooks/useAuth.js';
import { ADMIN_PERMISSIONS } from '../constants/adminRbac.js';
import AdminPageHeader from '../components/AdminPageHeader.jsx';

const EMPTY_RESULT = Object.freeze({ content: [], totalElements: 0, totalPages: 0 });

/** Màn hình chỉ đọc giúp ADMIN theo dõi mức độ sử dụng hashtag trong hệ thống. */
export default function AdminHashtagsPage() {
  const [searchParams] = useSearchParams();
  const { showToast } = useAdminToast();
  const auth = useAuth();
  // Nhận tên hashtag từ Analytics để màn quản lý mở đúng kết quả cần kiểm tra.
  const [query, setQuery] = useState(() => searchParams.get('keyword') || '');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [result, setResult] = useState(EMPTY_RESULT);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [refreshKey, setRefreshKey] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);
  const [newName, setNewName] = useState('');
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [editTarget, setEditTarget] = useState(null);
  const [editName, setEditName] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [actionError, setActionError] = useState('');

  useEffect(() => {
    const controller = new AbortController();
    const timer = setTimeout(() => {
      setLoading(true);
      adminApi.getHashtags(
        { keyword: query.trim(), page: page - 1, size: pageSize },
        controller.signal,
      )
        .then((response) => {
          setResult(response);
          setError('');
        })
        .catch((requestError) => {
          if (requestError.code !== 'ERR_CANCELED') setError(requestError.message);
        })
        .finally(() => {
          if (!controller.signal.aborted) setLoading(false);
        });
    }, 250);

    return () => {
      clearTimeout(timer);
      controller.abort();
    };
  }, [query, page, pageSize, refreshKey]);

  async function handleCreate(event) {
    event.preventDefault();
    if (submitting) return;
    setSubmitting(true);
    setActionError('');
    try {
      const created = await adminApi.createHashtag(newName);
      setCreateOpen(false);
      setNewName('');
      setQuery('');
      setPage(1);
      setRefreshKey((value) => value + 1);
      showToast(`Đã tạo #${created.name}.`);
    } catch (requestError) {
      setActionError(requestError.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget || submitting) return;
    setSubmitting(true);
    setActionError('');
    try {
      const deleted = await adminApi.deleteHashtag(deleteTarget.hashtagId);
      setDeleteTarget(null);
      if (result.content.length === 1 && page > 1) setPage((value) => value - 1);
      else setRefreshKey((value) => value + 1);
      showToast(`Đã xóa #${deleted.name} và gỡ khỏi ${deleted.detachedPostCount} bài viết.`);
    } catch (requestError) {
      setActionError(requestError.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function handleUpdate(event) {
    event.preventDefault();
    if (!editTarget || submitting) return;
    setSubmitting(true);
    setActionError('');
    try {
      const updated = await adminApi.updateHashtag(editTarget.hashtagId, editName);
      setEditTarget(null);
      setEditName('');
      setRefreshKey((value) => value + 1);
      showToast(`Đã đổi tên hashtag thành #${updated.name}.`);
    } catch (requestError) {
      setActionError(requestError.message);
    } finally {
      setSubmitting(false);
    }
  }

  const columns = [
    {
      key: 'name',
      label: 'Tên hashtag',
      sortType: 'text',
      render: (row) => <span className="font-semibold text-zinc-950">#{row.name}</span>,
    },
    {
      key: 'postCount',
      label: 'Số bài viết',
      sortType: 'number',
      render: (row) => Number(row.postCount || 0).toLocaleString('vi-VN'),
    },
    { key: 'createdAt', label: 'Ngày tạo', sortType: 'date', render: (row) => formatDateTime(row.createdAt) },
    {
      key: 'latestUsedAt',
      label: 'Ngày sử dụng mới nhất',
      sortType: 'date',
      render: (row) => row.latestUsedAt ? formatDateTime(row.latestUsedAt) : 'Chưa sử dụng',
    },
    {
      key: 'actions',
      label: 'Thao tác',
      sortable: false,
      className: 'w-28 text-right',
      render: (row) => (
        <div className="flex justify-end gap-1">
          {auth.hasAdminRole('SUPER_ADMIN') && <button
            type="button"
            onClick={() => {
              setActionError('');
              setEditTarget(row);
              setEditName(row.name);
            }}
            aria-label={`Sửa hashtag ${row.name}`}
            title="Sửa tên hashtag"
            className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-zinc-600 transition hover:bg-zinc-100 focus:outline-none focus:ring-2 focus:ring-zinc-300"
          >
            <Pencil size={17} />
          </button>}
          {auth.hasPermission(ADMIN_PERMISSIONS.HASHTAG_DELETE) && <button
            type="button"
            onClick={() => {
              setActionError('');
              setDeleteTarget(row);
            }}
            aria-label={`Xóa hashtag ${row.name}`}
            title="Xóa hashtag"
            className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-red-600 transition hover:bg-red-50 focus:outline-none focus:ring-2 focus:ring-red-300"
          >
            <Trash2 size={17} />
          </button>}
        </div>
      ),
    },
  ];

  return (
    <section className="flex h-[calc(100vh-4rem)] min-h-0 flex-col lg:h-[calc(100vh-6rem)]">
      <AdminPageHeader
        className="mb-5"
        icon={Hash}
        title="Quản lý hashtag"
        description="Tạo, theo dõi và gỡ hashtag khỏi hệ thống."
        actions={auth.hasAdminRole('SUPER_ADMIN') ? (
          <Button onClick={() => { setActionError(''); setCreateOpen(true); }}>
            <Plus size={17} /> Tạo hashtag
          </Button>
        ) : null}
      />

      <div className="mb-4 flex shrink-0 items-center gap-3 rounded-xl border border-zinc-200 bg-white p-3">
        <Search size={17} className="text-zinc-500" />
        <input
          value={query}
          onChange={(event) => {
            setQuery(event.target.value);
            setPage(1);
          }}
          maxLength={100}
          placeholder="Tìm theo tên hashtag..."
          aria-label="Tìm hashtag"
          className="min-w-0 flex-1 bg-transparent outline-none"
        />
        <span className="whitespace-nowrap text-sm text-zinc-500">Tổng: {result.totalElements}</span>
      </div>

      {error ? <p className="mb-4 shrink-0 rounded-xl bg-red-50 p-3 text-sm text-red-700">{error}</p> : null}
      <div className="min-h-0 flex-1 [&>div]:h-full [&>div]:max-h-none">
        {loading ? <LoadingState /> : (
          <DataTable
            rows={result.content}
            emptyText="Không có hashtag phù hợp"
            columns={columns}
            pagination={{
              currentPage: page,
              totalPages: result.totalPages,
              onPageChange: setPage,
              totalItems: result.totalElements,
              pageSize,
              onPageSizeChange: (size) => {
                setPageSize(size);
                setPage(1);
              },
            }}
          />
        )}
      </div>

      <Modal
        open={createOpen}
        title="Tạo hashtag"
        size="sm"
        onClose={() => { if (!submitting) { setCreateOpen(false); setActionError(''); } }}
        footer={(
          <>
            <Button variant="secondary" disabled={submitting} onClick={() => { setCreateOpen(false); setActionError(''); }}>Hủy</Button>
            <Button type="submit" form="admin-create-hashtag-form" loading={submitting}>Tạo hashtag</Button>
          </>
        )}
      >
        <form id="admin-create-hashtag-form" onSubmit={handleCreate}>
          <label htmlFor="admin-hashtag-name" className="mb-2 block text-sm font-semibold text-zinc-800">Tên hashtag</label>
          <div className="flex items-center rounded-xl border border-zinc-300 px-3 focus-within:border-zinc-500 focus-within:ring-2 focus-within:ring-zinc-200">
            <Hash size={17} className="shrink-0 text-zinc-500" />
            <input
              id="admin-hashtag-name"
              value={newName}
              onChange={(event) => setNewName(event.target.value)}
              maxLength={100}
              autoFocus
              placeholder="Ví dụ: sinh viên"
              className="min-w-0 flex-1 bg-transparent px-2 py-3 outline-none"
            />
          </div>
          <p className="mt-2 text-xs text-zinc-500">Dấu # sẽ được tự loại bỏ; tên được chuẩn hóa về chữ thường.</p>
          {actionError ? <p className="mt-3 rounded-lg bg-red-50 p-3 text-sm text-red-700">{actionError}</p> : null}
        </form>
      </Modal>

      <Modal
        open={Boolean(editTarget)}
        title="Sửa tên hashtag"
        size="sm"
        onClose={() => { if (!submitting) { setEditTarget(null); setEditName(''); setActionError(''); } }}
        footer={(
          <>
            <Button variant="secondary" disabled={submitting} onClick={() => { setEditTarget(null); setEditName(''); setActionError(''); }}>Hủy</Button>
            <Button type="submit" form="admin-update-hashtag-form" loading={submitting} loadingLabel="Đang lưu...">Lưu thay đổi</Button>
          </>
        )}
      >
        <form id="admin-update-hashtag-form" onSubmit={handleUpdate}>
          <label htmlFor="admin-update-hashtag-name" className="mb-2 block text-sm font-semibold text-zinc-800">Tên hashtag mới</label>
          <div className="flex items-center rounded-xl border border-zinc-300 px-3 focus-within:border-zinc-500 focus-within:ring-2 focus-within:ring-zinc-200">
            <Hash size={17} className="shrink-0 text-zinc-500" />
            <input
              id="admin-update-hashtag-name"
              value={editName}
              onChange={(event) => setEditName(event.target.value)}
              maxLength={100}
              autoFocus
              className="min-w-0 flex-1 bg-transparent px-2 py-3 outline-none"
            />
          </div>
          <p className="mt-2 text-xs text-zinc-500">Các bài viết đang dùng hashtag này vẫn được giữ nguyên liên kết sau khi đổi tên.</p>
          {actionError ? <p className="mt-3 rounded-lg bg-red-50 p-3 text-sm text-red-700">{actionError}</p> : null}
        </form>
      </Modal>

      <Modal
        open={Boolean(deleteTarget)}
        title="Xóa hashtag"
        size="sm"
        onClose={() => { if (!submitting) { setDeleteTarget(null); setActionError(''); } }}
        footer={(
          <>
            <Button variant="secondary" disabled={submitting} onClick={() => { setDeleteTarget(null); setActionError(''); }}>Hủy</Button>
            <Button variant="danger" loading={submitting} loadingLabel="Đang xóa..." onClick={handleDelete}>Xóa hashtag</Button>
          </>
        )}
      >
        <p className="text-sm leading-6 text-zinc-700">
          Bạn có chắc muốn xóa <strong className="text-zinc-950">#{deleteTarget?.name}</strong>?
          Hashtag sẽ được gỡ khỏi <strong>{Number(deleteTarget?.postCount || 0).toLocaleString('vi-VN')} bài viết</strong>;
          các bài viết vẫn được giữ lại.
        </p>
        {actionError ? <p className="mt-3 rounded-lg bg-red-50 p-3 text-sm text-red-700">{actionError}</p> : null}
      </Modal>
    </section>
  );
}
