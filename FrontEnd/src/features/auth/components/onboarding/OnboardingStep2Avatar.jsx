import { useRef } from 'react';
import { CameraIcon, SlidePanel, PrimaryBtn, SecondaryBtn } from './OnboardingShared.jsx';

// Bước 2: Chọn ảnh đại diện (tùy chọn)
// Chỉ có 2 nút: Tiếp tục (chuyển bước 3) và Quay lại (về bước 1)
// Ảnh là tùy chọn — người dùng bấm Tiếp tục không chọn ảnh vẫn được
export default function OnboardingStep2Avatar({ avatarUrl, displayName, onAvatarChange, onNext, onBack }) {
  const fileInputRef = useRef(null);

  // Initials từ displayName để hiển thị khi chưa chọn ảnh
  const initials = displayName
    ? displayName.split(' ').map((w) => w[0]).join('').slice(0, 2).toUpperCase()
    : 'U';

  // Đọc file và tạo data URL preview (mock — không upload API trong MVP)
  function handleFileChange(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => onAvatarChange(String(reader.result));
    reader.readAsDataURL(file);
  }

  return (
    <SlidePanel stepKey={2}>
      <div className="mb-6">
        <h3 className="text-xl font-bold text-gray-900 mb-2">
          Thêm ảnh đại diện
        </h3>
        <p className="text-base text-gray-500">
          Giúp bạn bè nhận ra bạn dễ hơn. Bạn có thể thêm sau từ hồ sơ.
        </p>
      </div>

      {/* Vùng avatar */}
      <div className="flex flex-col items-center mb-6">
        {/* Avatar click để chọn ảnh */}
        <div
          className="relative group cursor-pointer mb-5"
          onClick={() => fileInputRef.current?.click()}
        >
          <div className={[
            'p-1 rounded-full transition-all duration-300',
            avatarUrl
              ? 'bg-gradient-to-br from-violet-500 via-blue-500 to-indigo-500 shadow-md shadow-violet-200'
              : 'border-2 border-dashed border-gray-300',
          ].join(' ')}>
            <div className="h-28 w-28 rounded-full overflow-hidden bg-violet-50 flex items-center justify-center select-none">
              {avatarUrl ? (
                <img src={avatarUrl} alt="Ảnh đại diện" className="h-full w-full object-cover" />
              ) : (
                <span className="text-4xl font-black text-violet-300">{initials}</span>
              )}
            </div>
          </div>
          {/* Overlay khi hover */}
          <div className="absolute inset-0 rounded-full bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center text-white">
            <CameraIcon />
          </div>
        </div>

        {/* Input file ẩn */}
        <input ref={fileInputRef} type="file" accept="image/*" className="sr-only" onChange={handleFileChange} />

        {/* Nút hành động với ảnh */}
        {avatarUrl ? (
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="flex items-center gap-1.5 rounded-xl border border-violet-200 bg-violet-50 px-5 py-2.5 text-sm font-semibold text-violet-700 hover:bg-violet-100 transition-colors"
            >
              <CameraIcon />
              Đổi ảnh
            </button>
            <button
              type="button"
              onClick={() => onAvatarChange('')}
              className="rounded-xl border border-gray-200 px-5 py-2.5 text-sm font-medium text-gray-600 hover:border-red-200 hover:text-red-500 transition-colors"
            >
              Xóa
            </button>
          </div>
        ) : (
          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            className="flex items-center gap-2 rounded-xl border-2 border-dashed border-gray-200 px-6 py-3 text-sm font-medium text-gray-600 hover:border-violet-300 hover:text-violet-600 transition-colors"
          >
            <CameraIcon />
            Chọn ảnh từ thiết bị
          </button>
        )}
      </div>

      {/* Nút Tiếp tục */}
      <PrimaryBtn onClick={onNext}>Tiếp tục</PrimaryBtn>

      {/* Nút Quay lại – phân cách rõ với Tiếp tục */}
      <SecondaryBtn onClick={onBack}>Quay lại</SecondaryBtn>
    </SlidePanel>
  );
}
