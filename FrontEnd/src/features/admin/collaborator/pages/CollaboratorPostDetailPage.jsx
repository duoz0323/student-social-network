import { useEffect, useState } from 'react';
import { ArrowLeft, Clock3, Hash, MapPin, MessageCircle, Pencil, Repeat2, ThumbsUp, Trash2 } from 'lucide-react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Avatar from '../../../../components/common/Avatar.jsx';
import Button from '../../../../components/common/Button.jsx';
import Modal from '../../../../components/common/Modal.jsx';
import { EmptyState, LoadingState } from '../../../../components/common/StateBlock.jsx';
import { formatDateTime, formatNumber } from '../../../../utils/formatters.js';
import { useAuth } from '../../../auth/hooks/useAuth.js';
import EditPostMedia from '../../../post/components/EditPostMedia.jsx';
import PostMediaGrid from '../../../post/components/PostMediaGrid.jsx';
import LocationPicker from '../../../post/locations/LocationPicker.jsx';
import SelectedLocation from '../../../post/locations/SelectedLocation.jsx';
import { googleMapsLocationUrl } from '../../../post/locations/locationUtils.js';
import { resolveLocationUpdate } from '../../../post/locations/locationMultipart.js';
import { formatPostEditCountdown, postEditRemainingSeconds } from '../../../post/utils/postEditWindow.js';
import { ADMIN_PERMISSIONS } from '../../constants/adminRbac.js';
import CollaboratorCommentDiscussion from '../components/CollaboratorCommentDiscussion.jsx';
import { collaboratorApi } from '../services/collaboratorApi.js';

const STATUS_META = Object.freeze({
  PUBLISHED: { label: 'Đang hiển thị', className: 'bg-emerald-50 text-emerald-700 ring-emerald-200' },
  HIDDEN: { label: 'Đã bị ẩn', className: 'bg-amber-50 text-amber-700 ring-amber-200' },
  DELETED: { label: 'Đã xóa', className: 'bg-zinc-100 text-zinc-600 ring-zinc-200' },
});

export default function CollaboratorPostDetailPage() {
  const { postId } = useParams();
  const navigate = useNavigate();
  const auth = useAuth();
  const [state, setState] = useState({ data: null, error: '' });
  const [edit, setEdit] = useState(null);
  const [editError, setEditError] = useState('');
  const [saving, setSaving] = useState(false);
  const [locationPickerOpen, setLocationPickerOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');
  const [clockMs, setClockMs] = useState(() => Date.now());

  useEffect(() => {
    const controller = new AbortController();
    collaboratorApi.getPost(postId, controller.signal)
      .then((data) => setState({ data, error: '' }))
      .catch((error) => !controller.signal.aborted && setState({ data: null, error: error.message }));
    return () => controller.abort();
  }, [postId]);

  useEffect(() => {
    if (state.data?.status !== 'PUBLISHED') return undefined;
    const timer = window.setInterval(() => setClockMs(Date.now()), 1000);
    return () => window.clearInterval(timer);
  }, [state.data?.status]);

  const post = state.data;
  const remainingSeconds = post
    ? postEditRemainingSeconds(post.publishedAt ?? post.createdAt, clockMs)
    : 0;
  const hasEditPermission = auth.hasPermission(ADMIN_PERMISSIONS.COLLABORATOR_POST_UPDATE_OWN);
  const hasDeletePermission = auth.hasPermission(ADMIN_PERMISSIONS.COLLABORATOR_POST_DELETE_OWN);
  const canEdit = Boolean(post && post.status === 'PUBLISHED' && hasEditPermission
    && (remainingSeconds === null || remainingSeconds > 0));
  const canDelete = Boolean(post && post.status === 'PUBLISHED' && hasDeletePermission);

  function beginEdit() {
    if (!canEdit) return;
    const media = post.media ?? [];
    setEdit({
      content: post.content ?? '',
      hashtag: post.hashtag ?? '',
      keepMediaIds: media.map((item) => item.id),
      newMediaFiles: [],
      totalCount: media.length,
      mediaBusy: false,
      location: post.location ?? null,
    });
    setEditError('');
    setLocationPickerOpen(false);
  }

  function closeEdit() {
    if (saving) return;
    setEdit(null);
    setEditError('');
    setLocationPickerOpen(false);
  }

  async function saveEdit() {
    if (!edit || edit.mediaBusy || saving || (!edit.content.trim() && edit.totalCount === 0)) return;
    const locationUpdate = resolveLocationUpdate(post.location, edit.location);
    setSaving(true);
    setEditError('');
    try {
      const data = await collaboratorApi.updatePost(postId, {
        content: edit.content,
        hashtag: edit.hashtag,
        keepMediaIds: edit.keepMediaIds,
        newMediaFiles: edit.newMediaFiles,
        ...locationUpdate,
      });
      // Giữ trạng thái quản lý vì response update dùng DTO bài viết công khai.
      setState({ data: { ...post, ...data, status: data.status ?? post.status }, error: '' });
      setEdit(null);
      setLocationPickerOpen(false);
    } catch (error) {
      setEditError(error.message);
    } finally {
      setSaving(false);
    }
  }

  async function removePost() {
    if (!canDelete || deleting) return;
    setDeleting(true);
    setDeleteError('');
    try {
      await collaboratorApi.deletePost(postId);
      navigate('/admin/collaborator/posts', { replace: true });
    } catch (error) {
      setDeleteError(error.message);
      setDeleting(false);
    }
  }

  if (!post && !state.error) return <LoadingState />;
  if (!post) return <EmptyState title="Không thể tải bài viết" description={state.error} />;

  const statusMeta = STATUS_META[post.status]
    ?? { label: post.status, className: 'bg-zinc-100 text-zinc-600 ring-zinc-200' };
  const edited = post.isEdited ?? post.edited;

  return (
    <section className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <Link to="/admin/collaborator/posts" className="mb-1.5 inline-flex items-center gap-2 text-sm font-semibold text-zinc-500 transition hover:text-zinc-950">
            <ArrowLeft size={17} /> Quay lại nội dung của tôi
          </Link>
          <div className="flex flex-wrap items-center gap-2.5">
            <h1 className="text-2xl font-bold tracking-tight">Chi tiết bài viết</h1>
            <span className={`rounded-full px-2.5 py-1 text-xs font-bold ring-1 ring-inset ${statusMeta.className}`}>{statusMeta.label}</span>
          </div>
        </div>
        <div className="flex flex-wrap gap-2">
          {canEdit ? <Button size="sm" variant="secondary" onClick={beginEdit}><Pencil size={16} /> Sửa nội dung</Button> : null}
          {canDelete ? <Button size="sm" variant="dangerSoft" onClick={() => { setDeleteError(''); setDeleteOpen(true); }}><Trash2 size={16} /> Xóa bài</Button> : null}
        </div>
      </div>

      {state.error ? <p className="rounded-xl border border-red-200 bg-red-50 px-4 py-2.5 text-sm text-red-700">{state.error}</p> : null}
      {post.status === 'PUBLISHED' && hasEditPermission && !canEdit ? (
        <p className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-2.5 text-sm text-amber-800">Đã hết thời hạn chỉnh sửa 15 phút của bài viết.</p>
      ) : null}
      {post.status !== 'PUBLISHED' ? (
        <p className="rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2.5 text-sm text-zinc-600">Bài viết ở trạng thái {statusMeta.label.toLowerCase()} nên không thể sửa hoặc xóa thêm.</p>
      ) : null}

      <div className="grid gap-4 xl:grid-cols-[minmax(0,1.65fr)_minmax(280px,0.75fr)]">
        <article className="min-w-0 rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm">
          <div className="flex items-center gap-3 border-b border-zinc-100 pb-3">
            <Avatar src={post.author?.avatarUrl} name={post.author?.displayName} size="md" viewable />
            <div className="min-w-0">
              <p className="truncate font-bold text-zinc-950">{post.author?.displayName || 'Danh tính được quản lý'}</p>
              <p className="mt-0.5 text-xs text-zinc-500">Đăng lúc {formatDateTime(post.publishedAt ?? post.createdAt)}{edited ? ' · Đã chỉnh sửa' : ''}</p>
            </div>
          </div>

          <div className="py-3">
            {post.content ? <p className="whitespace-pre-wrap break-words text-sm leading-6 text-zinc-900">{post.content}</p> : <p className="text-sm italic text-zinc-500">Bài viết không có nội dung văn bản.</p>}
            {post.hashtag ? <div className="mt-2 inline-flex items-center gap-1.5 rounded-full bg-indigo-50 px-2.5 py-1 text-xs font-semibold text-indigo-700"><Hash size={14} />{post.hashtag}</div> : null}
            {post.location ? (
              <a href={googleMapsLocationUrl(post.location)} target="_blank" rel="noreferrer" className="mt-2 flex items-start gap-2.5 rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2 transition hover:border-zinc-400 hover:bg-zinc-100">
                <MapPin size={16} className="mt-0.5 shrink-0 text-zinc-700" />
                <span><span className="block text-sm font-semibold text-zinc-900">{post.location.displayName}</span>{post.location.formattedAddress ? <span className="mt-0.5 block text-xs text-zinc-500">{post.location.formattedAddress}</span> : null}</span>
              </a>
            ) : null}
            <PostMediaGrid post={post} compact />
          </div>

          <div className="grid grid-cols-3 gap-2 border-t border-zinc-100 pt-3">
            <Metric icon={ThumbsUp} label="Lượt thích" value={post.likeCount} />
            <Metric icon={MessageCircle} label="Bình luận" value={post.commentCount} />
            <Metric icon={Repeat2} label="Đăng lại" value={post.repostCount} />
          </div>
        </article>

        <aside className="h-fit rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm">
          <h2 className="font-bold text-zinc-950">Thông tin bài viết</h2>
          <dl className="mt-2 divide-y divide-zinc-100 text-sm">
            <InfoRow label="Mã bài viết" value={`#${post.id}`} />
            <InfoRow label="Trạng thái" value={statusMeta.label} />
            <InfoRow label="Ngày xuất bản" value={formatDateTime(post.publishedAt)} />
            <InfoRow label="Ngày tạo" value={formatDateTime(post.createdAt)} />
            <InfoRow label="Cập nhật gần nhất" value={formatDateTime(post.updatedAt)} />
            {post.hiddenAt ? <InfoRow label="Thời điểm ẩn" value={formatDateTime(post.hiddenAt)} /> : null}
            {post.deletedAt ? <InfoRow label="Thời điểm xóa" value={formatDateTime(post.deletedAt)} /> : null}
            <InfoRow label="Đã chỉnh sửa" value={edited ? 'Có' : 'Không'} />
            <InfoRow label="Số media" value={String(post.media?.length ?? 0)} />
          </dl>
          {post.hiddenReason ? <div className="mt-4 rounded-xl border border-amber-200 bg-amber-50 p-3"><p className="text-xs font-bold uppercase tracking-wide text-amber-700">Lý do ẩn</p><p className="mt-1 text-sm leading-6 text-amber-900">{post.hiddenReason}</p></div> : null}
          {canEdit && remainingSeconds !== null ? <div className="mt-4 flex items-center gap-2 rounded-xl bg-indigo-50 px-3 py-2.5 text-sm font-semibold text-indigo-700"><Clock3 size={16} /> Có thể sửa trong {formatPostEditCountdown(remainingSeconds)}</div> : null}
        </aside>
      </div>

      <CollaboratorCommentDiscussion
        key={`${post.id}:${post.status}`}
        postId={post.id}
        commentCount={post.commentCount}
        postStatus={post.status}
      />

      <Modal open={Boolean(edit)} title="Chỉnh sửa bài viết" onClose={closeEdit} size="lg"
        footer={<><Button variant="secondary" onClick={closeEdit} disabled={saving}>Hủy</Button><Button onClick={saveEdit} loading={saving} loadingLabel="Đang lưu..." disabled={!edit || edit.mediaBusy || (!edit.content.trim() && edit.totalCount === 0)}>Lưu thay đổi</Button></>}>
        {edit ? <div className="space-y-4">
          <div className="flex items-center gap-3"><Avatar src={post.author?.avatarUrl} name={post.author?.displayName} size="sm" /><p className="font-bold text-zinc-950">{post.author?.displayName}</p></div>
          <div><textarea value={edit.content} maxLength={500} rows={6} disabled={saving} onChange={(event) => setEdit((current) => ({ ...current, content: event.target.value }))} className="w-full resize-none rounded-xl border border-zinc-300 p-3 text-sm outline-none transition focus:border-zinc-900 focus:ring-2 focus:ring-zinc-100" placeholder="Nội dung bài viết" /><p className="mt-1 text-right text-xs text-zinc-500">{edit.content.length}/500</p></div>
          <div><label htmlFor="collaborator-edit-hashtag" className="mb-1.5 block text-sm font-semibold text-zinc-800">Hashtag</label><input id="collaborator-edit-hashtag" value={edit.hashtag} maxLength={100} disabled={saving} onChange={(event) => setEdit((current) => ({ ...current, hashtag: event.target.value }))} className="w-full rounded-xl border border-zinc-300 p-3 text-sm outline-none transition focus:border-zinc-900 focus:ring-2 focus:ring-zinc-100" placeholder="Một hashtag, không cần nhập ký tự #" /></div>
          <EditPostMedia media={post.media ?? []} disabled={saving} onBusyChange={(mediaBusy) => setEdit((current) => current && ({ ...current, mediaBusy }))} onChange={(selection) => setEdit((current) => current && ({ ...current, ...selection }))} />
          <div><p className="text-sm font-semibold text-zinc-800">Địa điểm</p><SelectedLocation location={edit.location} onRemove={() => !saving && setEdit((current) => ({ ...current, location: null }))} /><button type="button" disabled={saving} onClick={() => setLocationPickerOpen((open) => !open)} className="mt-2 text-sm font-semibold text-zinc-700 hover:text-zinc-950 disabled:opacity-50">{edit.location ? 'Thay đổi địa điểm' : 'Gắn địa điểm'}</button>{locationPickerOpen ? <LocationPicker onSelect={(location) => setEdit((current) => ({ ...current, location }))} onClose={() => setLocationPickerOpen(false)} /> : null}</div>
          {editError ? <p className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">{editError}</p> : null}
        </div> : null}
      </Modal>

      <Modal open={deleteOpen} title="Xóa bài viết?" size="sm" onClose={() => !deleting && setDeleteOpen(false)}
        footer={<><Button variant="secondary" disabled={deleting} onClick={() => setDeleteOpen(false)}>Hủy</Button><Button variant="danger" loading={deleting} loadingLabel="Đang xóa..." onClick={removePost}>Xóa bài viết</Button></>}>
        <p className="text-sm leading-6 text-zinc-600">Bài viết sẽ bị xóa mềm và không còn xuất hiện trên bảng tin, trang cá nhân hoặc kết quả tìm kiếm. Thao tác này không thể hoàn tác từ khu vực Cộng tác viên.</p>
        {deleteError ? <p className="mt-3 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">{deleteError}</p> : null}
      </Modal>
    </section>
  );
}

function Metric({ icon: Icon, label, value }) {
  return <div className="rounded-xl bg-zinc-50 p-2 text-center"><Icon size={15} className="mx-auto text-zinc-500" /><p className="mt-1 text-base font-bold text-zinc-950">{formatNumber(Number(value) || 0)}</p><p className="text-[11px] text-zinc-500">{label}</p></div>;
}

function InfoRow({ label, value }) {
  return <div className="flex items-start justify-between gap-4 py-2"><dt className="text-zinc-500">{label}</dt><dd className="text-right font-semibold text-zinc-900">{value || '—'}</dd></div>;
}
