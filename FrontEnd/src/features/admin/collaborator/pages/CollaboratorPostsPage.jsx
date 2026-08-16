import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { EmptyState, LoadingState } from '../../../../components/common/StateBlock.jsx';
import { ADMIN_PERMISSIONS } from '../../constants/adminRbac.js';
import { useAuth } from '../../../auth/hooks/useAuth.js';
import PostComposer from '../../../post/components/PostComposer.jsx';
import CollaboratorPostTable from '../components/CollaboratorPostTable.jsx';
import { collaboratorApi } from '../services/collaboratorApi.js';

export default function CollaboratorPostsPage() {
  const auth = useAuth();
  const canCreatePost = auth.hasPermission(ADMIN_PERMISSIONS.COLLABORATOR_POST_CREATE);
  const [searchParams, setSearchParams] = useSearchParams();
  const [filters, setFilters] = useState({ keyword: '', status: '', sort: 'NEWEST', page: 0, size: 20 });
  const [state, setState] = useState({ data: null, error: '' });
  const [identityState, setIdentityState] = useState({ data: null, error: '' });
  const [refreshKey, setRefreshKey] = useState(0);
  const [isComposerOpen, setIsComposerOpen] = useState(
    canCreatePost && searchParams.get('create') === '1',
  );

  useEffect(() => { const controller = new AbortController(); collaboratorApi.getPosts(filters, controller.signal)
    .then((data) => setState({ data, error: '' })).catch((error) => !controller.signal.aborted && setState({ data: null, error: error.message })); return () => controller.abort(); }, [filters, refreshKey]);

  useEffect(() => {
    if (!canCreatePost) return undefined;
    const controller = new AbortController();
    collaboratorApi.getIdentity(controller.signal)
      .then((data) => setIdentityState({ data, error: '' }))
      .catch((error) => !controller.signal.aborted && setIdentityState({ data: null, error: error.message }));
    return () => controller.abort();
  }, [canCreatePost]);

  function updateCreateQuery(shouldOpen) {
    const nextParams = new URLSearchParams(searchParams);
    if (shouldOpen) nextParams.set('create', '1');
    else nextParams.delete('create');
    setSearchParams(nextParams, { replace: true });
  }

  function openComposer() {
    setIsComposerOpen(true);
    updateCreateQuery(true);
  }

  function closeComposer() {
    setIsComposerOpen(false);
    updateCreateQuery(false);
  }

  async function submitPost(payload) {
    try {
      await collaboratorApi.createPost(payload);
      // Làm mới bảng để bài vừa đăng xuất hiện ngay sau khi modal đóng.
      setRefreshKey((value) => value + 1);
      return { ok: true };
    } catch (requestError) {
      return { ok: false, message: requestError.message };
    }
  }

  return <section className="space-y-6">
    <header className="flex flex-wrap items-end justify-between gap-4">
      <div><h1 className="text-3xl font-bold">Nội dung của tôi</h1><p className="mt-2 text-zinc-500">Quản lý bài do danh tính được liên kết đăng.</p></div>
      {canCreatePost ? <button type="button" onClick={openComposer} disabled={!identityState.data} className="rounded-xl bg-zinc-950 px-5 py-3 font-semibold text-white transition hover:bg-zinc-800 disabled:cursor-not-allowed disabled:opacity-50">Tạo bài viết</button> : null}
    </header>
    {identityState.error ? <p className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">Không thể tải danh tính đăng bài: {identityState.error}</p> : null}
    <div className="grid gap-3 md:grid-cols-3"><input placeholder="Tìm nội dung" value={filters.keyword} onChange={(e) => setFilters((v) => ({ ...v, keyword: e.target.value, page: 0 }))} className="rounded-xl border border-zinc-300 px-4 py-2.5" /><select value={filters.status} onChange={(e) => setFilters((v) => ({ ...v, status: e.target.value, page: 0 }))} className="rounded-xl border border-zinc-300 px-4"><option value="">Mọi trạng thái</option><option>PUBLISHED</option><option>HIDDEN</option><option>DELETED</option></select><select value={filters.sort} onChange={(e) => setFilters((v) => ({ ...v, sort: e.target.value, page: 0 }))} className="rounded-xl border border-zinc-300 px-4"><option value="NEWEST">Mới nhất</option><option value="OLDEST">Cũ nhất</option><option value="MOST_LIKED">Nhiều lượt thích</option><option value="MOST_COMMENTED">Nhiều bình luận</option></select></div>
    {!state.data && !state.error ? <LoadingState /> : state.error ? <EmptyState title="Không thể tải nội dung" description={state.error} /> : <CollaboratorPostTable posts={state.data.content} />}
    {isComposerOpen && identityState.data ? <PostComposer mode="modal" actor={identityState.data} submitPost={submitPost} onClose={closeComposer} /> : null}
  </section>;
}
