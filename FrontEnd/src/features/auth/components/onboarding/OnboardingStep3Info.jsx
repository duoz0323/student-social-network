import { inputCls, todayIsoDate } from './onboardingUtils.js';
import LogoLoader from '../../../../components/common/LogoLoader.jsx';
import { ErrorMsg, PrimaryBtn, SecondaryBtn, SlidePanel } from './OnboardingShared.jsx';

// Bước 3: Ngày sinh (bắt buộc, ≥18 tuổi) và bio (tùy chọn)
// Chỉ có 2 nút: Hoàn tất và Quay lại
export default function OnboardingStep3Info({ dateOfBirth, bio, onDateChange, onBioChange, onFinish, onBack, error, isSubmitting }) {
  return (
    <SlidePanel stepKey={3}>
      <div className="mb-6">
        <h3 className="text-xl font-bold text-gray-900 mb-2">
          Thêm thông tin cá nhân
        </h3>
        <p className="text-base text-gray-500">
          Ngày sinh bắt buộc — bạn phải đủ 18 tuổi để tham gia UniShare.
        </p>
      </div>

      <div className="space-y-5">
        {/* Ngày sinh – bắt buộc */}
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">
            Ngày sinh
            <span className="ml-2 text-sm font-normal text-red-500">Bắt buộc</span>
          </label>
          <input
            type="date"
            value={dateOfBirth}
            max={todayIsoDate()}
            onChange={(e) => onDateChange(e.target.value)}
            className={inputCls + ' h-12 text-base'}
          />
          {dateOfBirth && (
            <p className="mt-2 text-sm text-gray-400">
              Ngày sinh sẽ không hiển thị công khai.
            </p>
          )}
        </div>

        {/* Bio – tùy chọn */}
        <div>
          <div className="flex items-baseline justify-between mb-2">
            <label className="text-sm font-semibold text-gray-700">
              Giới thiệu bản thân
            </label>
            <span className="text-sm text-gray-400">Tùy chọn</span>
          </div>
          <textarea
            value={bio}
            onChange={(e) => onBioChange(e.target.value)}
            placeholder="Bạn học ngành gì? Sở thích? Câu lạc bộ?..."
            rows={2}
            maxLength={300}
            className={inputCls + ' py-3 text-base resize-none custom-scrollbar-with-arrows'}
          />
          <p className={`text-right text-xs mt-2 ${bio.length > 260 ? 'text-amber-500' : 'text-gray-400'}`}>
            {bio.length}/300
          </p>
        </div>
      </div>

      <ErrorMsg msg={error} />

      {/* Chỉ 2 nút: Hoàn tất và Quay lại */}
      <PrimaryBtn onClick={onFinish} disabled={isSubmitting}>
        {isSubmitting ? <LogoLoader size="sm" message="Đang lưu hồ sơ..." /> : 'Hoàn tất hồ sơ'}
      </PrimaryBtn>
      <SecondaryBtn onClick={onBack} disabled={isSubmitting}>Quay lại</SecondaryBtn>
    </SlidePanel>
  );
}
