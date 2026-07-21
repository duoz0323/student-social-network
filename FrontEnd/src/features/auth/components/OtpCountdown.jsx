import { useEffect, useState } from 'react';

function secondsUntil(timestamp) {
  if (!timestamp) return 0;
  return Math.max(0, Math.ceil((new Date(timestamp).getTime() - Date.now()) / 1000));
}

export default function OtpCountdown({ resendAvailableAt, onChange, render, readyText = 'Có thể gửi lại mã ngay.', waitingText = 'Có thể gửi lại sau' }) {
  const [seconds, setSeconds] = useState(() => secondsUntil(resendAvailableAt));

  useEffect(() => {
    function update() {
      const next = secondsUntil(resendAvailableAt);
      setSeconds(next);
      onChange?.(next);
    }
    update();
    const intervalId = window.setInterval(update, 1000);
    return () => window.clearInterval(intervalId);
  }, [resendAvailableAt, onChange]);

  if (render) return render(seconds);

  if (seconds === 0) return <span>{readyText}</span>;
  return <span>{waitingText} {seconds} giây.</span>;
}
