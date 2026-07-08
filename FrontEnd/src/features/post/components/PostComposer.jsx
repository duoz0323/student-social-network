import { useState, useRef, useEffect } from 'react';
import { createPortal } from 'react-dom';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';

function ImageToolIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
      <circle cx="8.5" cy="8.5" r="1.5" />
      <polyline points="21 15 16 10 5 21" />
    </svg>
  );
}

export default function PostComposer({ mode, onClose }) {
  const { createPost, currentUser } = useApp();
  const [content, setContent] = useState('');
  const [imageUrl, setImageUrl] = useState(null);
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

  function resetForm() {
    setContent('');
    setImageUrl(null);
    setError('');
    setShowOptions(false);
  }

  function submit() {
    const result = createPost({ content, hashtags: '', imageUrls: imageUrl ? [imageUrl] : [] });
    if (!result.ok) {
      setError(result.message);
      return;
    }
    resetForm();
    onClose();
  }

  function handleClose() {
    resetForm();
    onClose();
  }

  function handleImageChange(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    
    if (!file.type.startsWith('image/')) {
       setError('Vui lòng chọn một tập tin hình ảnh.');
       return;
    }
    
    const url = URL.createObjectURL(file);
    setImageUrl(url);
    setError('');
  }

  function handleRemoveImage() {
    if (imageUrl) {
       URL.revokeObjectURL(imageUrl);
       setImageUrl(null);
    }
    if (fileInputRef.current) {
       fileInputRef.current.value = '';
    }
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
          title="Thêm ảnh"
        >
          <ImageToolIcon />
        </button>
        <input 
          type="file" 
          accept="image/*" 
          hidden 
          ref={fileInputRef} 
          onChange={handleImageChange} 
        />
      </div>
      <div className="flex items-center gap-4">
        <span className="text-sm text-[var(--app-muted)]">{content.length}/500</span>
        <Button disabled={(!content.trim() && !imageUrl) || content.length > 500} onClick={submit} className="!rounded-full px-6 py-2 min-h-[40px] font-bold bg-[var(--app-text)] text-[var(--app-surface)] hover:bg-zinc-800 disabled:opacity-30 disabled:bg-[var(--app-text)] disabled:text-[var(--app-surface)] border-none">
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
      
      <div className="flex-1 pb-1">
        <div className="flex items-center gap-1">
          <p className="text-[15px] font-bold text-[var(--app-text)]">{currentUser?.displayName}</p>
          <span className="mx-0.5 text-xs text-[var(--app-muted)]">›</span>
          <span className="text-[15px] text-[var(--app-muted)]">Cộng đồng hoặc chủ đề</span>
        </div>
        
        <textarea
          value={content}
          maxLength={500}
          onChange={(event) => setContent(event.target.value)}
          placeholder="Có gì mới?"
          className="mt-1 min-h-[100px] w-full resize-none border-none bg-transparent text-[15px] leading-relaxed outline-none placeholder:text-[var(--app-muted)]"
          autoFocus
        />
        
        {imageUrl && (
          <div className="relative mt-2 max-w-fit mb-2">
            <img src={imageUrl} alt="Preview" className="max-h-64 rounded-xl border border-[var(--app-border)] object-cover" />
            <button 
              onClick={handleRemoveImage}
              className="absolute right-2 top-2 flex h-7 w-7 items-center justify-center rounded-full bg-zinc-900/60 text-white transition hover:bg-zinc-900/80"
              title="Gỡ ảnh"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
            </button>
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
        {error && <p className="mt-3 rounded-xl bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}
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
          {error && <p className="mt-2 rounded-xl bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}
        </div>

        <div className="shrink-0 border-t border-[var(--app-border)] px-5 py-4">
          {commonFooter}
        </div>
      </div>
    </>
  );

  return createPortal(floatingPopup, document.body);
}
