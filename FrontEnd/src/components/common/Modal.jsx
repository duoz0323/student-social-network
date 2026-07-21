import { useEffect } from 'react';
import { createPortal } from 'react-dom';

export default function Modal({ open, title, customHeader, children, footer, footerClassName, bodyClassName, className, onClose, size = 'md' }) {
  useEffect(() => {
    function closeByEscape(event) {
      if (event.key === 'Escape') onClose?.();
    }
    
    if (open) {
      window.addEventListener('keydown', closeByEscape);
      // Khóa cuộn trang nền khi modal mở
      document.body.style.overflow = 'hidden';
    }
    
    return () => {
      window.removeEventListener('keydown', closeByEscape);
      // Khôi phục cuộn trang
      document.body.style.overflow = '';
    };
  }, [onClose, open]);

  if (!open) return null;

  const sizes = { sm: 'max-w-md', md: 'max-w-xl', lg: 'max-w-2xl' };

  return createPortal(
    <div 
      className="fixed inset-0 z-50 flex items-center justify-center bg-zinc-950/40 px-4" 
      style={{ animation: 'backdrop-in 0.2s ease-out forwards' }}
      onClick={onClose}
    >
      <style>{`
        @keyframes backdrop-in { from { opacity: 0; } to { opacity: 1; } }
      `}</style>
      <section 
        className={`animate-modal-in flex max-h-[90vh] w-full flex-col overflow-visible rounded-3xl bg-[var(--app-surface)] shadow-2xl ${sizes[size]} ${className || ''}`}
        onClick={(e) => e.stopPropagation()}
      >
        {customHeader ? customHeader : (
          <header className="flex shrink-0 items-center justify-between border-b border-[var(--app-border)] px-5 py-4">
            <h2 className="text-lg font-bold text-[var(--app-text)]">{title}</h2>
            <button className="flex h-8 w-8 items-center justify-center rounded-full text-[var(--app-muted)] transition hover:bg-[var(--app-surface-soft)]" onClick={onClose} aria-label="Dong modal">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
            </button>
          </header>
        )}
        <div className={`flex-1 overflow-y-auto ${bodyClassName ?? 'px-5 py-4'}`}>{children}</div>
        {footer ? <footer className={`shrink-0 flex justify-end gap-2 border-t border-[var(--app-border)] px-5 py-4 ${footerClassName || ''}`}>{footer}</footer> : null}
      </section>
    </div>,
    document.body
  );
}
