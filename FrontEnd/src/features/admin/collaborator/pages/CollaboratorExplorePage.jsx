import { useCallback, useEffect, useState } from 'react';
import { Compass, Flag, Search, X } from 'lucide-react';
import Avatar from '../../../../components/common/Avatar.jsx';
import Button from '../../../../components/common/Button.jsx';
import Modal from '../../../../components/common/Modal.jsx';
import { EmptyState, LoadingState } from '../../../../components/common/StateBlock.jsx';
import { formatDateTime } from '../../../../utils/formatters.js';
import { useAdminToast } from '../../hooks/useAdminToast.js';
import { MODERATION_SUGGESTION_REASONS } from '../../moderation/moderationSuggestion.js';
import { collaboratorApi } from '../services/collaboratorApi.js';
import AdminPageHeader from '../../components/AdminPageHeader.jsx';

const INITIAL_FORM = { reason: 'INAPPROPRIATE_CONTENT', description: '' };

export default function CollaboratorExplorePage() {
  const { showToast } = useAdminToast();
  const [state, setState] = useState({ content: [], nextCursor: null, hasNext: false });
  const [searchInput, setSearchInput] = useState('');
  const [submittedQuery, setSubmittedQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState('');
  const [target, setTarget] = useState(null);
  const [form, setForm] = useState(INITIAL_FORM);
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(async (cursor = null) => {
    const append = Boolean(cursor);
    if (append) setLoadingMore(true);
    try {
      const response = submittedQuery
        ? await collaboratorApi.searchExplore({ q: submittedQuery, cursor, limit: 20 })
        : await collaboratorApi.explore({ cursor, limit: 20 });
      setState((current) => ({ ...response, content: append ? [...current.content, ...response.content] : response.content }));
      setError('');
    } catch (requestError) {
      setError(requestError.message || 'Không thể tải nội dung khám phá.');
    } finally {
      append ? setLoadingMore(false) : setLoading(false);
    }
  }, [submittedQuery]);

  useEffect(() => {
    const controller = new AbortController();
    const request = submittedQuery
      ? collaboratorApi.searchExplore({ q: submittedQuery, limit: 20 }, controller.signal)
      : collaboratorApi.explore({ limit: 20 }, controller.signal);
    request
      .then((response) => { setState(response); setError(''); })
      .catch((requestError) => { if (requestError.code !== 'ERR_CANCELED') setError(requestError.message || 'Không thể tải nội dung khám phá.'); })
      .finally(() => setLoading(false));
    return () => controller.abort();
  }, [submittedQuery]);

  function submitSearch(event) {
    event.preventDefault();
    const keyword = searchInput.trim();
    if (!keyword || keyword === submittedQuery) return;
    setLoading(true);
    setState({ content: [], nextCursor: null, hasNext: false });
    setSubmittedQuery(keyword);
  }

  function clearSearch() {
    setSearchInput('');
    if (!submittedQuery) return;
    setLoading(true);
    setState({ content: [], nextCursor: null, hasNext: false });
    setSubmittedQuery('');
  }

  async function submitSuggestion(event) {
    event.preventDefault();
    if (!target || submitting) return;
    setSubmitting(true);
    try {
      await collaboratorApi.createModerationSuggestion({ postId: target.postId, reason: form.reason, description: form.description.trim() || null });
      showToast('Đã gửi đề xuất kiểm duyệt. Bạn có thể theo dõi kết quả tại “Đề xuất của tôi”.');
      setTarget(null);
      setForm(INITIAL_FORM);
    } catch (requestError) {
      showToast(requestError.message || 'Không thể gửi đề xuất kiểm duyệt.', { type: 'error' });
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="mx-auto max-w-3xl space-y-4">
      <AdminPageHeader
        icon={Compass}
        title="Khám phá nội dung"
        description="Xem bài viết công khai và gửi đề xuất để kiểm duyệt viên kiểm tra khi phát hiện dấu hiệu vi phạm."
      />
      <form className="flex gap-2 rounded-2xl border border-[var(--app-border)] bg-[var(--app-surface)] p-2 shadow-sm" role="search" onSubmit={submitSearch}>
        <div className="relative min-w-0 flex-1">
          <Search size={18} className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-[var(--app-muted)]" aria-hidden="true" />
          <input
            type="search"
            value={searchInput}
            maxLength={100}
            onChange={(event) => setSearchInput(event.target.value)}
            placeholder="Tìm theo nội dung bài viết..."
            aria-label="Tìm nội dung bài viết"
            className="h-11 w-full rounded-xl bg-[var(--app-surface-soft)] pl-11 pr-10 text-sm outline-none transition focus:ring-2 focus:ring-[var(--app-brand)]"
          />
          {searchInput ? <button type="button" aria-label="Xóa từ khóa" onClick={clearSearch} className="absolute right-3 top-1/2 grid h-7 w-7 -translate-y-1/2 place-items-center rounded-full text-[var(--app-muted)] hover:bg-[var(--app-border)]"><X size={16} /></button> : null}
        </div>
        <Button type="submit" disabled={!searchInput.trim()}>Tìm kiếm</Button>
      </form>
      {submittedQuery && !loading ? <p className="text-sm text-[var(--app-muted)]">Kết quả cho: <strong className="text-[var(--app-text)]">“{submittedQuery}”</strong></p> : null}
      {loading ? <LoadingState /> : error && !state.content.length ? <EmptyState title="Không thể tải nội dung" description={error} actionLabel="Thử lại" onAction={() => { setLoading(true); void load(); }} /> : null}
      {state.content.map((post) => (
        <article key={post.postId} className="rounded-2xl border border-[var(--app-border)] bg-[var(--app-surface)] p-5 shadow-sm">
          <div className="flex items-start gap-3"><Avatar src={post.author?.avatarUrl} name={post.author?.displayName} /><div className="min-w-0 flex-1"><p className="font-semibold">{post.author?.displayName || 'Người dùng'}</p><p className="text-xs text-[var(--app-muted)]">{formatDateTime(post.publishedAt)}</p></div></div>
          {post.content ? <p className="mt-4 whitespace-pre-wrap text-sm leading-6">{post.content}</p> : null}
          {post.hashtag ? <p className="mt-2 text-sm font-semibold text-zinc-700">#{post.hashtag}</p> : null}
          {post.media?.[0]?.url ? <img src={post.media[0].url} alt="Ảnh bài viết" className="mt-4 max-h-96 w-full rounded-xl object-cover" /> : null}
          <div className="mt-4 flex justify-end border-t border-[var(--app-border)] pt-4"><Button size="sm" variant="secondary" onClick={() => setTarget(post)}><Flag size={15} /> Đề xuất kiểm duyệt</Button></div>
        </article>
      ))}
      {!loading && !state.content.length && !error ? <EmptyState title={submittedQuery ? 'Không tìm thấy nội dung phù hợp' : 'Chưa có nội dung để khám phá'} description={submittedQuery ? 'Hãy thử từ khóa khác hoặc xóa tìm kiếm để quay lại nội dung khám phá.' : 'Hãy quay lại sau khi hệ thống có bài viết phù hợp.'} actionLabel={submittedQuery ? 'Xóa tìm kiếm' : undefined} onAction={submittedQuery ? clearSearch : undefined} /> : null}
      {state.hasNext ? <div className="flex justify-center"><Button variant="secondary" loading={loadingMore} onClick={() => load(state.nextCursor)}>Tải thêm</Button></div> : null}
      {error && state.content.length ? <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700">{error}</p> : null}

      <Modal open={Boolean(target)} title="Đề xuất kiểm duyệt" onClose={() => !submitting && setTarget(null)} footer={<><Button variant="secondary" disabled={submitting} onClick={() => setTarget(null)}>Hủy</Button><Button type="submit" form="suggestion-form" loading={submitting}>Gửi đề xuất</Button></>}>
        <form id="suggestion-form" className="space-y-4" onSubmit={submitSuggestion}>
          <p className="rounded-xl bg-amber-50 p-3 text-sm text-amber-800">Đề xuất chỉ chuyển nội dung tới Moderator xem xét, không tự động ẩn bài viết.</p>
          <label className="block text-sm font-semibold">Lý do<select className="mt-2 w-full rounded-xl border border-[var(--app-border)] bg-transparent p-3" value={form.reason} onChange={(event) => setForm((current) => ({ ...current, reason: event.target.value }))}>{MODERATION_SUGGESTION_REASONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
          <label className="block text-sm font-semibold">Mô tả thêm <span className="font-normal text-[var(--app-muted)]">(không bắt buộc)</span><textarea maxLength={500} rows={4} className="mt-2 w-full resize-none rounded-xl border border-[var(--app-border)] bg-transparent p-3" value={form.description} onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))} /><span className="mt-1 block text-right text-xs text-[var(--app-muted)]">{form.description.length}/500</span></label>
        </form>
      </Modal>
    </section>
  );
}
