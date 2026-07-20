import Button from '../../../components/common/Button.jsx';
import GoogleAuthButton from './GoogleAuthButton.jsx';

export default function SocialAuthButtons({ onUnavailable, onGoogleAuthenticated, onGoogleConflict, includeRegistrationFlow = false, actionLabel = 'Tiếp tục với' }) {
  function clickSocial(providerName) {
    // Facebook được giữ nguyên trạng thái chờ cho tới giai đoạn tích hợp riêng.
    onUnavailable(providerName);
  }

  return (
    <div className="space-y-3">
      <GoogleAuthButton includeRegistrationFlow={includeRegistrationFlow} onAuthenticated={onGoogleAuthenticated} onConflict={onGoogleConflict} />
      <Button type="button" variant="secondary" className="min-h-[44px] w-full gap-3" onClick={() => clickSocial('Facebook')}>
        <span className="font-black text-blue-700">f</span>
        {actionLabel} Facebook
      </Button>
    </div>
  );
}
