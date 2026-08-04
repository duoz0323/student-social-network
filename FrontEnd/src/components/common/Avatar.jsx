import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import { X } from 'lucide-react';

export default function Avatar({ src, name, size = 'md', className = '', viewable = false }) {
  const [viewerOpen, setViewerOpen] = useState(false);
  const sizes = { 
    sm: 'h-9 w-9 text-sm', 
    md: 'h-11 w-11 text-base', 
    lg: 'h-[72px] w-[72px] text-2xl' 
  };
  const initials = name
    ?.split(' ')
    .map((part) => part[0])
    .join('')
    .slice(0, 2)
    .toUpperCase() || '?';

  useEffect(() => {
    if (!viewerOpen) return undefined;
    const previousOverflow = document.body.style.overflow;
    const handleKeyDown = (event) => { if (event.key === 'Escape') setViewerOpen(false); };
    document.body.style.overflow = 'hidden';
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.body.style.overflow = previousOverflow;
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [viewerOpen]);

  const avatar = (
    <div className={`relative shrink-0 overflow-hidden rounded-full bg-violet-100 text-violet-700 ${sizes[size]} ${className}`}>
      {/* Fallback luôn nằm dưới ảnh để URL hỏng không tạo avatar rỗng. */}
      <span className="flex h-full w-full items-center justify-center font-bold">{initials}</span>
      {src ? (
        <img src={src} alt={name ?? ''} className="absolute inset-0 h-full w-full object-cover" onError={(event) => (event.currentTarget.style.display = 'none')} />
      ) : null}
    </div>
  );

  if (!viewable) return avatar;

  return (
    <>
      <button type="button" onClick={() => setViewerOpen(true)} aria-label={`Xem ảnh đại diện của ${name || 'người dùng'}`}
        className="shrink-0 cursor-zoom-in rounded-full focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--app-brand)]">
        {avatar}
      </button>
      {viewerOpen ? createPortal(
        <div role="dialog" aria-modal="true" aria-label={`Ảnh đại diện của ${name || 'người dùng'}`}
          className="fixed inset-0 z-[110] flex items-center justify-center bg-black/95 p-6"
          onClick={() => setViewerOpen(false)}>
          <button type="button" onClick={() => setViewerOpen(false)} aria-label="Đóng ảnh đại diện"
            className="absolute left-5 top-5 grid h-14 w-14 place-items-center rounded-full bg-black/70 text-white transition hover:bg-white/10">
            <X size={28} aria-hidden="true" />
          </button>
          {/* Avatar lớn giữ hình tròn và fallback giống giao diện hồ sơ. */}
          <div className="relative flex h-[min(68vw,360px)] w-[min(68vw,360px)] items-center justify-center overflow-hidden rounded-full bg-violet-100 text-7xl font-bold text-violet-700 shadow-2xl"
            onClick={(event) => event.stopPropagation()}>
            <span>{initials}</span>
            {src ? (
              <img src={src} alt={`Ảnh đại diện của ${name || 'người dùng'}`}
                className="absolute inset-0 h-full w-full object-cover"
                onError={(event) => (event.currentTarget.style.display = 'none')} />
            ) : null}
          </div>
        </div>,
        document.body,
      ) : null}
    </>
  );
}
