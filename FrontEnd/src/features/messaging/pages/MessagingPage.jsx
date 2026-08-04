import { useEffect, useMemo, useRef, useState } from 'react';
import { ArrowLeft, ImagePlus, Search, Send, SquarePen, X } from 'lucide-react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import Avatar from '../../../components/common/Avatar.jsx';
import { useMessaging } from '../hooks/useMessaging.js';
import ConversationList from '../components/ConversationList.jsx';
import MessageThread from '../components/MessageThread.jsx';
import { validateMessageImages } from '../utils/messageImages.js';
import { preservedScrollTop } from '../utils/messagingState.js';
import { createTypingComposerController, typingKey } from '../utils/typingState.js';

export default function MessagingPage() {
  const { conversationId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const messaging = useMessaging();
  const { clearAccessRevoked, loadConversation, markRead, sendTyping, socketConnected } = messaging;
  const [content, setContent] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [inboxFilter, setInboxFilter] = useState('ALL');
  const [selectedImages, setSelectedImages] = useState([]);
  const [attachmentError, setAttachmentError] = useState('');
  const [sendingImages, setSendingImages] = useState(false);
  const selectedImagesRef = useRef([]);
  const imageInputRef = useRef(null);
  const threadRef = useRef(null);
  const markTimerRef = useRef(null);
  const typingController = useMemo(() => createTypingComposerController({
    isConnected: () => socketConnected,
    sendFrame: (activeId, typing) => sendTyping(activeId, typing),
  }), [sendTyping, socketConnected]);
  const activeConversation = useMemo(() => messaging.conversations.find((item) => String(item.conversationId) === String(conversationId)), [conversationId, messaging.conversations]);
  const activeOtherUser = activeConversation?.otherUser ?? location.state?.otherUser;
  const otherUserId = activeOtherUser?.userId;
  const otherReadMarker = messaging.readMarkers[String(otherUserId)] ?? 0;
  const otherUserTyping = Boolean(messaging.typingUsers[typingKey(conversationId, otherUserId)]);
  const filteredConversations = useMemo(() => {
    const keyword = searchQuery.trim().toLocaleLowerCase('vi-VN');
    return messaging.conversations.filter((conversation) => {
      if (inboxFilter === 'UNREAD' && Number(conversation.unreadCount) <= 0) return false;
      if (!keyword) return true;
      return [conversation.otherUser?.displayName, conversation.lastMessage?.contentPreview]
        .some((value) => value?.toLocaleLowerCase('vi-VN').includes(keyword));
    });
  }, [inboxFilter, messaging.conversations, searchQuery]);

  useEffect(() => () => typingController.stop(), [conversationId, typingController]);
  useEffect(() => { if (!socketConnected) typingController.disconnected(); }, [socketConnected, typingController]);
  useEffect(() => () => typingController.dispose(), [typingController]);
  useEffect(() => { if (conversationId) loadConversation(conversationId); }, [conversationId, loadConversation]);
  useEffect(() => { selectedImagesRef.current = selectedImages; }, [selectedImages]);
  useEffect(() => () => selectedImagesRef.current.forEach((item) => URL.revokeObjectURL(item.previewUrl)), []);

  useEffect(() => {
    if (messaging.accessRevokedConversationId && String(messaging.accessRevokedConversationId) === String(conversationId)) {
      clearAccessRevoked();
      navigate('/messages', { replace: true });
    }
  }, [clearAccessRevoked, conversationId, messaging.accessRevokedConversationId, navigate]);

  useEffect(() => {
    const incoming = [...messaging.messages].reverse().find((item) => item.messageId && String(item.senderId) !== String(messaging.currentUserId));
    if (!incoming || !conversationId) return undefined;
    markTimerRef.current = window.setTimeout(() => {
      const element = threadRef.current;
      const nearBottom = element && element.scrollHeight - element.scrollTop - element.clientHeight < 160;
      if (nearBottom && document.visibilityState === 'visible') markRead(conversationId, incoming.messageId).catch(() => {});
    }, 250);
    return () => window.clearTimeout(markTimerRef.current);
  }, [conversationId, markRead, messaging.messages, messaging.currentUserId]);

  useEffect(() => {
    const element = threadRef.current;
    if (element && !messaging.loadingMoreMessages) element.scrollTop = element.scrollHeight;
  }, [conversationId, messaging.loadingMessages, messaging.loadingMoreMessages]);

  useEffect(() => {
    const element = threadRef.current;
    if (element && element.scrollHeight - element.scrollTop - element.clientHeight < 240) element.scrollTop = element.scrollHeight;
  }, [messaging.messages.length]);

  const handleScroll = async () => {
    const element = threadRef.current;
    if (!element || element.scrollTop > 40 || messaging.loadingMoreMessages || !messaging.hasMoreMessages) return;
    const previousHeight = element.scrollHeight;
    const previousTop = element.scrollTop;
    const loaded = await messaging.loadOlderMessages();
    if (loaded) requestAnimationFrame(() => { element.scrollTop = preservedScrollTop({ previousHeight, nextHeight: element.scrollHeight, previousTop }); });
  };

  const chooseImages = (event) => {
    const incoming = Array.from(event.target.files ?? []);
    const result = validateMessageImages(selectedImages.map((item) => item.file), incoming);
    setAttachmentError(result.error);
    if (!result.error) {
      setSelectedImages((current) => [...current, ...incoming.map((file) => ({
        id: `${file.name}-${file.lastModified}-${crypto.randomUUID()}`,
        file,
        previewUrl: URL.createObjectURL(file),
      }))]);
    }
    event.target.value = '';
  };

  const removeImage = (id) => {
    setSelectedImages((current) => {
      const removed = current.find((item) => item.id === id);
      if (removed) URL.revokeObjectURL(removed.previewUrl);
      return current.filter((item) => item.id !== id);
    });
    setAttachmentError('');
  };

  const submit = async (event) => {
    event.preventDefault();
    const value = content;
    if ((!value.trim() && !selectedImages.length) || [...value].length > 2000 || !conversationId || sendingImages) return;
    typingController.stop();
    if (selectedImages.length) {
      setSendingImages(true);
      try {
        await messaging.sendImageMessage(conversationId, value, selectedImages.map((item) => item.file));
        selectedImages.forEach((item) => URL.revokeObjectURL(item.previewUrl));
        setSelectedImages([]);
        setContent('');
        setAttachmentError('');
      } catch (error) {
        setAttachmentError(error.message || 'Không thể gửi ảnh. Vui lòng thử lại.');
      } finally {
        setSendingImages(false);
      }
      return;
    }
    setContent('');
    try { await messaging.sendMessage(conversationId, value); } catch { /* Failed item vẫn được giữ để retry. */ }
  };

  return (
    <section className="h-[calc(100dvh-var(--header-height)-3.5rem)] w-full max-w-[1480px] overflow-hidden border-x border-[var(--app-border)] bg-[var(--app-surface)] lg:h-screen">
      <div className="grid h-full lg:grid-cols-[420px_minmax(0,1fr)]">
        <aside className={`${conversationId ? 'hidden lg:flex' : 'flex'} h-full min-h-0 flex-col border-r border-[var(--app-border)]`}>
          <div className="px-5 pb-4 pt-6">
            <div className="flex items-center justify-between">
              <h1 className="text-2xl font-bold tracking-tight">Tin nhắn</h1>
              <Link to="/search" aria-label="Tìm người để nhắn tin" className="grid h-10 w-10 place-items-center rounded-full transition hover:bg-[var(--app-surface-soft)]"><SquarePen size={21} /></Link>
            </div>
            <label className="mt-6 flex h-12 items-center gap-3 rounded-full bg-[var(--app-surface-soft)] px-4 text-[var(--app-muted)]">
              <Search size={19} aria-hidden="true" />
              <span className="sr-only">Tìm trong tin nhắn</span>
              <input value={searchQuery} onChange={(event) => setSearchQuery(event.target.value)} placeholder="Tìm kiếm" className="min-w-0 flex-1 bg-transparent text-sm text-[var(--app-text)] outline-none placeholder:text-[var(--app-muted)]" />
              {searchQuery ? <button type="button" onClick={() => setSearchQuery('')} aria-label="Xóa tìm kiếm"><X size={17} /></button> : null}
            </label>
            <div className="mt-4 flex gap-2">
              {[['ALL', 'Tất cả'], ['UNREAD', 'Chưa đọc']].map(([value, label]) => <button key={value} type="button" onClick={() => setInboxFilter(value)} className={`rounded-full border px-4 py-2 text-sm font-semibold ${inboxFilter === value ? 'border-[var(--app-border-strong)] bg-[var(--app-surface-soft)]' : 'border-[var(--app-border)] text-[var(--app-muted)]'}`}>{label}</button>)}
            </div>
          </div>
          <div className="min-h-0 flex-1 overflow-y-auto border-t border-[var(--app-border)]">
            <ConversationList conversations={filteredConversations} activeId={conversationId} loading={messaging.loadingInbox} error={messaging.error} hasMore={!searchQuery && inboxFilter === 'ALL' && messaging.hasMoreConversations} loadingMore={messaging.loadingMoreInbox} onLoadMore={messaging.loadMoreConversations} />
          </div>
        </aside>

        <div className={`${conversationId ? 'flex' : 'hidden lg:flex'} h-full min-h-0 min-w-0 flex-col overflow-hidden bg-[var(--app-bg)]`}>
          {!conversationId ? (
            <div className="grid h-full place-items-center px-6 text-center">
              <div><div className="mx-auto grid h-16 w-16 place-items-center rounded-full border border-[var(--app-border)]"><SquarePen size={26} /></div><h2 className="mt-4 text-xl font-semibold">Tin nhắn của bạn</h2><p className="mt-1 text-sm text-[var(--app-muted)]">Chọn một cuộc trò chuyện để bắt đầu.</p></div>
            </div>
          ) : (
            <>
              <header className="flex h-16 shrink-0 items-center gap-3 border-b border-[var(--app-border)] bg-[var(--app-surface)] px-5 sm:h-[72px]">
                <button type="button" className="grid h-10 w-10 place-items-center rounded-full lg:hidden" onClick={() => navigate('/messages')} aria-label="Quay lại"><ArrowLeft size={22} /></button>
                <Avatar src={activeOtherUser?.avatarUrl} name={activeOtherUser?.displayName} size="md" viewable />
                <strong className="min-w-0 truncate text-[15px]">{activeOtherUser?.displayName ?? 'Cuộc trò chuyện'}</strong>
              </header>

              <div ref={threadRef} onScroll={handleScroll} className="flex min-h-0 flex-1 flex-col gap-2 overflow-y-auto px-5 py-4 sm:px-7">
                {messaging.loadingMoreMessages ? <p className="text-center text-xs text-[var(--app-muted)]">Đang tải tin cũ...</p> : null}
                {messaging.loadingMessages ? <p className="m-auto text-sm text-[var(--app-muted)]">Đang tải lịch sử...</p> : null}
                {!messaging.loadingMessages && !messaging.messages.length ? <p className="m-auto text-sm text-[var(--app-muted)]">Hãy gửi tin nhắn đầu tiên.</p> : null}
                <MessageThread messages={messaging.messages} currentUserId={messaging.currentUserId} otherReadMarker={otherReadMarker} otherUser={activeOtherUser} onRetry={(message) => messaging.sendMessage(conversationId, message.content, message).catch(() => {})} />
              </div>

              {otherUserTyping ? (
                <div aria-live="polite" className="shrink-0 px-7 py-1 text-xs text-[var(--app-muted)]">
                  {`${activeOtherUser?.displayName ?? 'Người dùng'} đang nhập...`}
                </div>
              ) : null}
              <form onSubmit={submit} className="relative z-10 flex h-[76px] shrink-0 items-center border-t border-[var(--app-border)] bg-[var(--app-surface)] px-4 sm:px-6">
                {selectedImages.length || attachmentError ? (
                  <div className="absolute bottom-full left-0 right-0 border-t border-[var(--app-border)] bg-[var(--app-surface)] px-4 py-3 sm:px-6">
                    {selectedImages.length ? <div className="flex max-w-full gap-2 overflow-x-auto px-1 py-1">{selectedImages.map((item) => <div key={item.id} className="relative shrink-0"><img src={item.previewUrl} alt="Ảnh chờ gửi" className="h-16 w-16 rounded-xl border border-[var(--app-border)] object-cover" /><button type="button" onClick={() => removeImage(item.id)} aria-label="Bỏ ảnh" className="absolute -right-1 -top-1 grid h-5 w-5 place-items-center rounded-full bg-black text-white"><X size={12} /></button></div>)}</div> : null}
                    {attachmentError ? <p role="alert" className={`${selectedImages.length ? 'mt-2' : ''} text-xs text-red-500`}>{attachmentError}</p> : null}
                  </div>
                ) : null}
                <div className="flex w-full min-w-0 items-center gap-2">
                  <input ref={imageInputRef} type="file" accept="image/jpeg,image/png,image/webp" multiple className="hidden" onChange={chooseImages} />
                  <button type="button" onClick={() => imageInputRef.current?.click()} disabled={sendingImages || selectedImages.length >= 5} aria-label="Đính kèm ảnh" className="grid h-12 w-12 shrink-0 place-items-center rounded-full bg-[var(--app-surface-soft)] text-[var(--app-muted)] transition hover:text-[var(--app-text)] disabled:opacity-40"><ImagePlus size={22} /></button>
                  <div className="flex min-h-12 min-w-0 flex-1 items-end rounded-[24px] bg-[var(--app-surface-soft)] pl-4 pr-2">
                    <textarea value={content} onChange={(event) => { const value = event.target.value; setContent(value); typingController.update(conversationId, value); }} onBlur={() => typingController.stop()} onKeyDown={(event) => { if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); event.currentTarget.form?.requestSubmit(); } }} rows="1" maxLength="2000" aria-label="Nội dung tin nhắn" placeholder="Tin nhắn..." className="max-h-32 min-h-12 min-w-0 flex-1 resize-none bg-transparent py-3 text-sm outline-none placeholder:text-[var(--app-muted)]" />
                    <button type="submit" disabled={(!content.trim() && !selectedImages.length) || sendingImages} aria-label="Gửi tin nhắn" className="mb-1.5 grid h-9 w-9 shrink-0 place-items-center rounded-full bg-[var(--app-text)] text-[var(--app-bg)] transition disabled:bg-[#363638] disabled:text-[var(--app-muted)]"><Send size={17} /></button>
                  </div>
                </div>
              </form>
            </>
          )}
        </div>
      </div>
    </section>
  );
}
