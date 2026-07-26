

// Minh họa hero bên trái: mạng lưới sinh viên kết nối
export function RegisterHeroIllustration() {
  return (
    <div className="relative w-full max-w-sm mx-auto my-8 animate-[float_6s_ease-in-out_infinite]">
      <svg viewBox="0 0 420 300" className="w-full h-auto drop-shadow-2xl" fill="none" xmlns="http://www.w3.org/2000/svg">
        {/* Vòng tròn nền đứt nét */}
        <circle cx="210" cy="150" r="118" stroke="#334155" strokeWidth="1.5" strokeDasharray="6 6" opacity="0.7" />
        
        {/* Các đường kết nối giữa node */}
        <line x1="120" y1="150" x2="210" y2="150" stroke="#475569" strokeWidth="1.5" strokeDasharray="4 4" opacity="0.6"/>
        <line x1="210" y1="150" x2="300" y2="150" stroke="#475569" strokeWidth="1.5" strokeDasharray="4 4" opacity="0.6"/>
        <line x1="210" y1="150" x2="210" y2="62" stroke="#475569" strokeWidth="1.5" strokeDasharray="4 4" opacity="0.5"/>
        <line x1="120" y1="150" x2="88" y2="82" stroke="#475569" strokeWidth="1" strokeDasharray="3 5" opacity="0.4"/>
        <line x1="300" y1="150" x2="325" y2="82" stroke="#475569" strokeWidth="1" strokeDasharray="3 5" opacity="0.4"/>
        <line x1="210" y1="150" x2="308" y2="206" stroke="#475569" strokeWidth="1" strokeDasharray="3 5" opacity="0.4"/>
        <line x1="210" y1="150" x2="112" y2="206" stroke="#475569" strokeWidth="1" strokeDasharray="3 5" opacity="0.4"/>

        {/* Avatar trung tâm (lớn hơn) */}
        <circle cx="210" cy="150" r="38" fill="#475569" stroke="#0f172a" strokeWidth="4" />
        <circle cx="210" cy="139" r="13" fill="#cbd5e1" />
        <path d="M188 168 Q210 146 232 168 A22 22 0 0 1 188 168" fill="#cbd5e1" />

        {/* Avatar trái */}
        <circle cx="120" cy="150" r="26" fill="#374151" stroke="#0f172a" strokeWidth="3" />
        <circle cx="120" cy="141" r="9" fill="#94a3b8" />
        <path d="M104 162 Q120 146 136 162 A16 16 0 0 1 104 162" fill="#94a3b8" />

        {/* Avatar phải */}
        <circle cx="300" cy="150" r="26" fill="#374151" stroke="#0f172a" strokeWidth="3" />
        <circle cx="300" cy="141" r="9" fill="#94a3b8" />
        <path d="M284 162 Q300 146 316 162 A16 16 0 0 1 284 162" fill="#94a3b8" />

        {/* Node trên (sách/doc – màu xanh dương) */}
        <circle cx="210" cy="62" r="16" fill="#1e40af" fillOpacity="0.25" stroke="#3b82f6" strokeWidth="1.5" />
        <rect x="203" y="55" width="14" height="14" rx="2.5" fill="#3b82f6" />

        {/* Node trên phải (heart – màu đỏ hồng) */}
        <circle cx="325" cy="82" r="18" fill="#9f1239" fillOpacity="0.2" stroke="#e11d48" strokeWidth="1.5" />
        <path d="M325 88 l-5-5 a3 3 0 0 1 5-4 a3 3 0 0 1 5 4 l-5 5 z" fill="#e11d48" />

        {/* Node trên trái (chat – màu xám) */}
        <circle cx="88" cy="82" r="18" fill="#475569" fillOpacity="0.2" stroke="#64748b" strokeWidth="1.5" />
        <rect x="78" y="75" width="20" height="13" rx="3" fill="#64748b" />
        <polygon points="82,88 86,88 84,93" fill="#64748b" />

        {/* Bút chì dưới phải */}
        <g transform="translate(308, 206) rotate(-42)">
          <rect x="-5" y="-22" width="10" height="32" fill="#fbbf24" rx="2" />
          <polygon points="-5,10 5,10 0,21" fill="#f59e0b" />
          <polygon points="-2,10 2,10 0,17" fill="#1e293b" />
          <rect x="-5" y="-25" width="10" height="5" rx="1" fill="#e2e8f0" />
        </g>

        {/* Tài liệu dưới trái */}
        <g transform="translate(112, 206) rotate(12)">
          <rect x="-16" y="-22" width="32" height="42" rx="4" fill="#374151" stroke="#94a3b8" strokeWidth="1.5" />
          <line x1="-9" y1="-12" x2="9" y2="-12" stroke="#94a3b8" strokeWidth="1.8" strokeLinecap="round" />
          <line x1="-9" y1="-4" x2="9" y2="-4" stroke="#94a3b8" strokeWidth="1.8" strokeLinecap="round" />
          <line x1="-9" y1="4" x2="2" y2="4" stroke="#94a3b8" strokeWidth="1.8" strokeLinecap="round" />
        </g>
      </svg>
    </div>
  );
}

// Layout chính cho trang auth: nền tối với chấm lưới, cột trái hero + cột phải form
export default function AuthLayout({ children }) {
  return (
    <div className="min-h-screen lg:h-screen lg:overflow-hidden w-full flex flex-col lg:flex-row bg-[#0c1120] text-slate-100 font-sans relative">

      {/* Nền lưới chấm nhỏ */}
      <div
        className="absolute inset-0 z-0 opacity-[0.08] pointer-events-none"
        style={{
          backgroundImage: 'radial-gradient(circle, #ffffff 1px, transparent 1px)',
          backgroundSize: '26px 26px'
        }}
      />

      {/* Hiệu ứng glow tím mờ ở góc phải dưới */}
      <div
        className="absolute bottom-0 right-0 w-[500px] h-[400px] z-0 pointer-events-none"
        style={{
          background: 'radial-gradient(ellipse at bottom right, rgba(91,44,255,0.12) 0%, transparent 70%)'
        }}
      />

      {/* ===== Cột trái: Hero Section ===== */}
      <div className="hidden relative z-10 w-full lg:w-[55%] lg:flex flex-col justify-center items-center p-8 lg:p-14 animate-[fadeInLeft_0.6s_ease-out]">

        {/* Badge "Tham gia cộng đồng sinh viên" */}
        <div className="flex items-center gap-2 rounded-full border border-slate-600/60 bg-slate-800/50 px-4 py-1.5 mb-8 backdrop-blur-sm">
          <img
            src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23818cf8' width='14' height='14'%3E%3Cpath d='M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5'/%3E%3C/svg%3E"
            alt=""
            className="w-3.5 h-3.5 opacity-80"
            aria-hidden="true"
          />
          <span className="text-xs font-semibold text-slate-300 tracking-wide">Tham gia cộng đồng sinh viên</span>
        </div>

        {/* Tiêu đề chính – dùng mixed weight + gradient accent để tránh flat */}
        <h1 className="text-3xl lg:text-[2.7rem] font-extrabold text-white text-center mb-4 leading-[1.18] tracking-tight">
          Khởi đầu{' '}
          <span
            className="italic font-black"
            style={{
              background: 'linear-gradient(90deg, #a78bfa 0%, #818cf8 50%, #60a5fa 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              backgroundClip: 'text',
            }}
          >
            hành trình
          </span>
          <br />
          <span className="font-light tracking-normal text-slate-200">học tập</span>
          {' '}<span className="text-white">của bạn.</span>
        </h1>
        <p className="text-sm lg:text-base text-slate-400 text-center max-w-md font-medium leading-relaxed">
          Hàng nghìn sinh viên đang học hỏi,<br className="hidden sm:inline" />
          chia sẻ và phát triển cùng nhau trên UniShare.
        </p>

        {/* Minh họa SVG */}
        <RegisterHeroIllustration />

        {/* Thống kê */}
        <div className="flex flex-wrap justify-center gap-3 sm:gap-4 mt-0">
          {[
            { value: '10K+', label: 'Sinh viên' },
            { value: '50K+', label: 'Bài chia sẻ' },
            { value: '100+', label: 'Trường ĐH' },
          ].map((stat) => (
            <div
              key={stat.label}
              className="bg-slate-800/50 border border-slate-700/60 rounded-xl px-6 py-3.5 text-center min-w-[105px] backdrop-blur-sm"
            >
              <div className="text-xl font-bold text-white mb-0.5">{stat.value}</div>
              <div className="text-xs text-slate-400 font-medium">{stat.label}</div>
            </div>
          ))}
        </div>
      </div>

      {/* ===== Cột phải: Form Card ===== */}
      <div className="relative z-10 w-full lg:w-[45%] flex items-center justify-center p-4 py-10 lg:p-8 lg:overflow-y-auto">
        <div className="w-full max-w-[500px] animate-[fadeInUp_0.5s_ease-out]">
          {/*
            Wrapper tạo viền gradient tím → xanh dương theo hình tham chiếu.
            Kỹ thuật: padding 2px + nền gradient, bên trong là card trắng.
          */}
          <div
            className="rounded-[20px] p-[2px]"
            style={{
              background: 'linear-gradient(135deg, #7c3aed 0%, #4f46e5 40%, #2563eb 100%)',
            }}
          >
            <div className="auth-card-theme bg-white rounded-[18px] overflow-hidden shadow-2xl">
              {children}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
