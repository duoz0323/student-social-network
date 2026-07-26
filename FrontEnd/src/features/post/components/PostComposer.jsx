import { useState, useRef, useEffect } from 'react';
import { createPortal } from 'react-dom';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import { postApi } from '../../../api/index.js';

function MediaToolIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
      <circle cx="8.5" cy="8.5" r="1.5" />
      <polyline points="21 15 16 10 5 21" />
    </svg>
  );
}

function readVideoDuration(url) {
  // Trình duyệt chỉ đọc metadata để kiểm tra UX; Backend vẫn là nơi quyết định cuối cùng.
  return new Promise((resolve) => {
    const video = document.createElement('video');
    video.preload = 'metadata';
    video.onloadedmetadata = () => resolve(video.duration);
    video.onerror = () => resolve(Number.NaN);
    video.src = url;
  });
}

export default function PostComposer({ mode, onClose }) {
  const { createPost, currentUser } = useApp();
  const [content, setContent] = useState('');
  const [selectedTopic, setSelectedTopic] = useState(null);
  const [topicQuery, setTopicQuery] = useState('');
  const [topicPickerOpen, setTopicPickerOpen] = useState(false);
  const [hashtagSuggestions, setHashtagSuggestions] = useState([]);
  const [topicSearchResult, setTopicSearchResult] = useState(null);
  const [topicSearching, setTopicSearching] = useState(false);
  const [mediaPreviews, setMediaPreviews] = useState([]);
  const [error, setError] = useState('');
  const [showOptions, setShowOptions] = useState(false);
  const fileInputRef = useRef(null);
  const optionsRef = useRef(null);

  useEffect(() => {
    function handleClickOutside(event) {
      if (optionsRef.current && !optionsRef.current.contains(event.target)) {
        setShowOptions(false);
      }
    }
    if (showOptions) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [showOptions]);

  useEffect(() => {
    const keyword = topicQuery.trim();
    if (!keyword) return undefined;
    const controller = new AbortController();
    const timer = setTimeout(() => {
      setTopicSearching(true);
      postApi.suggestHashtags(keyword, controller.signal)
        .then((response) => {
          setTopicSearchResult(response);
          setHashtagSuggestions(response.suggestions ?? []);
        })
        .catch(() => {
          setTopicSearchResult(null);
          setHashtagSuggestions([]);
        })
        .finally(() => setTopicSearching(false));
    }, 250);
    return () => { clearTimeout(timer); controller.abort(); };
  }, [topicQuery]);

  function resetForm(revokePreview = true) {
    setContent('');
    setSelectedTopic(null);
    setTopicQuery('');
    setTopicPickerOpen(false);
    setHashtagSuggestions([]);
    setTopicSearchResult(null);
    setTopicSearching(false);
    if (revokePreview) {
      mediaPreviews.forEach((item) => URL.revokeObjectURL(item.url));
    }
    setMediaPreviews([]);
    setError('');
    setShowOptions(false);
  }

  async function submit() {
    const result = await createPost({
      content,
      hashtag: selectedTopic?.name ?? null,
      mediaFiles: mediaPreviews.map((item) => item.file),
    });
    if (!result.ok) {
      setError(result.message);
      return;
    }
    // Object URL đang được mock data dùng để hiển thị bài vừa tạo nên chưa thu hồi tại đây.
    resetForm();
    onClose();
  }

  function handleClose() {
    resetForm();
    onClose();
  }

  async function handleMediaChange(event) {
    const selectedFiles = Array.from(event.target.files ?? []);
    event.target.value = '';
    if (!selectedFiles.length) return;

    const selectedItems = selectedFiles.map((file) => ({
      file,
      mediaType: ['video/mp4', 'video/webm'].includes(file.type) ? 'VIDEO' : 'IMAGE',
      supported: ['image/jpeg', 'image/png', 'image/webp', 'video/mp4', 'video/webm'].includes(file.type),
    }));
    if (selectedItems.some((item) => !item.supported)) {
      setError('Chỉ hỗ trợ ảnh JPG, PNG, WEBP hoặc video MP4, WebM.');
      return;
    }
    if (mediaPreviews.length + selectedItems.length > 4) {
      setError('Mỗi bài viết chỉ được có tối đa 4 media.');
      return;
    }
    const videoCount = mediaPreviews.filter((item) => item.mediaType === 'VIDEO').length
      + selectedItems.filter((item) => item.mediaType === 'VIDEO').length;
    if (videoCount > 1) {
      setError('Mỗi bài viết chỉ được có tối đa 1 video.');
      return;
    }
    const oversizedItem = selectedItems.find((item) => {
      const maxSize = item.mediaType === 'VIDEO' ? 100 * 1024 * 1024 : 10 * 1024 * 1024;
      return item.file.size > maxSize;
    });
    if (oversizedItem) {
      setError(oversizedItem.mediaType === 'VIDEO'
        ? 'Video không được vượt quá 100 MB.'
        : 'Ảnh không được vượt quá 10 MB.');
      return;
    }

    const newPreviews = [];
    for (const item of selectedItems) {
      const url = URL.createObjectURL(item.file);
      if (item.mediaType === 'VIDEO') {
        const duration = await readVideoDuration(url);
        if (!Number.isFinite(duration) || duration <= 0 || duration > 180) {
          URL.revokeObjectURL(url);
          newPreviews.forEach((preview) => URL.revokeObjectURL(preview.url));
          setError('Video không được dài quá 3 phút.');
          return;
        }
      }
      newPreviews.push({ ...item, url });
    }
    setMediaPreviews((current) => [...current, ...newPreviews]);
    setError('');
  }

  function handleRemoveMedia(indexToRemove) {
    setMediaPreviews((current) => current.filter((item, index) => {
      if (index === indexToRemove) {
        URL.revokeObjectURL(item.url);
        return false;
      }
      return true;
    }));
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  }

  function openTopicPicker() {
    setTopicQuery(selectedTopic?.name ?? '');
    setTopicSearchResult(null);
    setHashtagSuggestions([]);
    setTopicPickerOpen(true);
  }

  function changeTopicQuery(event) {
    setTopicQuery(event.target.value);
    setTopicSearchResult(null);
    setHashtagSuggestions([]);
  }

  function selectTopic(name, isNew = false) {
    // Backend vẫn chuẩn hóa và quyết định tạo mới trong transaction của bài viết.
    setSelectedTopic({ name, isNew });
    setTopicQuery('');
    setTopicPickerOpen(false);
    setTopicSearchResult(null);
    setHashtagSuggestions([]);
  }

  function removeSelectedTopic() {
    setSelectedTopic(null);
    setTopicQuery('');
    setTopicSearchResult(null);
    setHashtagSuggestions([]);
  }

  if (!mode) return null;

  const commonHeader = (
    <header className="shrink-0 relative flex items-center justify-between border-b border-[var(--app-border)] px-5 py-4">
      <button onClick={handleClose} className="text-[15px] font-medium text-[var(--app-muted)] transition hover:text-[var(--app-text)]">Hủy</button>
      <h2 className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 text-[17px] font-bold text-[var(--app-text)]">Bài viết mới</h2>
      <button className="flex h-8 w-8 items-center justify-center rounded-full text-[var(--app-text)] transition hover:bg-[var(--app-surface-soft)]">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/></svg>
      </button>
    </header>
  );

  const commonFooter = (
    <div className="flex w-full items-center justify-between">
      <div className="flex items-center gap-1">
        <div className="relative" ref={optionsRef}>
          <button 
            onClick={() => setShowOptions(!showOptions)}
            className="flex items-center gap-1 mr-3 px-1 text-[13px] font-medium text-[var(--app-muted)] transition hover:text-[var(--app-text)]"
          >
            Lựa chọn về bài viết
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="m7 15 5 5 5-5"/><path d="m7 9 5-5 5 5"/></svg>
          </button>
          
          {showOptions && (
            <div className="absolute bottom-full left-0 mb-4 w-[280px] rounded-[1.25rem] bg-[var(--app-surface)] py-3 shadow-[0_8px_30px_rgba(0,0,0,0.12)] border border-[var(--app-border)] z-50 text-[14px]">
              <p className="mb-2 px-4 text-[13px] font-semibold text-[var(--app-muted)]">Ai có thể trả lời và trích dẫn</p>
              
              <button className="flex w-full items-center justify-between px-4 py-3 text-left hover:bg-[var(--app-surface-soft)] transition">
                <span className="font-semibold text-[var(--app-text)]">Người theo dõi bạn</span>
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
              </button>
              
              <button className="flex w-full items-center justify-between px-4 py-3 text-left hover:bg-[var(--app-surface-soft)] transition">
                <span className="font-semibold text-[var(--app-text)]">Trang cá nhân mà bạn theo dõi</span>
              </button>
        
              <button className="flex w-full items-center justify-between px-4 py-3 text-left hover:bg-[var(--app-surface-soft)] transition">
                <span className="font-semibold text-[var(--app-text)]">Trang cá nhân bạn nhắc đến</span>
              </button>
        
              <hr className="my-2 mx-4 border-[var(--app-border)]" />
        
              <button className="flex w-full items-center justify-between px-4 py-3 text-left hover:bg-[var(--app-surface-soft)] transition">
                <span className="font-semibold text-[var(--app-text)] truncate pr-2">Xem xét và phê duyệt câu tr...</span>
                <div className="relative inline-flex h-6 w-11 shrink-0 items-center rounded-full bg-[var(--app-border)] transition-colors">
                   <span className="inline-block h-5 w-5 translate-x-0.5 rounded-full bg-[var(--app-surface)] transition-transform" />
                </div>
              </button>
        
              <hr className="my-2 mx-4 border-[var(--app-border)]" />
        
              <p className="mb-1 px-4 mt-2 text-[13px] font-semibold text-[var(--app-muted)]">Đối tượng</p>
              <button className="flex w-full items-center justify-between px-4 py-3 text-left hover:bg-[var(--app-surface-soft)] transition">
                <span className="font-semibold text-[var(--app-text)] truncate pr-2">Chia sẻ lên cả...</span>
                <span className="text-[14px] text-[var(--app-muted)] flex items-center gap-1 shrink-0">
                  Tắt
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"><path d="m9 18 6-6-6-6"/></svg>
                </span>
              </button>
            </div>
          )}
        </div>

        <button 
          onClick={() => fileInputRef.current?.click()} 
          className="flex h-9 w-9 items-center justify-center rounded-full text-[var(--app-muted)] transition hover:bg-[var(--app-surface-soft)] hover:text-[var(--app-text)]" 
          title="Thêm ảnh hoặc video"
        >
          <MediaToolIcon />
        </button>
        <input 
          type="file" 
          accept="image/jpeg,image/png,image/webp,video/mp4,video/webm"
          multiple
          hidden 
          ref={fileInputRef} 
          onChange={handleMediaChange}
        />
      </div>
      <div className="flex items-center gap-4">
        <span className="text-sm text-[var(--app-muted)]">{content.length}/500</span>
        <Button disabled={(!content.trim() && mediaPreviews.length === 0) || content.length > 500} onClick={submit} className="!rounded-full px-6 py-2 min-h-[40px] font-bold bg-[var(--app-text)] text-[var(--app-surface)] hover:bg-zinc-800 disabled:opacity-30 disabled:bg-[var(--app-text)] disabled:text-[var(--app-surface)] border-none">
          Đăng
        </Button>
      </div>
    </div>
  );

  const commonBody = (
    <div className="relative flex gap-3">
      <div className="flex flex-col items-center">
        <Avatar src={currentUser?.avatarUrl} name={currentUser?.displayName} size="sm" />
      </div>
      
      <div className="relative flex-1 pb-1">
        <div className="flex items-center gap-1">
          <p className="text-[15px] font-bold text-[var(--app-text)]">{currentUser?.displayName}</p>
          <span className="mx-0.5 text-xs text-[var(--app-muted)]">›</span>
          <div className="relative min-w-0">
            {topicPickerOpen ? (
              <input
                value={topicQuery}
                onChange={changeTopicQuery}
                maxLength={100}
                placeholder="Cộng đồng hoặc chủ đề"
                className="w-[180px] max-w-[38vw] border-none bg-transparent p-0 text-[15px] font-semibold text-[var(--app-brand)] outline-none placeholder:font-normal placeholder:text-[var(--app-muted)]"
                autoFocus
              />
            ) : (
              <button
                type="button"
                onClick={openTopicPicker}
                className={`block max-w-[220px] truncate text-left text-[15px] transition hover:underline ${
                  selectedTopic ? 'font-semibold text-[var(--app-brand)]' : 'text-[var(--app-muted)]'
                }`}
              >
                {selectedTopic ? selectedTopic.name : 'Cộng đồng hoặc chủ đề'}
              </button>
            )}

            {topicPickerOpen && (
              <div className="absolute left-0 top-7 z-50 w-[330px] max-w-[calc(100vw-88px)] overflow-hidden rounded-xl border border-[var(--app-border)] bg-[var(--app-surface)] shadow-[0_16px_45px_rgba(0,0,0,0.22)]">
                <div className="max-h-[350px] overflow-y-auto">
                  {!topicQuery.trim() && (
                    <p className="px-4 py-5 text-sm text-[var(--app-muted)]">
                      Nhập tên để tìm hoặc gắn một chủ đề mới.
                    </p>
                  )}

                  {topicSearching && topicQuery.trim() && (
                    <p className="px-4 py-5 text-sm text-[var(--app-muted)]">Đang tìm chủ đề...</p>
                  )}

                  {!topicSearching && topicSearchResult?.canUseAsNewHashtag && topicSearchResult.normalizedKeyword && (
                    <button
                      type="button"
                      onClick={() => selectTopic(topicSearchResult.normalizedKeyword, true)}
                      className="block w-full border-b border-[var(--app-border)] px-4 py-3.5 text-left transition hover:bg-[var(--app-surface-soft)]"
                    >
                      <span className="block truncate text-[15px] font-semibold text-[var(--app-text)]">
                        {topicSearchResult.normalizedKeyword}
                      </span>
                      <span className="mt-0.5 block text-[13px] text-[var(--app-muted)]">+ Gắn thẻ chủ đề mới</span>
                    </button>
                  )}

                  {!topicSearching && hashtagSuggestions.map((item) => (
                    <button
                      type="button"
                      key={item.hashtagId}
                      onClick={() => selectTopic(item.name)}
                      className="block w-full border-b border-[var(--app-border)] px-4 py-3.5 text-left transition last:border-b-0 hover:bg-[var(--app-surface-soft)]"
                    >
                      <span className="flex items-center gap-2 text-[15px] font-semibold text-[var(--app-brand)]">
                        <svg width="10" height="10" viewBox="0 0 10 10" fill="currentColor" aria-hidden="true">
                          <circle cx="2" cy="5" r="2" />
                          <circle cx="6.5" cy="2" r="1.5" />
                          <circle cx="7.5" cy="7" r="1.5" />
                        </svg>
                        <span className="truncate">{item.name}</span>
                      </span>
                      <span className="mt-1 block text-[13px] text-[var(--app-muted)]">{item.postCount} bài viết</span>
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>
          {selectedTopic && (
            <button type="button" onClick={removeSelectedTopic} className="flex h-6 w-6 items-center justify-center rounded-full text-[var(--app-muted)] hover:bg-[var(--app-surface-soft)]" aria-label="Bỏ chủ đề đã chọn">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
                <path d="M18 6 6 18M6 6l12 12" />
              </svg>
            </button>
          )}
        </div>
        
        <textarea
          value={content}
          maxLength={500}
          onChange={(event) => setContent(event.target.value)}
          placeholder="Có gì mới?"
          className="mt-1 min-h-[100px] w-full resize-none border-none bg-transparent text-[15px] leading-relaxed outline-none placeholder:text-[var(--app-muted)]"
          autoFocus
        />
        {mediaPreviews.length > 0 && (
          <div className={`mt-2 mb-2 grid gap-2 ${mediaPreviews.length > 1 ? 'grid-cols-2' : 'grid-cols-1'}`}>
            {mediaPreviews.map((item, index) => (
              <div key={`${item.file.name}-${item.file.lastModified}-${index}`} className="relative overflow-hidden rounded-xl border border-[var(--app-border)] bg-black">
                {item.mediaType === 'VIDEO' ? (
                  <video src={item.url} controls preload="metadata" className="aspect-square max-h-64 w-full object-contain" />
                ) : (
                  <img src={item.url} alt={`Xem trước media ${index + 1}`} className="aspect-square max-h-64 w-full object-cover" />
                )}
                <button
                  onClick={() => handleRemoveMedia(index)}
                  className="absolute right-2 top-2 flex h-7 w-7 items-center justify-center rounded-full bg-zinc-900/60 text-white transition hover:bg-zinc-900/80"
                  title={`Gỡ media ${index + 1}`}
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );

  if (mode === 'modal') {
    return (
      <Modal
        open={true}
        onClose={handleClose}
        customHeader={commonHeader}
        footerClassName="border-t border-[var(--app-border)]"
        footer={commonFooter}
      >
        {commonBody}
        {error && <p className="app-error mt-3 rounded-xl px-3 py-2 text-sm">{error}</p>}
      </Modal>
    );
  }

  // Floating mode
  const floatingPopup = (
    <>
      <style>{`
        @keyframes popup-up { from { opacity: 0; transform: translateY(20px) scale(0.95); } to { opacity: 1; transform: translateY(0) scale(1); } }
      `}</style>
      
      <div 
        className="fixed bottom-6 right-6 lg:bottom-10 lg:right-10 z-[100] flex w-[480px] max-h-[85vh] max-w-[calc(100vw-32px)] flex-col overflow-hidden rounded-3xl bg-[var(--app-surface)] shadow-[0_20px_60px_rgba(0,0,0,0.2)] border border-[var(--app-border)]"
        style={{ animation: 'popup-up 0.25s cubic-bezier(0.16, 1, 0.3, 1) forwards' }}
      >
        {commonHeader}
        
        <div className="flex-1 overflow-y-auto px-5 py-4">
          {commonBody}
          {error && <p className="app-error mt-2 rounded-xl px-3 py-2 text-sm">{error}</p>}
        </div>

        <div className="shrink-0 border-t border-[var(--app-border)] px-5 py-4">
          {commonFooter}
        </div>
      </div>
    </>
  );

  return createPortal(floatingPopup, document.body);
}
