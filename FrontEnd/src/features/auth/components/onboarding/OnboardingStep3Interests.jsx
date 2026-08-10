import InterestSelector from '../../../profile/components/InterestSelector.jsx';
import { ErrorMsg, PrimaryBtn, SecondaryBtn, SlidePanel, CheckIcon } from './OnboardingShared.jsx';

// Sở thích chỉ là dữ liệu personalization nền; bước này không chạy recommendation.
export default function OnboardingStep3Interests({ value, onChange, onSave, onSkip, onBack, error, isSubmitting }) {
  return (
    <SlidePanel stepKey="interests">
      <div className="mb-7">
        <h3 className="mb-2 text-[22px] font-semibold tracking-[-0.01em] text-zinc-900">Sở thích của bạn</h3>
        <p className="text-base leading-relaxed text-zinc-500">
          Chọn tối đa 10 chủ đề. Bạn cũng có thể bỏ qua và cập nhật sau.
        </p>
      </div>

      <InterestSelector value={value} onChange={onChange} disabled={isSubmitting} />
      <ErrorMsg msg={error} />

      <div className="mt-8 grid grid-cols-2 gap-3">
        <SecondaryBtn onClick={onBack} disabled={isSubmitting}>Quay lại</SecondaryBtn>
        <PrimaryBtn onClick={onSave} disabled={isSubmitting}>
          <span>{isSubmitting ? 'Đang lưu...' : 'Lưu & hoàn tất'}</span>
          {!isSubmitting ? <CheckIcon size={16} /> : null}
        </PrimaryBtn>
      </div>
      <button
        type="button"
        disabled={isSubmitting}
        onClick={onSkip}
        className="mt-4 w-full text-center text-sm font-semibold text-zinc-500 hover:text-zinc-900 disabled:opacity-50"
      >
        Bỏ qua bước này
      </button>
    </SlidePanel>
  );
}
