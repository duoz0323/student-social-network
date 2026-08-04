import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import { Image as ImageIcon, RotateCcw, X } from 'lucide-react';
import { useMessageAttachmentAccess } from '../hooks/useMessageAttachmentAccess.js';

function AttachmentTile({ attachment, onOpen }) {
  const { accessUrl, loading, error, retry } = useMessageAttachmentAccess(attachment.attachmentId);
  if (loading) {
    return <div className="grid min-h-32 animate-pulse place-items-center bg-black/10 text-[var(--app-muted)]"><ImageIcon aria-hidden="true" size={22} /></div>;
  }
  if (error || !accessUrl) {
    return (
      <button type="button" onClick={retry} className="grid min-h-32 w-full place-items-center gap-1 bg-black/10 p-3 text-xs text-[var(--app-muted)]">
        <RotateCcw aria-hidden="true" size={18} />
        Tải lại ảnh
      </button>
    );
  }
  return (
    <button type="button" onClick={() => onOpen(accessUrl)} aria-label="Xem ảnh đính kèm"
      className="block h-full w-full cursor-zoom-in overflow-hidden bg-black/10">
      <img src={accessUrl} alt="Ảnh đính kèm trong tin nhắn" className="h-full max-h-[420px] w-full object-cover" onError={retry} />
    </button>
  );
}

function ImageLightbox({ src, onClose }) {
  useEffect(() => {
    const handleKeyDown = (event) => { if (event.key === 'Escape') onClose(); };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  return createPortal(
    <div role="dialog" aria-modal="true" aria-label="Xem ảnh tin nhắn"
      className="fixed inset-0 z-[100] flex items-center justify-center bg-black/90 p-4 sm:p-8"
      onClick={onClose}>
      <button type="button" onClick={onClose} aria-label="Đóng ảnh"
        className="absolute right-4 top-4 grid h-11 w-11 place-items-center rounded-full bg-white/10 text-white transition hover:bg-white/20">
        <X size={24} aria-hidden="true" />
      </button>
      <img src={src} alt="Ảnh tin nhắn đang xem" onClick={(event) => event.stopPropagation()}
        className="max-h-full max-w-full rounded-lg object-contain shadow-2xl" />
    </div>,
    document.body,
  );
}

/** Lưới ảnh giới hạn tối đa 5 phần tử theo contract của Messaging. */
export default function MessageAttachmentGrid({ attachments = [] }) {
  const [lightboxUrl, setLightboxUrl] = useState('');
  if (!attachments.length) return null;
  return (
    <>
      <div className={`grid overflow-hidden rounded-2xl ${attachments.length === 1 ? 'grid-cols-1' : 'grid-cols-2'} gap-0.5 bg-[var(--app-border)]`}>
        {attachments.map((attachment) => (
          <AttachmentTile key={attachment.attachmentId} attachment={attachment} onOpen={setLightboxUrl} />
        ))}
      </div>
      {lightboxUrl ? <ImageLightbox src={lightboxUrl} onClose={() => setLightboxUrl('')} /> : null}
    </>
  );
}
