import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import BrandLockup from '../../../components/common/BrandLockup.jsx';
import Button from '../../../components/common/Button.jsx';
import { useAuth } from '../hooks/useAuth.js';

// Trang xác nhận đăng ký hoàn tất, dùng cùng ngôn ngữ thiết kế với khu vực Auth.
export default function OnboardingSuccessPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { updateProfileCompletion } = useAuth();
  const [isPreparing, setIsPreparing] = useState(true);
  const [showCheckmark, setShowCheckmark] = useState(false);

  useEffect(() => {
    if (location.state?.onboardingJustCompleted) {
      // Chỉ cập nhật AuthContext sau khi route success đã mount để tránh guard chuyển thẳng sang Feed.
      updateProfileCompletion(true);
    }
  }, [location.state, updateProfileCompletion]);

  useEffect(() => {
    // Chuyển tiếp ngắn giúp người dùng nhận biết hệ thống đang chuẩn bị giao diện sau khi Backend đã lưu xong.
    const revealTimer = setTimeout(() => setIsPreparing(false), 600);
    const checkmarkTimer = setTimeout(() => setShowCheckmark(true), 760);
    return () => {
      clearTimeout(revealTimer);
      clearTimeout(checkmarkTimer);
    };
  }, []);

  return (
    <main className="relative flex min-h-screen items-center justify-center overflow-hidden bg-[#0c1120] px-4 py-10">
      {/* Nền chấm nhỏ đồng bộ với các màn hình đăng nhập và đăng ký. */}
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.08]"
        style={{
          backgroundImage: 'radial-gradient(circle, #ffffff 1px, transparent 1px)',
          backgroundSize: '300px 300px',
        }}
      />

      <section className="relative z-10 w-full max-w-[430px]">
        {isPreparing ? (
          <div
            className="rounded-[24px] border border-slate-200 bg-white px-6 py-12 text-center shadow-[0_24px_70px_rgba(0,0,0,0.32)] sm:px-10"
            role="status"
            aria-live="polite"
          >
            <div className="flex justify-center">
              <BrandLockup compact />
            </div>
            <div className="mx-auto mb-5 mt-9 h-9 w-9 animate-spin rounded-full border-[3px] border-slate-200 border-t-slate-900" />
            <p className="text-base font-semibold text-slate-900">Đang chuẩn bị trang của bạn...</p>
            <p className="mt-2 text-sm text-slate-500">Chỉ mất một chút thời gian.</p>
          </div>
        ) : (
          <div className="animate-[fadeInUp_0.42s_ease-out_both] rounded-[24px] border border-slate-200 bg-white px-6 py-8 text-center shadow-[0_24px_70px_rgba(0,0,0,0.32)] sm:px-10 sm:py-10">
            <div className="flex justify-center">
              <BrandLockup compact />
            </div>

            {/* Dấu tích đen trắng không viền, đồng bộ với hệ màu chính của giao diện. */}
            <div
              className="mx-auto mb-5 mt-8 h-20 w-20 drop-shadow-[0_10px_18px_rgba(15,23,42,0.22)]"
              style={{
                opacity: showCheckmark ? 1 : 0,
                transform: showCheckmark ? 'scale(1)' : 'scale(0.72)',
                transition: 'opacity 220ms ease-out, transform 420ms cubic-bezier(0.22, 1, 0.36, 1)',
              }}
            >
              <svg className="h-full w-full" viewBox="0 0 80 80" fill="none" aria-hidden="true">
                <circle cx="40" cy="40" r="32" fill="#0f172a" />
                <path
                  d="M23.5 40.5 35 52l22-24"
                  stroke="white"
                  strokeWidth="6"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  pathLength="1"
                  style={{
                    strokeDasharray: 1,
                    strokeDashoffset: showCheckmark ? 0 : 1,
                    transition: 'stroke-dashoffset 480ms ease-out 120ms',
                  }}
                />
              </svg>
            </div>
            <h1 className="mb-3 text-2xl font-extrabold tracking-tight text-slate-950 sm:text-[28px]">
              Tạo tài khoản thành công!
            </h1>
            <p className="mx-auto max-w-[320px] text-[15px] leading-6 text-slate-600">
              Hồ sơ của bạn đã sẵn sàng. Bắt đầu khám phá những bài viết mới và kết nối cùng cộng đồng UniShare.
            </p>

            <div className="mt-8 border-t border-slate-200 pt-6">
              <Button
                type="button"
                size="lg"
                onClick={() => navigate('/feed/for-you', { replace: true })}
                className="w-full gap-2"
              >
                <span>Vào trang chủ</span>
                <svg
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  aria-hidden="true"
                >
                  <path d="M5 12h14" />
                  <path d="m13 6 6 6-6 6" />
                </svg>
              </Button>
            </div>
          </div>
        )}
      </section>
    </main>
  );
}
