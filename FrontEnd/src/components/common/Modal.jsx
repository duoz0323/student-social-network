import { useEffect } from 'react';

export default function Modal({ open, title, children, footer, onClose, size = 'md' }) {
  useEffect(() => {
    function closeByEscape(event) {
      if (event.key === 'Escape') onClose?.();
    }
    if (open) window.addEventListener('keydown', closeByEscape);
    return () => window.removeEventListener('keydown', closeByEscape);
  }, [onClose, open]);

  if (!open) return null;

  const sizes = { sm: 'max-w-md', md: 'max-w-xl', lg: 'max-w-2xl' };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-zinc-950/40 px-4">
      <section className={`max-h-[90vh] w-full overflow-auto rounded-2xl bg-white shadow-2xl ${sizes[size]}`}>
        <header className="flex items-center justify-between border-b border-zinc-100 px-5 py-4">
          <h2 className="text-lg font-bold text-zinc-950">{title}</h2>
          <button className="rounded-full px-3 py-1 text-xl text-zinc-500 hover:bg-zinc-100" onClick={onClose} aria-label="Dong modal">
            x
          </button>
        </header>
        <div className="px-5 py-4">{children}</div>
        {footer ? <footer className="flex justify-end gap-2 border-t border-zinc-100 px-5 py-4">{footer}</footer> : null}
      </section>
    </div>
  );
}
