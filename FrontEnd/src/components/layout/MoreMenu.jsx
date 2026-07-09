import { useEffect, useRef } from 'react';

export default function MoreMenu({ open, onClose, onLogout }) {
  const menuRef = useRef(null);

  // Đóng menu khi click ra ngoài
  useEffect(() => {
    if (!open) return;
    function handleClickOutside(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        onClose();
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div
      ref={menuRef}
      className="absolute bottom-0 left-[calc(100%+12px)] w-max min-w-[260px] rounded-[16px] border border-[var(--app-border)] bg-[var(--app-surface)] py-2 shadow-[0_12px_40px_rgba(0,0,0,0.12)] z-50"
    >
      <button className="flex w-full items-center justify-between px-5 py-3 text-left text-[15px] font-semibold text-[var(--app-text)] transition hover:bg-[var(--app-surface-soft)]">
        Giao diện
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="m9 18 6-6-6-6"/></svg>
      </button>
      <button className="flex w-full items-center justify-between px-5 py-3 text-left text-[15px] font-semibold text-[var(--app-text)] transition hover:bg-[var(--app-surface-soft)]">
        Cài đặt
      </button>
      <div className="my-2 h-[1px] w-full bg-[var(--app-border)]"></div>
      <button className="flex w-full items-center justify-between px-5 py-3 text-left text-[15px] font-semibold text-[var(--app-text)] transition hover:bg-[var(--app-surface-soft)]">
        Đã thích
      </button>
      <button className="flex w-full items-center justify-between px-5 py-3 text-left text-[15px] font-semibold text-[var(--app-text)] transition hover:bg-[var(--app-surface-soft)]">
        Lưu trữ
      </button>
      <div className="my-2 h-[1px] w-full bg-[var(--app-border)]"></div>
      <button className="flex w-full items-center justify-between px-5 py-3 text-left text-[15px] font-semibold text-[var(--app-text)] transition hover:bg-[var(--app-surface-soft)]">
        Báo cáo sự cố
      </button>
      <button 
        className="flex w-full items-center justify-between px-5 py-3 text-left text-[15px] font-semibold text-[#ff3040] transition hover:bg-[var(--app-surface-soft)]"
        onClick={() => {
          onClose();
          onLogout();
        }}
      >
        Đăng xuất
      </button>
    </div>
  );
}
