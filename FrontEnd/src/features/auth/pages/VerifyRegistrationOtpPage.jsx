import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import logo from '../../../assets/brand/logo.png';
import Button from '../../../components/common/Button.jsx';
import OtpCountdown from '../components/OtpCountdown.jsx';
import OtpInput from '../components/OtpInput.jsx';
import { useRegistration } from '../hooks/useRegistration.js';
import { getRegistrationErrorMessage, isTerminalRegistrationError } from '../utils/registrationErrorMapper.js';
import { validateOtp } from '../validation/registrationValidation.js';
import GoogleAuthButton from '../components/GoogleAuthButton.jsx';
import { getAuthenticatedHome } from '../utils/authNavigation.js';

export default function VerifyRegistrationOtpPage() {
  const registration = useRegistration();
  const navigate = useNavigate();
  const restoredRef = useRef(false);
  const [otp, setOtp] = useState('');
  const [otpError, setOtpError] = useState('');
  const [message, setMessage] = useState('');
  const [countdown, setCountdown] = useState(1);

  useEffect(() => {
    if (restoredRef.current) return;
    restoredRef.current = true;
    if (!registration.hasFlow) {
      navigate('/register', { replace: true, state: { reason: 'MISSING_REGISTRATION_FLOW' } });
      return;
    }
    registration.restoreFlow()
      .then((flow) => {
        if (!flow || flow.terminal) navigate('/register', { replace: true, state: { reason: 'REGISTRATION_TERMINAL' } });
      })
      .catch((error) => {
        setMessage(getRegistrationErrorMessage(error));
        if (isTerminalRegistrationError(error)) navigate('/register', { replace: true, state: { reason: error.code } });
      });
  }, [navigate, registration]);

  async function verify(event) {
    event.preventDefault();
    const validationError = validateOtp(otp);
    if (validationError) {
      setOtpError(validationError);
      return;
    }
    setOtpError('');
    setMessage('');
    try {
      const session = await registration.verifyOtp(otp);
      if (!session) return;
      setOtp('');
      navigate('/onboarding/profile', { replace: true });
    } catch (error) {
      setOtp('');
      setOtpError(getRegistrationErrorMessage(error));
      if (isTerminalRegistrationError(error)) navigate('/register', { replace: true, state: { reason: error.code } });
    }
  }

  async function resend() {
    if (countdown > 0 || registration.isResending) return;
    setMessage('');
    try {
      const flow = await registration.resendOtp();
      if (!flow) return;
      setOtp('');
      setOtpError('');
      setMessage('Mã OTP mới đã được gửi. Mã cũ không còn hiệu lực.');
    } catch (error) {
      setMessage(getRegistrationErrorMessage(error));
    }
  }

  async function cancel() {
    setMessage('');
    try {
      const cancelled = await registration.cancelRegistration();
      if (!cancelled) return;
      navigate('/register', { replace: true, state: { reason: 'REGISTRATION_CANCELLED' } });
    } catch (error) {
      setMessage(getRegistrationErrorMessage(error));
    }
  }

  const updateCountdown = useCallback((value) => setCountdown(value), []);
  const busy = registration.isVerifying || registration.isResending || registration.isCancelling || registration.isRestoring;

  return (
    <main className="auth-pattern flex min-h-screen items-center justify-center px-4 py-8">
      <form onSubmit={verify} className="stitch-card-shadow w-full max-w-[420px] rounded-[10px] border border-[var(--app-border)] bg-white px-9 py-8">
        <img src={logo} alt="UniShare" className="mx-auto h-14 w-14 object-contain" />
        <h1 className="mt-4 text-center text-2xl font-black">Xác minh đăng ký</h1>
        <p className="mt-2 text-center text-sm leading-6 text-[var(--app-muted)]">
          Nhập mã OTP đã gửi đến <strong className="text-zinc-800">{registration.maskedIdentifier || 'định danh của bạn'}</strong>.
        </p>
        {registration.otpExpiresAt ? <p className="mt-1 text-center text-xs text-zinc-500">Mã hiện tại hết hạn lúc {new Date(registration.otpExpiresAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}.</p> : null}

        <div className="mt-7">
          <OtpInput value={otp} onChange={setOtp} disabled={busy} error={otpError} />
        </div>
        {registration.remainingAttempts !== null ? <p className="mt-2 text-xs text-zinc-500">Còn {registration.remainingAttempts} lần nhập.</p> : null}
        {message ? <p className="mt-4 rounded-xl bg-zinc-100 px-3 py-2 text-sm font-semibold text-zinc-700">{message}</p> : null}

        <Button type="submit" disabled={busy || otp.length !== 6} className="mt-5 min-h-[48px] w-full font-black">
          {registration.isVerifying ? 'Đang xác minh...' : 'Xác minh OTP'}
        </Button>

        <div className="mt-5 text-center text-sm text-zinc-600">
          <OtpCountdown resendAvailableAt={registration.resendAvailableAt} onChange={updateCountdown} />
          <button type="button" disabled={busy || countdown > 0} onClick={resend} className="mt-2 block w-full font-black text-zinc-900 disabled:text-zinc-400">
            {registration.isResending ? 'Đang gửi lại...' : 'Gửi lại OTP'}
          </button>
        </div>

        <div className="mt-6 flex justify-between text-sm font-bold">
          <Link to="/register" className="text-zinc-600">Quay lại đăng ký</Link>
          <button type="button" onClick={cancel} disabled={busy} className="text-red-700 disabled:text-zinc-400">Hủy đăng ký</button>
        </div>

        <div className="my-5 flex items-center gap-3 text-xs text-zinc-500"><span className="h-px flex-1 bg-zinc-200" />Hoặc<span className="h-px flex-1 bg-zinc-200" /></div>
        <GoogleAuthButton
          includeRegistrationFlow
          onAuthenticated={(session) => navigate(getAuthenticatedHome(session), { replace: true })}
          onConflict={() => navigate('/auth/social-conflict', { replace: true })}
        />
      </form>
    </main>
  );
}
