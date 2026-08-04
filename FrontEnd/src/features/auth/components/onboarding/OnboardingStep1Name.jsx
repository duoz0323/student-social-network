import { inputCls, inputStyle } from './onboardingUtils.js';
import { ErrorMsg, PrimaryBtn, SlidePanel, UserIcon, ArrowRightIcon, CheckIcon } from './OnboardingShared.jsx';

// Bước 1: Nhập tên hiển thị
export default function OnboardingStep1Name({ displayName, onChange, onNext, error }) {
  const isValid = displayName.trim().length >= 2;

  return (
    <SlidePanel stepKey={1}>
      <div className="mb-7">
        <h3 className="mb-2 text-[22px] font-semibold tracking-[-0.01em] text-zinc-900">
          Bạn muốn mọi người gọi bạn là gì?
        </h3>
        <p className="text-base text-zinc-500 leading-relaxed">
          Tên hiển thị sẽ xuất hiện trên hồ sơ và mọi bài viết của bạn.
        </p>
      </div>

      <div className="mb-6">
        <label className="block mb-2 text-sm font-medium text-zinc-800">
          Tên hiển thị
        </label>

        {/* Input bọc icon theo đúng mockup */}
        <div className="relative flex items-center">
          <div className="pointer-events-none absolute left-4 text-zinc-400">
            <UserIcon size={19} />
          </div>
          <input
            value={displayName}
            onChange={(e) => onChange(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && onNext()}
            placeholder="VD: Nguyễn Văn An"
            className={`${inputCls} h-13 pl-11 pr-11 text-base shadow-xs`}
            style={inputStyle}
            autoFocus
            maxLength={50}
          />
          {isValid && (
            <div className="absolute right-3.5 flex h-6 w-6 items-center justify-center rounded-full bg-emerald-500 text-white">
              <CheckIcon size={12} />
            </div>
          )}
        </div>

        <p className="mt-2.5 text-xs text-zinc-400">
          Bạn có thể thay đổi tên hiển thị bất cứ lúc nào.
        </p>
      </div>

      <ErrorMsg msg={error} />

      {/* Nút Tiếp tục */}
      <div className="mt-8">
        <PrimaryBtn onClick={onNext}>
          <span>Tiếp tục</span>
          <ArrowRightIcon size={18} />
        </PrimaryBtn>
      </div>
    </SlidePanel>
  );
}
