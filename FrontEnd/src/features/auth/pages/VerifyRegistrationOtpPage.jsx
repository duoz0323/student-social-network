import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../../../components/common/Button.jsx';
import AuthLayout from '../components/AuthLayout.jsx';
import GoogleAuthButton from '../components/GoogleAuthButton.jsx';
import OtpCountdown from '../components/OtpCountdown.jsx';
import OtpInput from '../components/OtpInput.jsx';
import { useRegistration } from '../hooks/useRegistration.js';
import { getAuthenticatedHome } from '../utils/authNavigation.js';
import { getRegistrationErrorMessage, isTerminalRegistrationError } from '../utils/registrationErrorMapper.js';
import { validateOtp } from '../validation/registrationValidation.js';

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
        else if (flow.resumed) {
          setMessage('Đang tiếp tục phiên đăng ký cũ. Hệ thống không tự gửi lại OTP; nếu chưa nhận được mã, hãy bấm “Gửi lại mã” khi thời gian chờ kết thúc.');
        }
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
      navigate(getAuthenticatedHome(session), { replace: true });
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
      setMessage('Mã OTP mới đã được gửi.');
    } catch (error) {
      setMessage(getRegistrationErrorMessage(error));
    }
  }

  async function cancel() {
    setMessage('');
    try {
      if (await registration.cancelRegistration()) {
        navigate('/register', { replace: true, state: { reason: 'REGISTRATION_CANCELLED' } });
      }
    } catch (error) {
      setMessage(getRegistrationErrorMessage(error));
    }
  }

  const updateCountdown = useCallback((value) => setCountdown(value), []);
  const busy = registration.isVerifying || registration.isResending || registration.isCancelling || registration.isRestoring;

  const displayMessage = message || otpError;
  const isSuccessMessage = message && message.includes('đã được gửi');
  const isInformationMessage = message && message.includes('phiên đăng ký cũ');

  return (
    <AuthLayout>
      <form onSubmit={verify} className="px-7 sm:px-10 py-8">
        <div className="mb-6 text-center">
          <h1 className="text-[1.35rem] font-bold text-gray-900 mb-2">
            Nhập mã xác minh
          </h1>
          <p className="text-[13px] text-gray-500 font-medium">
            Vui lòng nhập mã gồm 6 chữ số từ email xác minh.
          </p>
          {registration.otpExpiresAt ? (
            <p className="mt-1 text-[12px] text-gray-400 font-medium">
              Mã hết hạn lúc {new Date(registration.otpExpiresAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}.
            </p>
          ) : null}
        </div>

        <div className={`overflow-hidden transition-all duration-300 ease-in-out ${displayMessage ? 'max-h-24 opacity-100 mb-5' : 'max-h-0 opacity-0 mb-0'}`}>
          <p className={`rounded-lg p-3 text-[13px] font-medium text-center border ${isSuccessMessage ? 'bg-green-50 text-green-700 border-green-100' : isInformationMessage ? 'bg-blue-50 text-blue-700 border-blue-100' : 'bg-red-50 text-red-600 border-red-100'}`}>
            {displayMessage}
          </p>
        </div>

        <div className="mb-6">
          <OtpInput value={otp} onChange={(value) => { setOtp(value); setOtpError(''); setMessage(''); }} disabled={busy} error={otpError} />
        </div>
        
        <div className="mb-2">
          <Button 
            type="submit" 
            disabled={busy || otp.length !== 6}
            className="w-full"
          >
            {registration.isVerifying ? 'Đang xác minh...' : 'Xác minh'}
          </Button>
        </div>

        <div className="mt-5 mb-6 text-center text-[13px] font-medium text-gray-600">
          Chưa nhận được mã?{' '}
          <button 
            type="button" 
            disabled={busy || countdown > 0} 
            onClick={resend} 
            className="font-bold text-gray-900 hover:underline disabled:text-gray-400 disabled:no-underline transition-colors"
          >
            {registration.isResending ? 'Đang gửi lại...' : 'Gửi lại mã'}
          </button>
          {' '}
          <OtpCountdown resendAvailableAt={registration.resendAvailableAt} onChange={updateCountdown} render={(sec) => sec > 0 ? <span className="font-bold text-gray-900">(sau {sec}s)</span> : null} />
        </div>

        {/* Nút Quay lại */}
        <div className="mb-4">
          <Button type="button" onClick={() => navigate('/register')} variant="secondary" className="w-full">
            Quay lại
          </Button>
        </div>

        <div className="my-5 flex items-center gap-3 text-[10px] font-bold uppercase tracking-widest text-gray-400">
          <span className="h-px flex-1 bg-gray-200" />
          Hoặc
          <span className="h-px flex-1 bg-gray-200" />
        </div>
        
        <GoogleAuthButton
          includeRegistrationFlow
          onAuthenticated={(session) => navigate(getAuthenticatedHome(session), { replace: true })}
          onConflict={() => navigate('/auth/social-conflict', { replace: true })}
        />

        {/* Nút Hủy đăng ký */}
        <div className="mt-6 text-center">
          <button 
            type="button" 
            onClick={cancel} 
            disabled={busy} 
            className="text-[13px] font-medium text-gray-500 hover:text-gray-900 transition-colors disabled:opacity-50"
          >
            Hủy quá trình đăng ký
          </button>
        </div>
      </form>
    </AuthLayout>
  );
}
