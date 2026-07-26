import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import logo from '../../../assets/brand/logo.png';
import { useApp } from '../../../contexts/AppContext.jsx';
import { onboardingService } from '../services/onboardingService.js';
import { todayIsoDate, calcAge } from '../components/onboarding/onboardingUtils.js';
import { OnboardingBackground, StepIndicator } from '../components/onboarding/OnboardingShared.jsx';
import OnboardingStep1Name from '../components/onboarding/OnboardingStep1Name.jsx';
import OnboardingStep2Avatar from '../components/onboarding/OnboardingStep2Avatar.jsx';
import OnboardingStep3Info from '../components/onboarding/OnboardingStep3Info.jsx';

export default function OnboardingProfilePage() {
  const { currentUser } = useApp();
  const navigate = useNavigate();

  // State form chia sẻ giữa cả 3 bước
  const [step, setStep] = useState(1);
  const [form, setForm] = useState({
    displayName: currentUser?.profile?.displayName ?? '',
    avatarUrl: currentUser?.profile?.avatarUrl ?? '',
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
    <main className="relative flex min-h-screen flex-col items-center justify-center px-4 py-10 bg-[#0c1120] overflow-hidden">

      {/* Background chủ đề sinh viên: icons floating + orbs */}
      <OnboardingBackground />

      {/* Card chính với gradient border */}
      <div
        className="relative z-10 w-full max-w-[440px]"
        style={{
          animation: 'fadeInUp 0.4s ease-out both',
          background: 'linear-gradient(145deg, #7c3aed 0%, #4f46e5 50%, #2563eb 100%)',
          borderRadius: '24px',
          padding: '2px',
        }}
      >
        <div className="bg-white rounded-[22px] max-h-[85vh] flex flex-col py-2 pr-1.5 pl-0">
          <div className="overflow-y-scroll relative pl-7 pr-[10px] pt-5 pb-6 flex-1 custom-scrollbar-with-arrows flex flex-col">
            
            {/* Dummy element ép tràn cuộn: luôn dài 101% chiều cao khung chứa */}
            <div className="absolute top-0 left-0 w-[1px] h-[101%] pointer-events-none opacity-0" />

            <div className="flex-1 flex flex-col">
              {/* Header đồng bộ với AuthForm: logo căn giữa + tiêu đề to */}
              <div className="flex flex-col items-center text-center mb-6">
                <img src={logo} alt="UniShare" className="h-16 w-16 object-contain mb-3" />
                <h2 className="text-2xl font-bold text-gray-900 mb-1">
                  Thiết lập hồ sơ
                </h2>
                <p className="text-base text-gray-500">
                  Hoàn thiện thông tin để bắt đầu trải nghiệm
                </p>
              </div>

              {/* Thanh tiến trình bước */}
              <StepIndicator current={step} total={3} />

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
                  onAvatarChange={setField('avatarUrl')}
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
        </div>
      </div>
    </main>
  );
}
