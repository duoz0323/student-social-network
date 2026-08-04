import { useState, useEffect } from 'react';

// ─── Icons ─────────────────────────────────────────────────────────
export function CameraIcon({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" />
      <circle cx="12" cy="13" r="4" />
    </svg>
  );
}

export function CheckIcon({ size = 13 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="20 6 9 17 4 12" />
    </svg>
  );
}

export function ArrowLeftIcon({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M19 12H5M5 12l7 7M5 12l7-7" />
    </svg>
  );
}

export function ArrowRightIcon({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M5 12h14M12 5l7 7-7 7" />
    </svg>
  );
}

export function UserIcon({ size = 18 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
      <circle cx="12" cy="7" r="4" />
    </svg>
  );
}

export function CalendarIcon({ size = 18 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
      <line x1="16" y1="2" x2="16" y2="6" />
      <line x1="8" y1="2" x2="8" y2="6" />
      <line x1="3" y1="10" x2="21" y2="10" />
    </svg>
  );
}

// ─── Background đồng bộ với giao diện Auth ────────────────────────
export function OnboardingBackground() {
  return (
    <div className="pointer-events-none absolute inset-0 overflow-hidden" aria-hidden="true">
      <div className="absolute -left-[14%] -top-[20%] h-[140%] w-[68%] rounded-[50%] bg-[#dcd0ff]/50 blur-[2px]" />
      <div className="absolute -left-[18%] -top-[16%] h-[132%] w-[64%] rounded-[50%] bg-[radial-gradient(circle_at_42%_50%,#ffffff_0%,#ffffff_38%,#eee7ff_65%,#dcd0ff_100%)] shadow-[0_0_60px_rgba(124,58,237,0.14)]" />
      <div className="absolute -left-16 bottom-[-90px] h-64 w-64 rounded-full bg-[radial-gradient(circle,#a1a1aa_1.2px,transparent_1.4px)] [background-size:14px_14px] opacity-25" />
    </div>
  );
}

// ─── Step Indicator – dạng node + line giống hệt mẫu ────────────────────
const STEP_META = [
  { label: 'Tên hiển thị' },
  { label: 'Ảnh đại diện' },
  { label: 'Thông tin cá nhân' },
];

export function StepIndicator({ current, total = 3 }) {
  return (
    <div className="mb-8 w-full">
      <div className="flex items-center justify-between">
        {STEP_META.map((m, i) => {
          const stepNum = i + 1;
          const isActive = stepNum === current;
          const isDone = stepNum < current;
          const isLast = i === total - 1;

          return <div key={i} className={`flex items-center ${isLast ? 'flex-none' : 'flex-1'}`}>
                {/* Node vòng tròn số */}
                <div className="relative flex flex-col items-center">
                  <div
                    className="relative z-10 flex h-9 w-9 items-center justify-center rounded-full text-sm font-bold transition-all duration-300 text-white"
                    style={
                      isDone
                        ? { background: 'var(--auth-step-done-bg)' }
                        : isActive
                        ? { background: 'var(--auth-step-active-bg)', boxShadow: '0 0 0 4px var(--auth-step-active-ring)' }
                        : { background: 'var(--auth-step-inactive-bg)', border: '1px solid var(--auth-step-inactive-border)', color: 'var(--auth-step-inactive-text)' }
                    }
                  >
                    {isDone ? (
                      <CheckIcon size={15} />
                    ) : (
                      <span>{stepNum}</span>
                    )}
                  </div>

                  {/* Nhãn tên bước */}
                  <div
                    className={`absolute -bottom-6 text-[12px] font-medium transition-colors duration-300 whitespace-nowrap ${isActive || isDone ? 'font-semibold' : ''}`}
                    style={{ color: isActive || isDone ? 'var(--auth-accent-text)' : 'var(--auth-step-inactive-text)' }}
                  >
                    {m.label}
                  </div>
                </div>

                {/* Đường nối giữa các node */}
                {!isLast && (
                  <div className="mx-3 flex-1 h-[2px] relative overflow-hidden" style={{ background: 'var(--auth-step-line)' }}>
                    <div
                      className={`absolute top-0 left-0 h-full transition-all duration-400 ${isDone ? 'w-full' : 'w-0'}`}
                      style={{ background: 'var(--auth-step-line-done)' }}
                    />
                  </div>
                )}
              </div>;
        })}
      </div>
    </div>
  );
}

// ─── Slide animation khi chuyển bước ──────────────────────────────
export function SlidePanel({ children, stepKey }) {
  const [state, setState] = useState('entering');

  useEffect(() => {
    // Khởi tạo animation theo step mới rồi chuyển trạng thái ở tick kế tiếp.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setState('entering');
    const t = setTimeout(() => setState('visible'), 40);
    return () => clearTimeout(t);
  }, [stepKey]);

  return (
    <div
      style={{
        opacity: state === 'visible' ? 1 : 0,
        transform: state === 'visible' ? 'translateX(0)' : 'translateX(16px)',
        transition: 'opacity 0.28s ease, transform 0.28s ease',
      }}
    >
      {children}
    </div>
  );
}

// ─── Thông báo lỗi ─────────────────────────────────────────────────
export function ErrorMsg({ msg }) {
  if (!msg) return null;
  return (
    <div 
      className="mt-4 flex items-center gap-3 rounded-xl border px-4 py-3"
      style={{ background: 'var(--auth-error-bg)', borderColor: 'var(--auth-error-border)' }}
    >
      <div className="flex-shrink-0 h-5 w-5 rounded-full flex items-center justify-center" style={{ background: 'var(--auth-error-bg)' }}>
        <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" style={{ color: 'var(--auth-error-text)' }} strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </div>
      <p className="text-sm font-medium leading-relaxed" style={{ color: 'var(--auth-error-text)' }}>{msg}</p>
    </div>
  );
}

// ─── Nút chính (Primary) ────────────────────────────────────────────
export function PrimaryBtn({ onClick, children, disabled = false, className = '' }) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className={`h-12 w-full rounded-xl text-base font-semibold transition-all duration-200 hover:-translate-y-0.5 active:translate-y-0 active:scale-[0.99] disabled:cursor-not-allowed disabled:opacity-60 flex items-center justify-center gap-2 ${className}`}
      style={{ background: 'var(--auth-btn-bg)', color: 'var(--auth-btn-text)', boxShadow: 'var(--auth-btn-shadow)' }}
    >
      {children}
    </button>
  );
}

// ─── Nút phụ (Secondary) ─────────────────────────────────────────
export function SecondaryBtn({ onClick, children, disabled = false, className = '' }) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className={`h-12 w-full rounded-xl border text-base font-semibold transition-all duration-200 hover:-translate-y-0.5 active:translate-y-0 active:scale-[0.99] disabled:cursor-not-allowed disabled:opacity-60 flex items-center justify-center gap-2 ${className}`}
      style={{ background: 'var(--auth-btn-secondary-bg)', borderColor: 'var(--auth-btn-secondary-border)', color: 'var(--auth-btn-secondary-text)' }}
    >
      {children}
    </button>
  );
}

export function BackBtn({ onClick }) {
  return <SecondaryBtn onClick={onClick}>Quay lại</SecondaryBtn>;
}
