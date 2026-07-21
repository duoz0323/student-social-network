import { useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import OtpInput from './OtpInput.jsx';
import OtpCountdown from './OtpCountdown.jsx';

function secondsUntil(timestamp) {
  return Math.max(0, Math.ceil((Date.parse(timestamp) - Date.now()) / 1000));
}

export default function RecoveryOtpForm({ challenge, onVerify, onResend, onRestart, disabled, error, fieldError }) {
  const [code, setCode] = useState('');
  const [cooldown, setCooldown] = useState(() => secondsUntil(challenge.resendAvailableAt));

  return (
    <form onSubmit={(event) => { event.preventDefault(); if (code.length === 6) onVerify(code); }} className="px-7 sm:px-10 py-8">
      <div className="mb-6 text-center">
        <h2 className="text-[1.35rem] font-bold text-gray-900 mb-2">Nhập mã xác minh</h2>
        <p className="text-[13px] text-gray-500 font-medium px-2">
          Vui lòng nhập mã gồm 6 chữ số đã được gửi đến email.
        </p>
        <p className="mt-1 text-[12px] text-gray-400 font-medium"><OtpCountdown resendAvailableAt={challenge.otpExpiresAt} readyText="Mã đã hết hạn." waitingText="Mã còn hiệu lực trong" /></p>
      </div>
      
      {error ? (
        <div className="mb-5 overflow-hidden">
          <p className="rounded-lg bg-red-50 p-3 text-[13px] font-medium text-center border border-red-100 text-red-600">
            {error}
          </p>
        </div>
      ) : null}

      <div className="mb-6">
        <OtpInput value={code} onChange={setCode} disabled={disabled} error={fieldError} />
      </div>
      
      <div className="mb-2">
        <Button 
          type="submit" 
          disabled={disabled || code.length !== 6}
          className="w-full"
        >
          {disabled ? 'Đang xử lý...' : 'Xác minh'}
        </Button>
      </div>
      
      <div className="mt-5 mb-6 text-center text-[13px] font-medium text-gray-600">
        Chưa nhận được mã?{' '}
        <button 
          type="button" 
          disabled={disabled || cooldown > 0} 
          onClick={onResend} 
          className="font-bold text-gray-900 hover:underline disabled:text-gray-400 disabled:no-underline transition-colors"
        >
          Gửi lại mã
        </button>
        {' '}
        <OtpCountdown resendAvailableAt={challenge.resendAvailableAt} onChange={setCooldown} render={(sec) => sec > 0 ? <span className="font-bold text-gray-900">(sau {sec}s)</span> : null} />
      </div>
      
      <div className="mt-4">
        <Button type="button" disabled={disabled} onClick={onRestart} variant="secondary" className="w-full">
          Quay lại định danh
        </Button>
      </div>
    </form>
  );
}
