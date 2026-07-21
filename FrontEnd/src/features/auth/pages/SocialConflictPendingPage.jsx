import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout.jsx';
import SocialConflictCard from '../components/SocialConflictCard.jsx';
import { useRegistration } from '../hooks/useRegistration.js';
import { useSocialConflict } from '../hooks/useSocialConflict.js';
import { getAuthenticatedHome } from '../utils/authNavigation.js';

export default function SocialConflictPendingPage() {
  const navigate = useNavigate();
  const registration = useRegistration();
  const socialConflict = useSocialConflict();

  useEffect(() => {
    if (socialConflict.conflict) return;
    const target = registration.hasFlow ? '/register' : '/login';
    navigate(target, {
      replace: true,
      state: { reason: socialConflict.restoreReason === 'EXPIRED' ? 'SOCIAL_CONFLICT_EXPIRED' : 'SOCIAL_CONFLICT_UNAVAILABLE' },
    });
  }, [navigate, registration.hasFlow, socialConflict.conflict, socialConflict.restoreReason]);

  async function chooseAction(action) {
    const result = await socialConflict.resolveAction(action);
    if (result?.type === 'CONTINUE_OTP') navigate('/register/verify', { replace: true });
    if (result?.type === 'AUTH_SUCCESS') navigate(getAuthenticatedHome(result.session), { replace: true });
    if (result?.type === 'LOGIN_EXISTING_ACCOUNT') navigate('/login', { replace: true, state: { reason: 'USE_EXISTING_AUTH_METHOD' } });
  }

  function beginAgain() {
    const target = socialConflict.beginAgain() === 'REGISTER' || registration.hasFlow ? '/register' : '/login';
    navigate(target, { replace: true, state: { reason: 'SOCIAL_CONFLICT_OUTCOME_UNKNOWN' } });
  }

  if (!socialConflict.conflict) return null;
  return (
    <AuthLayout>
      <SocialConflictCard
        conflict={socialConflict.conflict}
        isResolving={socialConflict.isResolving}
        isOutcomeUnknown={socialConflict.isOutcomeUnknown}
        error={socialConflict.error}
        onAction={chooseAction}
        onBeginAgain={beginAgain}
      />
    </AuthLayout>
  );
}
