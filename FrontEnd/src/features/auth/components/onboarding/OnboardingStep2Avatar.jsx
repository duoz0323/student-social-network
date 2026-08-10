import { useRef } from 'react';
import { CameraIcon, SlidePanel, PrimaryBtn, SecondaryBtn, ArrowRightIcon, UserIcon } from './OnboardingShared.jsx';

// Bước 2: Chọn ảnh đại diện
export default function OnboardingStep2Avatar({ avatarUrl, onAvatarChange, onNext, onBack }) {
  const fileInputRef = useRef(null);

  function handleFileChange(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    // Giữ File để upload thật ở lúc hoàn tất, còn data URL chỉ phục vụ preview tại bước này.
    reader.onload = () => onAvatarChange(file, String(reader.result));
    reader.readAsDataURL(file);
  }

  return (
    <SlidePanel stepKey={2}>
      <div className="mb-7">
        <h3 className="mb-2 text-[22px] font-semibold tracking-[-0.01em] text-zinc-900">
          Thêm ảnh đại diện
        </h3>
        <p className="text-base text-zinc-500 leading-relaxed">
          Giúp bạn bè nhận ra bạn dễ hơn.<br />
          Bạn có thể thêm hoặc đổi ảnh sau.
        </p>
      </div>

      {/* Vùng xem trước avatar theo mockup */}
      <div className="my-6 flex flex-col items-center">
        <div
          className="group relative mb-5 cursor-pointer"
          onClick={() => fileInputRef.current?.click()}
        >
          <div 
            className="flex h-32 w-32 select-none items-center justify-center overflow-hidden rounded-full shadow-md ring-4"
            style={{ background: 'var(--auth-avatar-placeholder-bg)', '--tw-ring-color': 'var(--auth-avatar-ring)' }}
          >
            {avatarUrl ? (
              <img src={avatarUrl} alt="Ảnh đại diện" className="h-full w-full object-cover" />
            ) : (
              <div className="flex items-center justify-center" style={{ color: 'var(--auth-avatar-placeholder-text)' }}>
                <UserIcon size={64} />
              </div>
            )}
          </div>
          
          {/* Badge camera ở góc dưới bên phải avatar */}
          <div 
            className="absolute bottom-1 right-1 flex h-8 w-8 items-center justify-center rounded-full text-white shadow-sm ring-2 transition-transform group-hover:scale-110"
            style={{ background: 'var(--auth-avatar-badge-bg)', '--tw-ring-color': 'var(--auth-avatar-ring)' }}
          >
            <CameraIcon size={14} />
          </div>
        </div>

        {/* Input file ẩn */}
        <input ref={fileInputRef} type="file" accept="image/*" className="sr-only" onChange={handleFileChange} />

        {/* Nút chọn ảnh từ thiết bị */}
        <div className="w-full max-w-[280px]">
          <SecondaryBtn onClick={() => fileInputRef.current?.click()} className="!h-11 text-sm">
            <CameraIcon size={16} />
            <span>Chọn ảnh từ thiết bị</span>
          </SecondaryBtn>
        </div>
      </div>

      {/* Hàng 2 nút dưới cùng: Quay lại + Tiếp tục theo đúng mockup */}
      <div className="mt-8 grid grid-cols-2 gap-3">
        <SecondaryBtn onClick={onBack}>
          <span>Quay lại</span>
        </SecondaryBtn>

        <PrimaryBtn onClick={onNext}>
          <span>Tiếp tục</span>
          <ArrowRightIcon size={18} />
        </PrimaryBtn>
      </div>
    </SlidePanel>
  );
}
