import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import logo from '../../../assets/brand/logo-dark.jpg';
import { socialApi } from '../../../api/index.js';
import { useApp } from '../../../contexts/AppContext.jsx';
import { onboardingService } from '../services/onboardingService.js';
import { uploadSelectedOnboardingAvatar } from '../utils/onboardingAvatarUpload.js';
import { todayIsoDate, calcAge } from '../components/onboarding/onboardingUtils.js';
import { StepIndicator } from '../components/onboarding/OnboardingShared.jsx';
import OnboardingStep1Name from '../components/onboarding/OnboardingStep1Name.jsx';
import OnboardingStep2Avatar from '../components/onboarding/OnboardingStep2Avatar.jsx';
import OnboardingStep3Info from '../components/onboarding/OnboardingStep3Info.jsx';

// ── Minh họa hero – Các thẻ mạng xã hội trang trí ────────────────
function HeroIllustration() {
  return (
    <div className="relative z-10 mx-auto mt-6 h-[280px] w-full max-w-[420px] select-none xl:mt-8 xl:h-[320px]" aria-hidden="true">
      {/* Thẻ bài viết mạng xã hội */}
      <div
        className="absolute left-[4%] top-[6%] w-[200px] rounded-2xl p-3.5 backdrop-blur-sm xl:w-[220px] xl:p-4"
        style={{ animation: 'fadeInUp 0.5s ease-out 0.2s both', background: 'var(--auth-card-bg)', opacity: 0.97, boxShadow: 'var(--auth-card-shadow)' }}
      >
        <div className="flex items-center gap-2.5">
          <div className="h-8 w-8 rounded-full bg-zinc-900" />
          <div>
            <div className="h-2 w-16 rounded-full bg-zinc-800/70" />
            <div className="mt-1.5 h-1.5 w-10 rounded-full bg-zinc-200" />
          </div>
        </div>
        <div className="mt-3 h-[72px] w-full rounded-xl bg-zinc-100 xl:h-20" />
        <div className="mt-2.5 flex items-center gap-4">
          <div className="flex items-center gap-1">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="#e4e4e7" stroke="#18181b" strokeWidth="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" /></svg>
            <span className="text-[11px] font-medium text-zinc-500">24</span>
          </div>
          <div className="flex items-center gap-1">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#18181b" strokeWidth="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" /></svg>
            <span className="text-[11px] font-medium text-zinc-500">8</span>
          </div>
        </div>
      </div>

      {/* Avatar với nút kết bạn "+" */}
      <div className="absolute right-[12%] top-[4%]" style={{ animation: 'fadeInUp 0.5s ease-out 0.4s both' }}>
        <div className="relative">
          <div className="h-11 w-11 rounded-full bg-zinc-800 ring-[3px] ring-white" />
          <div className="absolute -bottom-0.5 -right-0.5 flex h-[18px] w-[18px] items-center justify-center rounded-full bg-zinc-950 text-white ring-2 ring-white">
            <svg width="9" height="9" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3.5"><path d="M12 5v14M5 12h14" /></svg>
          </div>
        </div>
      </div>

      {/* Bong bóng chat */}
      <div
        className="absolute right-[2%] top-[34%] w-[150px] rounded-2xl rounded-br-sm bg-zinc-950 px-3.5 py-2.5 shadow-lg xl:w-[160px]"
        style={{ animation: 'fadeInUp 0.5s ease-out 0.6s both' }}
      >
        <p className="text-[11px] font-medium leading-relaxed text-white">Chào bạn! Cùng nhóm project nhé 👋</p>
      </div>

      {/* Nhóm avatar */}
      <div className="absolute bottom-[20%] right-[12%] flex -space-x-2" style={{ animation: 'fadeInUp 0.5s ease-out 0.8s both' }}>
        <div className="h-8 w-8 rounded-full bg-zinc-800 ring-2 ring-white" />
        <div className="h-8 w-8 rounded-full bg-zinc-700 ring-2 ring-white" />
        <div className="h-8 w-8 rounded-full bg-zinc-600 ring-2 ring-white" />
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-zinc-100 text-[11px] font-bold text-zinc-700 ring-2 ring-white">+5</div>
      </div>

      {/* Thẻ thông báo */}
      <div
        className="absolute bottom-[5%] left-[2%] flex items-center gap-2.5 rounded-xl px-3.5 py-2.5 backdrop-blur-sm"
        style={{ animation: 'fadeInUp 0.5s ease-out 1s both', background: 'var(--auth-card-bg)', opacity: 0.97, boxShadow: 'var(--auth-card-shadow)' }}
      >
        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-zinc-100">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#09090b" strokeWidth="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" /><path d="M13.73 21a2 2 0 0 1-3.46 0" /></svg>
        </div>
        <div>
          <p className="text-[11px] font-semibold text-zinc-900">3 bạn mới</p>
          <p className="text-[10px] text-zinc-500">vừa theo dõi bạn</p>
        </div>
      </div>
    </div>
  );
}

// ── Trang chính: layout 2 cột đồng bộ với AuthEntryLayout ────────
export default function OnboardingProfilePage() {
  const { currentUser } = useApp();
  const navigate = useNavigate();

  // State form chia sẻ giữa cả 3 bước
  const [step, setStep] = useState(1);
  const [form, setForm] = useState({
    displayName: currentUser?.profile?.displayName ?? '',
    avatarUrl: currentUser?.profile?.avatarUrl ?? '',
    avatarFile: null,
    dateOfBirth: currentUser?.profile?.dateOfBirth ?? '',
    bio: currentUser?.profile?.bio ?? '',
  });
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Helper: cập nhật một field, xóa lỗi hiện tại
  function setField(field) {
    return (value) => {
      setForm((prev) => ({ ...prev, [field]: value }));
      setError('');
    };
  }

  function setAvatar(file, previewUrl) {
    setForm((previous) => ({ ...previous, avatarFile: file, avatarUrl: previewUrl }));
    setError('');
  }

  // ── Validate bước 1: tên hiển thị bắt buộc, ≥2 ký tự ──
  function handleNextFromStep1() {
    if (!form.displayName.trim()) {
      setError('Vui lòng nhập tên hiển thị để tiếp tục.');
      return;
    }
    if (form.displayName.trim().length < 2) {
      setError('Tên hiển thị phải có ít nhất 2 ký tự.');
      return;
    }
    setError('');
    setStep(2);
  }

  // ── Bước 2: ảnh là tùy chọn, chuyển tiếp không cần validate ──
  function handleNextFromStep2() {
    setError('');
    setStep(3);
  }

  // ── Validate bước 3: ngày sinh bắt buộc + ≥18 tuổi ──
  async function handleFinish() {
    if (isSubmitting) return;
    if (!form.dateOfBirth) {
      setError('Ngày sinh là bắt buộc để hoàn tất hồ sơ.');
      return;
    }
    if (form.dateOfBirth > todayIsoDate()) {
      setError('Ngày sinh không được lớn hơn ngày hiện tại.');
      return;
    }
    if (calcAge(form.dateOfBirth) < 18) {
      setError('Bạn phải đủ 18 tuổi để tham gia sử dụng UniShare.');
      return;
    }

    setIsSubmitting(true);
    setError('');
    try {
      // Avatar endpoint được Backend cho phép trong onboarding. Upload trước để chỉ điều hướng
      // thành công khi ảnh đã được lưu thật, thay vì mất preview base64 sau khi đổi trang.
      const uploadedAvatarUrl = await uploadSelectedOnboardingAvatar(form.avatarFile, socialApi.uploadAvatar);
      if (uploadedAvatarUrl) {
        // Nếu bước hoàn tất hồ sơ thất bại, lần thử lại không upload trùng ảnh đã lưu.
        setForm((previous) => ({ ...previous, avatarFile: null, avatarUrl: uploadedAvatarUrl }));
      }
      const result = await onboardingService.completeProfile(form);
      if (!result.profileCompleted) {
        throw new Error('Backend chưa xác nhận hồ sơ đã hoàn tất. Vui lòng thử lại.');
      }
      // Đánh dấu chuyển tiếp hợp lệ để route success hiển thị trước khi guard nhận trạng thái profile mới.
      navigate('/onboarding/success', {
        replace: true,
        state: { onboardingJustCompleted: true },
      });
    } catch (submitError) {
      setError(submitError.message || 'Không thể lưu hồ sơ. Vui lòng thử lại.');
    } finally {
      setIsSubmitting(false);
    }
  }

  // ── Quay lại bước trước, xóa lỗi ──
  function goBack() {
    setError('');
    setStep((s) => Math.max(1, s - 1));
  }

  return (
    <main className="auth-theme relative min-h-dvh overflow-x-hidden font-sans text-zinc-950 lg:h-dvh lg:overflow-hidden" style={{ background: 'var(--auth-bg)' }}>
      {/* Lớp nền uốn cong tím – đồng bộ thiết kế Auth */}
      <div className="pointer-events-none absolute inset-0 hidden overflow-hidden lg:block" aria-hidden="true">
        <div className="auth-hero-oval auth-hero-oval--outer absolute -left-[14%] -top-[20%] h-[140%] w-[68%] rounded-[50%] blur-[2px]" style={{ background: 'var(--auth-hero-oval)' }} />
        <div className="auth-hero-oval absolute -left-[18%] -top-[16%] h-[132%] w-[64%] rounded-[50%]" style={{ background: 'var(--auth-hero-gradient)', boxShadow: 'var(--auth-hero-shadow)' }} />
        <div className="absolute -left-16 bottom-[-90px] h-64 w-64 rounded-full [background-size:14px_14px]" style={{ background: 'radial-gradient(circle, var(--auth-dot-color) 1.2px, transparent 1.4px)', opacity: 'var(--auth-dot-opacity)' }} />
      </div>

      <div className="relative z-10 min-h-dvh lg:grid lg:h-dvh lg:grid-cols-[minmax(0,54fr)_minmax(450px,46fr)]">
        {/* ── Cột trái: Hero thông điệp ── */}
        <section className="relative hidden h-dvh bg-transparent px-10 py-7 lg:flex lg:flex-col xl:px-12 xl:py-8 2xl:px-16">
          {/* Logo ứng dụng */}
          <Link to="/" className="relative z-10 inline-flex w-fit items-center gap-3.5" aria-label="UniShare - Trang chủ">
            <img src={logo} alt="" className="h-11 w-11 rounded-xl object-cover shadow-xs xl:h-12 xl:w-12" />
            <span className="text-2xl font-bold tracking-[-0.02em] text-zinc-950 xl:text-[26px]">UniShare</span>
          </Link>

          {/* Khối Hero thông điệp chính */}
          <div className="relative z-10 mx-auto mt-6 w-full max-w-[650px] pr-10 xl:mt-8 xl:pr-6 2xl:mt-10">
            <h1 className="max-w-[620px] text-[2.3rem] font-semibold leading-[1.15] tracking-[-0.02em] text-zinc-950 xl:text-[2.85rem] 2xl:text-[3.15rem]">
              Hoàn thiện hồ sơ<br />
              Bắt đầu{' '}
              <span className="italic font-bold text-zinc-950 tracking-tight">trải nghiệm</span>
            </h1>
            <p className="mt-3.5 max-w-[520px] text-[14px] leading-6 text-zinc-500 xl:text-[15px]">
              Giúp bạn kết nối, chia sẻ và học hỏi
              <br className="hidden 2xl:block" /> cùng cộng đồng sinh viên.
            </p>
          </div>

          {/* Minh họa mạng xã hội */}
          <HeroIllustration />

          {/* Điểm nổi bật – dưới cùng */}
          <div className="relative z-10 mt-auto space-y-3.5 pb-4">
            {[
              {
                icon: (
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#09090b" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                    <circle cx="9" cy="7" r="4" />
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                    <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                  </svg>
                ),
                text: 'Kết nối bạn bè cùng trường',
              },
              {
                icon: (
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#09090b" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z" />
                    <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z" />
                  </svg>
                ),
                text: 'Chia sẻ kiến thức và kinh nghiệm',
              },
              {
                icon: (
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#09090b" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
                  </svg>
                ),
                text: 'Lưu lại những nội dung hữu ích',
              },
            ].map((item) => (
              <div key={item.text} className="flex items-center gap-3 text-[14px] text-zinc-600">
                <div className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-zinc-100">
                  {item.icon}
                </div>
                <span>{item.text}</span>
              </div>
            ))}
          </div>
        </section>

        {/* ── Cột phải: Form theo bước ── */}
        <section className="flex min-h-dvh flex-col items-center bg-transparent px-4 py-8 sm:px-8 lg:justify-center lg:overflow-y-auto lg:py-10">
          {/* Logo mobile – ẩn trên desktop */}
          <div className="mb-8 flex items-center gap-3 lg:hidden">
            <img src={logo} alt="UniShare" className="h-10 w-10 rounded-xl object-cover shadow-xs" />
            <span className="text-xl font-bold text-zinc-950">UniShare</span>
          </div>

          <div className="w-full max-w-[460px]" style={{ animation: 'fadeInUp 0.4s ease-out both' }}>
            {/* Thanh tiến trình bước */}
            <StepIndicator current={step} total={3} />

            {/* Nội dung form theo bước */}
            <div className="mt-10">
              {/* Bước 1 – Tên hiển thị */}
              {step === 1 && (
                <OnboardingStep1Name
                  displayName={form.displayName}
                  onChange={setField('displayName')}
                  onNext={handleNextFromStep1}
                  error={error}
                />
              )}

              {/* Bước 2 – Ảnh đại diện */}
              {step === 2 && (
                <OnboardingStep2Avatar
                  avatarUrl={form.avatarUrl}
                  displayName={form.displayName}
                  onAvatarChange={setAvatar}
                  onNext={handleNextFromStep2}
                  onBack={goBack}
                />
              )}

              {/* Bước 3 – Ngày sinh + Bio */}
              {step === 3 && (
                <OnboardingStep3Info
                  dateOfBirth={form.dateOfBirth}
                  bio={form.bio}
                  onDateChange={setField('dateOfBirth')}
                  onBioChange={setField('bio')}
                  onFinish={handleFinish}
                  onBack={goBack}
                  error={error}
                  isSubmitting={isSubmitting}
                />
              )}
            </div>
          </div>
        </section>
      </div>
    </main>
  );
}
