import AcademicProfileFields from '../../../profile/components/AcademicProfileFields.jsx';
import { ErrorMsg, PrimaryBtn, SecondaryBtn, SlidePanel, ArrowRightIcon } from './OnboardingShared.jsx';

// Academic là bước tùy chọn sau khi hồ sơ cơ bản đã hoàn tất, không tham gia auth gate.
export default function OnboardingStep2Academic({ value, onChange, onSave, onBack, error, isSubmitting }) {
  return (
    <SlidePanel stepKey="academic">
      <div className="mb-7">
        <h3 className="mb-2 text-[22px] font-semibold tracking-[-0.01em] text-zinc-900">Thông tin học tập</h3>
        <p className="text-base leading-relaxed text-zinc-500">
          Thông tin này là tùy chọn và có thể chỉnh sửa lại trong trang cá nhân.
        </p>
      </div>

      <div className="text-zinc-800">
        <AcademicProfileFields value={value} onChange={onChange} disabled={isSubmitting} idPrefix="onboarding-academic" />
      </div>
      <ErrorMsg msg={error} />

      <div className="mt-8 grid grid-cols-2 gap-3">
        <SecondaryBtn onClick={onBack} disabled={isSubmitting}>Quay lại</SecondaryBtn>
        <PrimaryBtn onClick={onSave} disabled={isSubmitting}>
          <span>{isSubmitting ? 'Đang lưu...' : 'Lưu & tiếp tục'}</span>
          {!isSubmitting ? <ArrowRightIcon size={18} /> : null}
        </PrimaryBtn>
      </div>
    </SlidePanel>
  );
}
