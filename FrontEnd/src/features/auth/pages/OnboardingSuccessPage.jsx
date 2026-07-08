import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
// Trang thành công sau khi hoàn tất onboarding hồ sơ
export default function OnboardingSuccessPage() {
  const navigate = useNavigate();
  // Delay animation xuất hiện icon tích
  const [checked, setChecked] = useState(false);
  useEffect(() => {
    const t = setTimeout(() => setChecked(true), 200);
    return () => clearTimeout(t);
  }, []);

  return (
    <main className="relative flex min-h-screen flex-col items-center justify-center px-4 py-10 bg-[#0c1120] overflow-hidden">

      {/* Lưới chấm nền */}
      <div
        className="absolute inset-0 opacity-[0.07] pointer-events-none"
        style={{
          backgroundImage: 'radial-gradient(circle, #ffffff 1px, transparent 1px)',
          backgroundSize: '26px 26px',
        }}
      />

      {/* Orb tím lớn – nền animated */}
      <div
        className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[700px] h-[500px] rounded-full pointer-events-none"
        style={{
          background: 'radial-gradient(ellipse at center, rgba(124,58,237,0.14) 0%, transparent 65%)',
          animation: 'float 9s ease-in-out infinite',
        }}
      />
      {/* Orb xanh lam */}
      <div
        className="absolute -bottom-40 -right-20 w-[450px] h-[450px] rounded-full pointer-events-none"
        style={{
          background: 'radial-gradient(circle, rgba(37,99,235,0.13) 0%, transparent 65%)',
          animation: 'float 11s ease-in-out infinite reverse',
        }}
      />

      {/* Card */}
      <div
        className="relative z-10 w-full max-w-[420px]"
        style={{
          animation: 'fadeInUp 0.5s ease-out both',
          background: 'linear-gradient(135deg, #7c3aed 0%, #4f46e5 45%, #2563eb 100%)',
          borderRadius: '24px',
          padding: '2px',
        }}
      >
        <div className="bg-white rounded-[22px] px-8 py-10 text-center">

          {/* Icon tích thành công với scale-in animation */}
          <div
            className="mx-auto mb-6 flex h-20 w-20 items-center justify-center rounded-full"
            style={{
              background: 'linear-gradient(135deg, #7c3aed, #4f46e5)',
              transform: checked ? 'scale(1)' : 'scale(0)',
              transition: 'transform 0.45s cubic-bezier(0.175, 0.885, 0.32, 1.275)',
            }}
          >
            <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.8" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="20 6 9 17 4 12" />
            </svg>
          </div>

          <h2 className="text-2xl font-bold text-gray-900 mb-2">
            Hồ sơ đã hoàn tất!
          </h2>
          <p className="text-base text-gray-500 leading-relaxed max-w-[290px] mx-auto mb-8">
            Chào mừng bạn đến với cộng đồng sinh viên UniShare. Hãy bắt đầu khám phá và kết nối ngay!
          </p>

          {/* Nút chính */}
          <button
            type="button"
            onClick={() => navigate('/feed/for-you')}
            className="w-full h-12 rounded-xl bg-gray-900 hover:bg-black text-white text-base font-semibold transition-colors shadow-sm"
          >
            Khám phá Feed
          </button>
        </div>
      </div>
    </main>
  );
}
