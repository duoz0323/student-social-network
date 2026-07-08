import { useState, useEffect } from 'react';

// ─── Icons ─────────────────────────────────────────────────────────
export function CameraIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
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

export function ArrowLeftIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M19 12H5M5 12l7 7M5 12l7-7" />
    </svg>
  );
}

// ─── Background chủ đề sinh viên ──────────────────────────────────
// Các icon nổi lên ở background với animation float để tạo chiều sâu
const BG_ICONS = [
  // Mũ tốt nghiệp – biểu tượng sinh viên
  { x: '8%', y: '12%', size: 28, delay: '0s', dur: '7s', opacity: 0.18, el: 'graduation' },
  { x: '88%', y: '18%', size: 22, delay: '2s', dur: '9s', opacity: 0.14, el: 'graduation' },
  // Sách – học tập
  { x: '5%', y: '60%', size: 24, delay: '1.5s', dur: '8s', opacity: 0.16, el: 'book' },
  { x: '92%', y: '55%', size: 20, delay: '3.5s', dur: '10s', opacity: 0.13, el: 'book' },
  // Chat bubble – mạng xã hội
  { x: '15%', y: '80%', size: 26, delay: '0.8s', dur: '8.5s', opacity: 0.15, el: 'chat' },
  { x: '80%', y: '75%', size: 20, delay: '2.5s', dur: '7.5s', opacity: 0.13, el: 'chat' },
  // Ngôi sao / ý tưởng
  { x: '75%', y: '10%', size: 18, delay: '1s', dur: '11s', opacity: 0.12, el: 'star' },
  { x: '30%', y: '88%', size: 16, delay: '4s', dur: '9.5s', opacity: 0.10, el: 'star' },
  // Bút chì
  { x: '55%', y: '5%', size: 20, delay: '3s', dur: '8s', opacity: 0.12, el: 'pencil' },
  // Tim yêu thích
  { x: '3%', y: '35%', size: 16, delay: '2s', dur: '10s', opacity: 0.11, el: 'heart' },
  { x: '95%', y: '38%', size: 14, delay: '5s', dur: '7s', opacity: 0.10, el: 'heart' },
];

function BgIcon({ type, size }) {
  const s = size;
  const color = 'rgba(167,139,250,1)'; // violet-400

  if (type === 'graduation') return (
    <svg width={s} height={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <path d="M22 10v6M2 10l10-5 10 5-10 5z" />
      <path d="M6 12v5c3 3 9 3 12 0v-5" />
    </svg>
  );
  if (type === 'book') return (
    <svg width={s} height={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
      <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
    </svg>
  );
  if (type === 'chat') return (
    <svg width={s} height={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
    </svg>
  );
  if (type === 'star') return (
    <svg width={s} height={s} viewBox="0 0 24 24" fill="rgba(167,139,250,0.6)" stroke={color} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
    </svg>
  );
  if (type === 'pencil') return (
    <svg width={s} height={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z" />
    </svg>
  );
  if (type === 'heart') return (
    <svg width={s} height={s} viewBox="0 0 24 24" fill="rgba(167,139,250,0.4)" stroke={color} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
    </svg>
  );
  return null;
}

// Background tổng thể cho trang onboarding
export function OnboardingBackground() {
  return (
    <div className="absolute inset-0 pointer-events-none overflow-hidden">
      {/* Nền tối cơ bản */}
      {/* Lưới chấm mờ */}
      <div
        className="absolute inset-0 opacity-[0.06]"
        style={{
          backgroundImage: 'radial-gradient(circle, #a78bfa 1px, transparent 1px)',
          backgroundSize: '32px 32px',
        }}
      />

      {/* Orb lớn – góc tím trên trái */}
      <div
        className="absolute -top-40 -left-40 w-[600px] h-[600px] rounded-full"
        style={{
          background: 'radial-gradient(circle at 40% 40%, rgba(124,58,237,0.22) 0%, transparent 60%)',
          animation: 'float 9s ease-in-out infinite',
        }}
      />
      {/* Orb xanh indigo – dưới phải */}
      <div
        className="absolute -bottom-40 -right-40 w-[550px] h-[550px] rounded-full"
        style={{
          background: 'radial-gradient(circle at 60% 60%, rgba(79,70,229,0.18) 0%, transparent 60%)',
          animation: 'float 11s ease-in-out infinite reverse',
        }}
      />
      {/* Orb hồng tím – giữa trái */}
      <div
        className="absolute top-1/2 -left-24 w-[300px] h-[300px] rounded-full"
        style={{
          background: 'radial-gradient(circle, rgba(217,70,239,0.10) 0%, transparent 65%)',
          animation: 'float 13s ease-in-out infinite',
          animationDelay: '2s',
        }}
      />

      {/* Các icon floating chủ đề sinh viên */}
      {BG_ICONS.map((icon, i) => (
        <div
          key={i}
          className="absolute"
          style={{
            left: icon.x,
            top: icon.y,
            opacity: icon.opacity,
            animation: `float ${icon.dur} ease-in-out infinite`,
            animationDelay: icon.delay,
            animationDirection: i % 2 === 0 ? 'normal' : 'reverse',
          }}
        >
          <BgIcon type={icon.el} size={icon.size} />
        </div>
      ))}

      {/* Đường mạng lưới kết nối mờ */}
      <svg className="absolute inset-0 w-full h-full opacity-[0.05]" xmlns="http://www.w3.org/2000/svg">
        <line x1="8%" y1="12%" x2="15%" y2="80%" stroke="#a78bfa" strokeWidth="1" strokeDasharray="4 8" />
        <line x1="88%" y1="18%" x2="80%" y2="75%" stroke="#818cf8" strokeWidth="1" strokeDasharray="4 8" />
        <line x1="5%" y1="60%" x2="30%" y2="88%" stroke="#a78bfa" strokeWidth="1" strokeDasharray="3 9" />
        <line x1="75%" y1="10%" x2="55%" y2="5%" stroke="#818cf8" strokeWidth="1" strokeDasharray="4 8" />
      </svg>
    </div>
  );
}

// ─── Step Indicator – dạng progress bar + node ────────────────────
const STEP_META = [
  { label: 'Tên hiển thị', short: '01' },
  { label: 'Ảnh đại diện', short: '02' },
  { label: 'Thông tin', short: '03' },
];

export function StepIndicator({ current, total }) {
  return (
    <div className="mb-10 w-full px-4 mt-2">
      <div className="flex items-center justify-between">
        {STEP_META.map((m, i) => {
          const stepNum = i + 1;
          const isActive = stepNum === current;
          const isDone = stepNum < current;
          const isLast = i === total - 1;

          return (
            <div key={i} className={`flex items-center ${isLast ? 'flex-none' : 'flex-1'}`}>
              
              {/* Vòng tròn số */}
              <div className="relative flex flex-col items-center">
                <div 
                  className={`relative z-10 flex items-center justify-center w-8 h-8 rounded-full border-[2px] transition-colors duration-300 ${
                    isActive 
                      ? 'border-gray-900 bg-white text-gray-900' 
                      : isDone 
                      ? 'border-gray-900 bg-gray-900 text-white' 
                      : 'border-gray-200 bg-white text-gray-400'
                  }`}
                >
                  {isDone ? (
                    <CheckIcon size={14} />
                  ) : (
                    <span className="text-[13px] font-bold">
                      {stepNum}
                    </span>
                  )}
                </div>

                {/* Tên bước */}
                <div 
                  className={`absolute -bottom-6 text-[12px] font-medium transition-colors duration-300 whitespace-nowrap ${
                    isActive ? 'text-gray-900' : 'text-gray-400'
                  }`}
                >
                  {m.label}
                </div>
              </div>

              {/* Đường thẳng nối */}
              {!isLast && (
                <div className="flex-1 h-[2px] bg-gray-200 mx-2 relative overflow-hidden">
                  <div 
                    className={`absolute top-0 left-0 h-full bg-gray-900 transition-all duration-300 ${
                      isDone ? 'w-full' : 'w-0'
                    }`}
                  />
                </div>
              )}
              
            </div>
          );
        })}
      </div>
    </div>
  );
}

// ─── Slide animation khi chuyển bước ──────────────────────────────
export function SlidePanel({ children, stepKey }) {
  const [state, setState] = useState('entering'); // 'entering' | 'visible'

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setState('entering');
    const t = setTimeout(() => setState('visible'), 40);
    return () => clearTimeout(t);
  }, [stepKey]);

  return (
    <div style={{
      opacity: state === 'visible' ? 1 : 0,
      transform: state === 'visible' ? 'translateX(0)' : 'translateX(16px)',
      transition: 'opacity 0.28s ease, transform 0.28s ease',
    }}>
      {children}
    </div>
  );
}

// ─── Thông báo lỗi ─────────────────────────────────────────────────
export function ErrorMsg({ msg }) {
  if (!msg) return null;
  return (
    <div className="mt-4 flex items-center gap-3 rounded-2xl bg-red-50 border border-red-100 px-4 py-3">
      <div className="flex-shrink-0 h-5 w-5 rounded-full bg-red-100 flex items-center justify-center">
        <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#ef4444" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
          <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </div>
      <p className="text-sm font-medium text-red-600 leading-relaxed">{msg}</p>
    </div>
  );
}

// ─── Nút tiếp tục chính ────────────────────────────────────────────
export function PrimaryBtn({ onClick, children }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="mt-6 w-full h-12 rounded-xl bg-gray-900 hover:bg-black active:scale-[0.98] text-white text-base font-semibold transition-all duration-150"
    >
      {children}
    </button>
  );
}

// ─ Nút quay lại dạng phụ (secondary) ─────────────────────────
export function SecondaryBtn({ onClick, children }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="mt-3 w-full h-12 rounded-xl border border-gray-300 bg-white hover:bg-gray-50 active:scale-[0.98] text-gray-700 text-base font-semibold transition-all duration-150 flex items-center justify-center gap-2"
    >
      <ArrowLeftIcon />
      {children}
    </button>
  );
}

// ─ BackBtn giữ lại để không break import cũ ────────────────────
export function BackBtn({ onClick }) {
  return <SecondaryBtn onClick={onClick}>Quay lại</SecondaryBtn>;
}
