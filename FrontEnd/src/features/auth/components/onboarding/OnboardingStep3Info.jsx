import {
  inputCls,
  inputStyle,
  todayIsoDate,
} from './onboardingUtils.js';
import { ErrorMsg, PrimaryBtn, SecondaryBtn, SlidePanel, CalendarIcon, CheckIcon } from './OnboardingShared.jsx';

// Bước 3: Ngày sinh & Bio
export default function OnboardingStep3Info({
  dateOfBirth,
  bio,
  onDateChange,
  onBioChange,
  onFinish,
  onBack,
  error,
  dateError,
  isSubmitting,
}) {
  return (
    <SlidePanel stepKey={3}>
      <div className="mb-7">
        <h3 className="mb-2 text-[22px] font-semibold tracking-[-0.01em] text-zinc-900">
          Thêm thông tin cá nhân
        </h3>
        <p className="text-base text-zinc-500 leading-relaxed">
          Ngày sinh bắt buộc — bạn phải đủ 18 tuổi để tham gia UniShare.
        </p>
      </div>

      <div className="space-y-5">
        {/* Ngày sinh */}
        <div>
          <label className="block mb-2 text-sm font-medium text-zinc-800">
            Ngày sinh
          </label>
          <div className="relative flex items-center">
            <div className="pointer-events-none absolute left-4 text-zinc-400">
              <CalendarIcon size={19} />
            </div>
            <input
              id="onboarding-date-of-birth"
              type="date"
              autoComplete="bday"
              value={dateOfBirth}
              max={todayIsoDate()}
              onChange={(event) => onDateChange(event.target.value)}
              className={`${inputCls} h-13 pl-11 text-base shadow-xs`}
              style={inputStyle}
              aria-describedby="onboarding-date-of-birth-error"
              aria-invalid={Boolean(dateError)}
            />
          </div>
          {dateError ? (
            <p id="onboarding-date-of-birth-error" className="mt-2 text-sm font-medium text-red-600" role="alert">
              {dateError}
            </p>
          ) : null}
        </div>

        {/* Bio */}
        <div>
          <div className="mb-2 flex items-baseline justify-between">
            <label className="text-sm font-medium text-zinc-800">
              Giới thiệu bản thân <span className="font-normal text-zinc-400">(tùy chọn)</span>
            </label>
          </div>
          <textarea
            value={bio}
            onChange={(e) => onBioChange(e.target.value)}
            placeholder="Bạn học ngành gì? Sở thích? Câu lạc bộ?..."
            rows={3}
            maxLength={300}
            className={`${inputCls} py-3 text-base resize-none shadow-xs custom-scrollbar-with-arrows`}
            style={inputStyle}
          />
          <p className={`text-right text-xs mt-1.5 ${bio.length > 260 ? 'text-amber-500' : 'text-zinc-400'}`}>
            {bio.length}/300
          </p>
        </div>
      </div>

      <ErrorMsg msg={error} />

      {/* Hàng 2 nút dưới cùng: Quay lại + Hoàn tất theo đúng mockup */}
      <div className="mt-8 grid grid-cols-2 gap-3">
        <SecondaryBtn onClick={onBack} disabled={isSubmitting}>
          <span>Quay lại</span>
        </SecondaryBtn>

        <PrimaryBtn onClick={onFinish} disabled={isSubmitting}>
          <span>{isSubmitting ? 'Đang lưu...' : 'Hoàn tất'}</span>
          {!isSubmitting && <CheckIcon size={16} />}
        </PrimaryBtn>
      </div>
    </SlidePanel>
  );
}
