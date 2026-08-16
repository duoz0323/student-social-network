import { useEffect, useRef, useState } from 'react';
import { Check, Link2, LoaderCircle, Search, Send } from 'lucide-react';
import Avatar from '../../../components/common/Avatar.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { messagingApi } from '../../messaging/services/messagingApi.js';
import { copyPostLink, FACEBOOK_SHARE_STATUS, openFacebookPostShare } from '../utils/postViewModel.js';

const PAGE_SIZE = 20;

/** Share Modal chỉ chọn một recipient và luôn xác nhận mutation bền vững qua REST. */
export default function SharePostDialog({ open, postId, onClose, onToast }) {
  const [keyword, setKeyword] = useState('');
  const [recipients, setRecipients] = useState([]);
  const [selected, setSelected] = useState(null);
  const [caption, setCaption] = useState('');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const [sendError, setSendError] = useState('');
  const requestRef = useRef(null);
  const captionLength = [...caption].length;

  useEffect(() => {
    if (!open) return undefined;
    return () => requestRef.current?.abort();
  }, [open]);

  useEffect(() => {
    if (!open) return undefined;
    const timerId = window.setTimeout(() => loadRecipients(0, false), keyword ? 300 : 0);
    return () => window.clearTimeout(timerId);
    // loadRecipients chỉ phụ thuộc state keyword hiện tại và được gọi theo debounce.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [keyword, open]);

  async function loadRecipients(nextPage, append) {
    requestRef.current?.abort();
    const controller = new AbortController();
    requestRef.current = controller;
    append ? setLoadingMore(true) : setLoading(true);
    setError('');
    try {
      const response = await messagingApi.getShareRecipients({ keyword: keyword.trim(), page: nextPage, size: PAGE_SIZE }, controller.signal);
      if (controller.signal.aborted) return;
      setRecipients((current) => append
        ? [...current, ...(response.content ?? [])].filter((item, index, list) => list.findIndex((candidate) => String(candidate.userId) === String(item.userId)) === index)
        : (response.content ?? []));
      setPage(response.page ?? nextPage);
      setHasMore(!response.last);
    } catch (requestError) {
      if (requestError.code !== 'ERR_CANCELED') setError(requestError.message || 'Không thể tải danh sách người nhận.');
    } finally {
      if (requestRef.current === controller) {
        requestRef.current = null;
        setLoading(false);
        setLoadingMore(false);
      }
    }
  }

  async function sendPost() {
    if (!selected || sending || captionLength > 2000) return;
    setSending(true);
    setSendError('');
    try {
      // Conversation chỉ được mở/reuse lúc người dùng xác nhận gửi để không tạo conversation rỗng do chọn nhầm.
      const conversation = await messagingApi.openDirectConversation(selected.userId);
      await messagingApi.sendPostShare(conversation.conversationId, {
        clientMessageId: crypto.randomUUID(),
        content: caption.trim() ? caption : null,
        sharedPostId: Number(postId),
      });
      onToast('Đã gửi bài viết');
      closeDialog();
    } catch (requestError) {
      setSendError(requestError.message || 'Không thể gửi bài viết. Vui lòng thử lại.');
    } finally {
      setSending(false);
    }
  }

  function closeDialog() {
    if (sending) return;
    requestRef.current?.abort();
    setKeyword('');
    setSelected(null);
    setCaption('');
    setRecipients([]);
    setPage(0);
    setHasMore(false);
    setError('');
    setSendError('');
    onClose();
  }

  async function copyLink() {
    try {
      await copyPostLink(postId);
      onToast('Đã sao chép liên kết');
    } catch {
      onToast('Không thể sao chép liên kết', 'error');
    }
  }

  function shareFacebook() {
    const status = openFacebookPostShare(postId);
    if (status === FACEBOOK_SHARE_STATUS.PUBLIC_URL_REQUIRED) {
      onToast('Facebook không thể chia sẻ đường dẫn local. Hãy cấu hình URL HTTPS công khai.', 'error');
    } else if (status === FACEBOOK_SHARE_STATUS.POPUP_BLOCKED) {
      onToast('Trình duyệt đang chặn cửa sổ chia sẻ Facebook.', 'error');
    }
  }

  const header = (
    <header className="grid grid-cols-[1fr_auto_1fr] items-center border-b border-[var(--app-border)] px-5 py-4">
      <button type="button" disabled={sending} onClick={closeDialog} className="justify-self-start text-sm font-semibold text-[var(--app-muted)] disabled:opacity-50">Hủy</button>
      <h2 className="text-base font-bold text-[var(--app-text)]">Gửi đến</h2>
      <span />
    </header>
  );

  return (
    <Modal open={open} customHeader={header} onClose={closeDialog} size="md"
      bodyClassName="min-h-0 flex-1 overflow-y-auto px-5 py-4"
      footerClassName="!block"
      footer={selected ? (
        <div className="w-full">
          {sendError ? <p role="alert" className="mb-2 text-sm text-red-500">{sendError}</p> : null}
          <div className="flex items-end gap-2 rounded-2xl bg-[var(--app-surface-soft)] p-2 pl-4">
            <label className="min-w-0 flex-1">
              <span className="sr-only">Soạn tin nhắn kèm bài viết</span>
              <textarea value={caption} disabled={sending} rows="1" onChange={(event) => setCaption(event.target.value)}
                placeholder="Soạn tin nhắn..." className="max-h-28 min-h-10 w-full resize-none bg-transparent py-2 text-sm outline-none" />
            </label>
            <button type="button" onClick={sendPost} disabled={sending || captionLength > 2000}
              aria-label="Gửi bài viết" className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-[var(--app-text)] text-[var(--app-bg)] disabled:opacity-50">
              {sending ? <LoaderCircle className="animate-spin" size={18} /> : <Send size={18} />}
            </button>
          </div>
          <p className={`mt-1 text-right text-[11px] ${captionLength > 2000 ? 'text-red-500' : 'text-[var(--app-muted)]'}`}>{captionLength}/2000</p>
        </div>
      ) : (
        <div className="grid w-full grid-cols-2 gap-3">
          <button type="button" onClick={copyLink} className="flex min-h-20 flex-col items-center justify-center gap-2 rounded-2xl bg-[var(--app-surface-soft)] text-sm font-semibold">
            <Link2 size={22} /><span>Sao chép liên kết</span>
          </button>
          <button type="button" onClick={shareFacebook} className="flex min-h-20 flex-col items-center justify-center gap-2 rounded-2xl bg-[var(--app-surface-soft)] text-sm font-semibold">
            <span aria-hidden="true" className="grid h-[22px] w-[22px] place-items-center rounded-full bg-[#1877f2] font-bold text-white">f</span><span>Facebook · Chia sẻ</span>
          </button>
        </div>
      )}>
      <label className="flex h-11 items-center gap-3 rounded-full bg-[var(--app-surface-soft)] px-4 text-[var(--app-muted)]">
        <Search size={18} aria-hidden="true" />
        <span className="sr-only">Tìm kiếm người dùng</span>
        <input autoFocus value={keyword} disabled={sending} onChange={(event) => setKeyword(event.target.value)}
          placeholder="Tìm kiếm người dùng..." className="min-w-0 flex-1 bg-transparent text-sm text-[var(--app-text)] outline-none" />
      </label>

      {loading ? <div role="status" className="flex min-h-52 items-center justify-center gap-2 text-sm text-[var(--app-muted)]"><LoaderCircle className="animate-spin" size={18} />Đang tải người nhận...</div> : null}
      {!loading && error ? <div role="alert" className="min-h-52 py-12 text-center text-sm text-red-500"><p>{error}</p><button type="button" onClick={() => loadRecipients(0, false)} className="mt-3 font-semibold underline">Thử lại</button></div> : null}
      {!loading && !error && !recipients.length ? <p className="min-h-52 py-16 text-center text-sm text-[var(--app-muted)]">Không tìm thấy người dùng có thể nhận bài viết.</p> : null}
      {!loading && !error && recipients.length ? (
        <div className="mt-5 grid grid-cols-3 gap-x-3 gap-y-5 sm:grid-cols-4">
          {recipients.map((recipient) => {
            const active = String(selected?.userId) === String(recipient.userId);
            return (
              <button key={recipient.userId} type="button" disabled={sending} aria-pressed={active}
                onClick={() => { setSelected(active ? null : recipient); setSendError(''); }} className="min-w-0 text-center disabled:opacity-50">
                <span className="relative mx-auto block w-fit">
                  <Avatar src={recipient.avatarUrl} name={recipient.displayName} size="lg" className={active ? 'ring-2 ring-[var(--app-brand)] ring-offset-2 ring-offset-[var(--app-surface)]' : ''} />
                  {active ? <span className="absolute -bottom-1 -right-1 grid h-6 w-6 place-items-center rounded-full bg-[var(--app-brand)] text-white"><Check size={15} strokeWidth={3} /></span> : null}
                </span>
                <span className="mt-2 block truncate text-xs font-semibold">@{recipient.username}</span>
                <span className="mt-0.5 block truncate text-[11px] text-[var(--app-muted)]">{recipient.displayName}</span>
              </button>
            );
          })}
        </div>
      ) : null}
      {!loading && hasMore ? <button type="button" disabled={loadingMore} onClick={() => loadRecipients(page + 1, true)} className="mt-5 w-full rounded-xl py-2 text-sm font-semibold text-[var(--app-brand)] disabled:opacity-50">{loadingMore ? 'Đang tải...' : 'Xem thêm'}</button> : null}
    </Modal>
  );
}
