import Button from '../../../components/common/Button.jsx';

export default function RecoveryEmailNotice({ email, disabled, onContinue, onRestart }) {
  return (
    <div className="px-7 py-8 sm:px-10">
      <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-violet-50 text-violet-700" aria-hidden="true">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <rect width="20" height="16" x="2" y="4" rx="2" />
          <path d="m22 7-10 6L2 7" />
        </svg>
      </div>

      <div className="mt-4 text-center">
        <h1 className="text-[1.35rem] font-bold text-gray-900">Kiểm tra email của bạn</h1>
        <p className="mt-2 text-[13px] leading-5 text-gray-500">
          Nếu <span className="font-semibold text-gray-700">{email}</span> thuộc tài khoản đủ điều kiện,
          UniShare đã gửi một mã xác minh.
        </p>
      </div>

      <div className="mt-5 rounded-lg border border-gray-200 bg-gray-50 px-4 py-3 text-[12px] leading-5 text-gray-600">
        Hãy kiểm tra cả hộp thư rác. Nếu không nhận được email, bạn có thể quay lại kiểm tra địa chỉ đã nhập.
      </div>

      <div className="mt-6 space-y-3">
        <Button type="button" disabled={disabled} onClick={onContinue} className="w-full">
          Tôi đã nhận được mã
        </Button>
        <Button type="button" disabled={disabled} onClick={onRestart} variant="secondary" className="w-full">
          Nhập lại email
        </Button>
      </div>
    </div>
  );
}
