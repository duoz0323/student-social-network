import GoogleAuthButton from './GoogleAuthButton.jsx';
import FacebookAuthButton from './FacebookAuthButton.jsx';

export default function SocialAuthButtons({
  onGoogleAuthenticated,
  onGoogleConflict,
  onFacebookAuthenticated,
  onFacebookConflict,
  includeRegistrationFlow = false,
  actionLabel = 'Tiếp tục với',
}) {
  return (
    <div className="space-y-2">
      <GoogleAuthButton
        includeRegistrationFlow={includeRegistrationFlow}
        onAuthenticated={onGoogleAuthenticated}
        onConflict={onGoogleConflict}
        actionLabel={actionLabel}
      />
      <FacebookAuthButton
        includeRegistrationFlow={includeRegistrationFlow}
        onAuthenticated={onFacebookAuthenticated}
        onConflict={onFacebookConflict}
        actionLabel={actionLabel}
      />
    </div>
  );
}
