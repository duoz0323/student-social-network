export default function OtpInput({ value, onChange, disabled, error }) {
  function updateValue(event) {
    // OTP chỉ gồm sáu chữ số; ký tự khác bị loại ngay tại input và không được lưu ở browser storage.
    onChange(event.target.value.replace(/\D/g, '').slice(0, 6));
  }

  return (
    <label className="block text-sm font-black text-zinc-800">
      Mã xác minh
      <input
        autoFocus
        autoComplete="one-time-code"
        inputMode="numeric"
        pattern="[0-9]*"
        value={value}
        onChange={updateValue}
        disabled={disabled}
        aria-invalid={Boolean(error)}
        aria-describedby={error ? 'otp-error' : undefined}
        placeholder="Nhập mã gồm 6 chữ số"
        className="mt-2 h-14 w-full rounded-[var(--radius-input)] border border-[var(--app-border-strong)] bg-zinc-50 px-4 text-center text-2xl font-black tracking-[0.45em] outline-none focus:border-[var(--app-text)]"
      />
      {error ? <span id="otp-error" className="mt-2 block text-xs font-semibold text-red-700">{error}</span> : null}
    </label>
  );
}
