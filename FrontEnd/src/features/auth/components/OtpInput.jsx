import { useRef } from 'react';

export default function OtpInput({ value, onChange, disabled, error }) {
  const inputRefs = useRef([]);

  const handleChange = (e, index) => {
    const val = e.target.value;
    const digit = val.replace(/\D/g, '').slice(-1);
    
    const chars = value.split('');
    if (digit) {
      chars[index] = digit;
      onChange(chars.join('').slice(0, 6));
      if (index < 5) {
        inputRefs.current[index + 1]?.focus();
      }
    } else {
      chars[index] = '';
      onChange(chars.join('').slice(0, 6));
    }
  };

  const handleKeyDown = (e, index) => {
    if (e.key === 'Backspace') {
      if (!value[index] && index > 0) {
        const chars = value.split('');
        chars[index - 1] = '';
        onChange(chars.join(''));
        inputRefs.current[index - 1]?.focus();
      } else {
        const chars = value.split('');
        chars[index] = '';
        onChange(chars.join(''));
      }
    } else if (e.key === 'ArrowLeft' && index > 0) {
      inputRefs.current[index - 1]?.focus();
    } else if (e.key === 'ArrowRight' && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData('text/plain').replace(/\D/g, '').slice(0, 6);
    if (pastedData) {
      onChange(pastedData);
      const focusIndex = Math.min(pastedData.length, 5);
      inputRefs.current[focusIndex]?.focus();
    }
  };

  return (
    <div className="flex flex-col items-center">
      <div className="flex justify-center gap-3 sm:gap-4" onPaste={handlePaste}>
        {[0, 1, 2, 3, 4, 5].map((index) => (
          <input
            key={index}
            ref={(el) => (inputRefs.current[index] = el)}
            type="text"
            inputMode="numeric"
            pattern="[0-9]*"
            maxLength={2}
            value={value[index] || ''}
            onChange={(e) => handleChange(e, index)}
            onKeyDown={(e) => handleKeyDown(e, index)}
            disabled={disabled}
            className={`w-[45px] h-[55px] sm:w-[54px] sm:h-[64px] text-center text-2xl font-semibold rounded-xl outline-none transition-all duration-200 
              ${error 
                ? 'bg-red-50 text-red-600 border-2 border-red-400 focus:ring-2 focus:ring-red-200' 
                : 'bg-gray-100 text-gray-900 border-2 border-transparent focus:bg-white focus:border-gray-900 focus:ring-2 focus:ring-gray-200'
              }`}
          />
        ))}
      </div>
    </div>
  );
}
