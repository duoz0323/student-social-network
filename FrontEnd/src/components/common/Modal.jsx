import { useEffect, useId, useRef } from 'react';
import { createPortal } from 'react-dom';
import { X } from 'lucide-react';

export default function Modal({ open, title, customHeader, children, footer, footerClassName, bodyClassName, className, onClose, size = 'md' }) {
  const titleId = useId();
  const dialogRef = useRef(null);
  const onCloseRef = useRef(onClose);

  // Giữ callback mới nhất mà không chạy lại hiệu ứng focus khi nội dung form thay đổi.
  useEffect(() => {
    onCloseRef.current = onClose;
  });

  useEffect(() => {
    function closeByEscape(event) {
      if (event.key === 'Escape') onCloseRef.current?.();
    }

    if (open) {
      const previousOverflow = document.body.style.overflow;
      const previousActiveElement = document.activeElement;
      window.addEventListener('keydown', closeByEscape);
      // Modal khóa nền và chuyển focus vào hộp thoại để thao tác bàn phím rõ ràng.
      document.body.style.overflow = 'hidden';
      requestAnimationFrame(() => dialogRef.current?.focus());

      return () => {
        window.removeEventListener('keydown', closeByEscape);
        document.body.style.overflow = previousOverflow;
        previousActiveElement?.focus?.();
      };
    }

    return undefined;
  }, [open]);

  if (!open) return null;

  const sizes = { sm: 'max-w-md', md: 'max-w-xl', lg: 'max-w-2xl' };

  return createPortal(
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-zinc-950/45 p-3 sm:p-5"
      onClick={onClose}
    >
      <section
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={title ? titleId : undefined}
        aria-label={!title ? 'Hộp thoại' : undefined}
        tabIndex={-1}
        className={`animate-modal-in flex max-h-[calc(100vh-24px)] w-full flex-col overflow-hidden rounded-[var(--radius-modal)] border border-[var(--app-border)] bg-[var(--app-surface)] shadow-[var(--shadow-modal)] outline-none sm:max-h-[90vh] ${sizes[size] ?? sizes.md} ${className || ''}`}
        onClick={(e) => e.stopPropagation()}
      >
        {customHeader ? customHeader : (
          <header className="flex shrink-0 items-center justify-between border-b border-[var(--app-border)] px-5 py-4">
            <h2 id={titleId} className="text-lg font-semibold text-[var(--app-text)]">{title}</h2>
            <button className="flex h-8 w-8 items-center justify-center rounded-[var(--radius-control)] text-[var(--app-muted)] outline-none transition-colors duration-[var(--motion-fast)] hover:bg-[var(--app-surface-soft)] hover:text-[var(--app-text)] focus-visible:ring-2 focus-visible:ring-[var(--app-brand)]" onClick={onClose} aria-label="Đóng modal">
              <X size={20} strokeWidth={2} aria-hidden="true" />
            </button>
          </header>
        )}
        <div className={`min-h-0 flex-1 overflow-y-auto ${bodyClassName ?? 'px-5 py-4'}`}>{children}</div>
        {footer ? <footer className={`shrink-0 flex justify-end gap-2 border-t border-[var(--app-border)] px-5 py-4 ${footerClassName || ''}`}>{footer}</footer> : null}
      </section>
    </div>,
    document.body
  );
}
