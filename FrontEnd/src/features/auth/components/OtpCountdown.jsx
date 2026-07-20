import { useEffect, useState } from 'react';

function secondsUntil(timestamp) {
  if (!timestamp) return 0;
  return Math.max(0, Math.ceil((new Date(timestamp).getTime() - Date.now()) / 1000));
}

export default function OtpCountdown({ resendAvailableAt, onChange }) {
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

  if (seconds === 0) return <span>Có thể gửi lại mã ngay.</span>;
  return <span>Có thể gửi lại sau {seconds} giây.</span>;
}
