import { inputCls } from './onboardingUtils.js';
import { ErrorMsg, PrimaryBtn, SlidePanel } from './OnboardingShared.jsx';

// Bước 1: Nhập tên hiển thị — bước đầu tiên, không có nút quay lại
export default function OnboardingStep1Name({ displayName, onChange, onNext, error }) {
  return (
    <SlidePanel stepKey={1}>
      <div className="mb-6">
        <h3 className="text-xl font-bold text-gray-900 mb-2">
          Bạn muốn được gọi là gì?
        </h3>
        <p className="text-base text-gray-500">
          Tên hiển thị sẽ xuất hiện trên hồ sơ và mọi bài viết của bạn.
        </p>
      </div>

      <div>
        <label className="block text-sm font-semibold text-gray-700 mb-2">
          Tên hiển thị
          <span className="ml-2 text-sm font-normal text-red-500">Bắt buộc</span>
        </label>
        <input
          value={displayName}
          onChange={(e) => onChange(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && onNext()}
          placeholder="VD: Nguyễn Văn An"
          className={inputCls + ' h-12 text-base !text-black'}
          autoFocus
          maxLength={50}
        />
        {displayName.length > 0 && (
          <p className={`text-right text-xs mt-2 ${displayName.length > 40 ? 'text-amber-500' : 'text-gray-400'}`}>
            {displayName.length}/50
          </p>
        )}
      </div>

      <ErrorMsg msg={error} />
      <PrimaryBtn onClick={onNext}>Tiếp tục</PrimaryBtn>
    </SlidePanel>
  );
}
