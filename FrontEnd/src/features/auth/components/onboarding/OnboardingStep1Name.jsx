import { inputCls, inputStyle } from './onboardingUtils.js';
import { ErrorMsg, PrimaryBtn, SlidePanel, UserIcon, ArrowRightIcon, CheckIcon } from './OnboardingShared.jsx';

const AVAILABILITY_VIEW = {
  checking: { text: 'Đang kiểm tra...', className: 'text-zinc-500' },
  available: { text: 'Tên người dùng có thể sử dụng.', className: 'text-emerald-600' },
  exists: { text: 'Tên người dùng đã tồn tại.', className: 'text-red-600' },
  invalid: { text: 'Tên người dùng không đúng định dạng.', className: 'text-red-600' },
  reserved: { text: 'Tên người dùng này được dành riêng.', className: 'text-red-600' },
  error: { text: 'Chưa thể kiểm tra tên người dùng. Vui lòng thử lại.', className: 'text-amber-600' },
};

// Bước 1 thu thập định danh công khai; ký tự @ chỉ là prefix trình bày, không thuộc giá trị form.
export default function OnboardingStep1Name({
  displayName,
  username,
  onDisplayNameChange,
  onUsernameChange,
  onNext,
  error,
  usernameError,
  availabilityStatus,
  canContinue,
  usernameLocked = false,
}) {
  const displayNameValid = displayName.trim().length >= 2;
  const availabilityView = AVAILABILITY_VIEW[availabilityStatus];

  return (
    <SlidePanel stepKey={1}>
      <div className="mb-7">
        <h3 className="mb-2 text-[22px] font-semibold tracking-[-0.01em] text-zinc-900">
          Bạn muốn mọi người gọi bạn là gì?
        </h3>
        <p className="text-base leading-relaxed text-zinc-500">
          Tên hiển thị và tên người dùng sẽ xuất hiện trên hồ sơ của bạn.
        </p>
      </div>

      <div className="space-y-5">
        <div>
          <label className="mb-2 block text-sm font-medium text-zinc-800" htmlFor="onboarding-display-name">
            Tên hiển thị
          </label>
          <div className="relative flex items-center">
            <div className="pointer-events-none absolute left-4 text-zinc-400"><UserIcon size={19} /></div>
            <input
              id="onboarding-display-name"
              value={displayName}
              onChange={(event) => onDisplayNameChange(event.target.value)}
              placeholder="VD: Nguyễn Văn An"
              className={`${inputCls} h-13 pl-11 pr-11 text-base shadow-xs`}
              style={inputStyle}
              autoFocus
              maxLength={100}
            />
            {displayNameValid ? (
              <div className="absolute right-3.5 flex h-6 w-6 items-center justify-center rounded-full bg-emerald-500 text-white">
                <CheckIcon size={12} />
              </div>
            ) : null}
          </div>
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium text-zinc-800" htmlFor="onboarding-username">
            Tên người dùng
          </label>
          <div className="relative flex items-center">
            <span className="pointer-events-none absolute left-4 text-base font-medium text-zinc-500" aria-hidden="true">@</span>
            <input
              id="onboarding-username"
              value={username}
              disabled={usernameLocked}
              onChange={(event) => onUsernameChange(event.target.value)}
              onKeyDown={(event) => event.key === 'Enter' && canContinue && onNext()}
              placeholder="duoz_03"
              className={`${inputCls} h-13 pl-9 pr-11 text-base shadow-xs`}
              style={inputStyle}
              autoComplete="username"
              maxLength={31}
              aria-describedby="username-helper username-status"
              aria-invalid={Boolean(usernameError) || ['exists', 'invalid', 'reserved'].includes(availabilityStatus)}
            />
            {availabilityStatus === 'available' ? (
              <div className="absolute right-3.5 flex h-6 w-6 items-center justify-center rounded-full bg-emerald-500 text-white">
                <CheckIcon size={12} />
              </div>
            ) : null}
          </div>
          <p id="username-helper" className="mt-2 text-xs text-zinc-400">
            {usernameLocked
              ? 'Tên người dùng không thể thay đổi sau khi hồ sơ cơ bản đã hoàn tất.'
              : '3–30 ký tự, chỉ gồm chữ thường, số, dấu chấm và gạch dưới.'}
          </p>
          <div id="username-status" className="mt-1 min-h-5 text-xs" aria-live="polite">
            {usernameError ? <span className="text-red-600">{usernameError}</span> : null}
            {!usernameError && availabilityView ? (
              <span className={availabilityView.className}>{availabilityView.text}</span>
            ) : null}
          </div>
        </div>
      </div>

      <ErrorMsg msg={error} />

      <div className="mt-8">
        <PrimaryBtn onClick={onNext} disabled={!canContinue}>
          <span>Tiếp tục</span>
          <ArrowRightIcon size={18} />
        </PrimaryBtn>
      </div>
    </SlidePanel>
  );
}
