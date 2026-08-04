import logo from '../../../assets/brand/logo-dark.jpg';
import { SOCIAL_CONFLICT_ACTIONS, SOCIAL_CONFLICT_TYPES } from '../services/socialConflictService.js';

const ACTION_PRESENTATION = Object.freeze({
  [SOCIAL_CONFLICT_ACTIONS.CONTINUE_OTP]: { label: 'Tiếp tục xác minh OTP', primary: false },
  [SOCIAL_CONFLICT_ACTIONS.CANCEL_PENDING_AND_CONTINUE_SOCIAL]: { label: 'Hủy đăng ký đang chờ và tiếp tục', primary: true },
  [SOCIAL_CONFLICT_ACTIONS.LOGIN_EXISTING_ACCOUNT]: { label: 'Đăng nhập bằng phương thức hiện có', primary: true },
  [SOCIAL_CONFLICT_ACTIONS.START_ACCOUNT_RECOVERY]: { label: 'Khôi phục tài khoản', primary: false },
});

function conflictCopy(conflict) {
  const providerName = conflict.provider === 'FACEBOOK' ? 'Facebook' : 'Google';
  if (conflict.conflictType === SOCIAL_CONFLICT_TYPES.PENDING_EMAIL_MISMATCH) {
    return {
      title: 'Email đăng ký không trùng khớp',
      description: `Email từ ${providerName} khác với email đang chờ xác minh. Hãy chọn cách bạn muốn tiếp tục.`,
      warning: 'Hệ thống sẽ không tự gộp hai email hoặc hủy đăng ký đang chờ.',
    };
  }
  return {
    title: 'Tài khoản đã tồn tại',
    description: `Thông tin từ ${providerName} trùng với một tài khoản đang hoạt động nhưng provider chưa được liên kết.`,
    warning: 'Vui lòng dùng phương thức hiện có. Hệ thống không tự đăng nhập, liên kết hoặc gộp tài khoản.',
  };
}

export default function SocialConflictCard({ conflict, isResolving, isOutcomeUnknown, error, onAction, onBeginAgain }) {
  const copy = conflictCopy(conflict);

  return (
    <section className="px-4 py-4 text-center sm:px-8 sm:py-6">
      <img src={logo} alt="UniShare" className="mx-auto h-14 w-14 rounded-2xl object-cover shadow-sm" />
      <div className="mx-auto mt-4 flex h-12 w-12 items-center justify-center rounded-full bg-amber-100 text-xl font-bold text-amber-700" aria-hidden="true">!</div>
      <h1 className="mt-4 text-xl font-bold text-gray-900">{copy.title}</h1>
      <p className="mt-2 text-sm leading-6 text-gray-600">{copy.description}</p>

      {conflict.maskedPendingIdentifier ? (
        <p className="mt-3 rounded-lg bg-gray-50 px-3 py-2 text-sm text-gray-700">
          Đăng ký đang chờ: <strong>{conflict.maskedPendingIdentifier}</strong>
        </p>
      ) : null}

      <p className="mt-3 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-left text-xs leading-5 text-amber-800">{copy.warning}</p>
      {error ? <p role="alert" className="mt-4 rounded-lg border border-red-100 bg-red-50 px-3 py-2 text-left text-xs font-semibold leading-5 text-red-700">{error}</p> : null}

      {isOutcomeUnknown ? (
        <button type="button" onClick={onBeginAgain} className="mt-5 h-11 w-full rounded-full bg-slate-900 px-5 text-sm font-semibold text-white hover:bg-black">
          Bắt đầu lại đăng nhập social
        </button>
      ) : (
        <div className="mt-5 space-y-3">
          {conflict.allowedActions.map((action) => {
            const presentation = ACTION_PRESENTATION[action];
            if (!presentation) return null;
            return (
              <button
                key={action}
                type="button"
                disabled={isResolving}
                onClick={() => onAction(action)}
                className={`min-h-11 w-full rounded-full px-4 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-60 ${presentation.primary ? 'bg-slate-900 text-white hover:bg-black' : 'border border-gray-300 bg-white text-gray-800 hover:bg-gray-50'}`}
              >
                {isResolving ? 'Đang xử lý...' : presentation.label}
              </button>
            );
          })}
        </div>
      )}
    </section>
  );
}
