import { useEffect, useState } from 'react';
import { isRequestCanceled } from '../../../api/apiError.js';
import { academicProfileService } from '../services/academicProfileService.js';
import { MAX_INTERESTS, toggleInterest } from '../utils/academicProfileUtils.js';

export default function InterestSelector({ value, onChange, disabled = false }) {
  const [interests, setInterests] = useState([]);
  const [status, setStatus] = useState('loading');
  const [limitMessage, setLimitMessage] = useState('');
  const [retryKey, setRetryKey] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    academicProfileService.getInterests(controller.signal)
      .then((result) => {
        const items = Array.isArray(result) ? result : [];
        setInterests(items);
        setStatus(items.length ? 'success' : 'empty');
      })
      .catch((error) => {
        if (!isRequestCanceled(error)) setStatus('error');
      });
    return () => controller.abort();
  }, [retryKey]);

  function handleToggle(interestId) {
    const result = toggleInterest(value, interestId);
    setLimitMessage(result.limitReached ? `Bạn chỉ có thể chọn tối đa ${MAX_INTERESTS} sở thích.` : '');
    onChange(result.interestIds);
  }

  return (
    <div>
      <div className="mb-3 flex items-center justify-between gap-3">
        <p className="text-sm text-[var(--app-muted)]">Chọn những chủ đề bạn quan tâm.</p>
        <span className="shrink-0 text-sm font-semibold text-[var(--app-text)]">{value.length}/{MAX_INTERESTS}</span>
      </div>

      {status === 'loading' ? <p className="py-6 text-center text-sm text-[var(--app-muted)]">Đang tải sở thích...</p> : null}
      {status === 'empty' ? <p className="py-6 text-center text-sm text-[var(--app-muted)]">Chưa có danh mục sở thích.</p> : null}
      {status === 'error' ? (
        <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          <p>Không thể tải danh sách sở thích.</p>
          <button
            type="button"
            className="mt-1 font-semibold underline"
            onClick={() => {
              setStatus('loading');
              setRetryKey((key) => key + 1);
            }}
          >
            Thử lại
          </button>
        </div>
      ) : null}
      {status === 'success' ? (
        <div className="flex flex-wrap gap-2.5">
          {interests.map((interest) => {
            const selected = value.some((id) => String(id) === String(interest.id));
            const selectionLocked = !selected && value.length >= MAX_INTERESTS;
            return (
              <button
                key={interest.id}
                type="button"
                disabled={disabled || selectionLocked}
                aria-pressed={selected}
                onClick={() => handleToggle(interest.id)}
                className={`rounded-full border px-3.5 py-2 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-45 ${
                  selected
                    ? 'border-[var(--app-text)] bg-[var(--app-text)] text-[var(--app-surface)]'
                    : 'border-[var(--app-border-strong)] bg-[var(--app-surface)] text-[var(--app-text)] hover:bg-[var(--app-surface-soft)]'
                }`}
              >
                {interest.name}
              </button>
            );
          })}
        </div>
      ) : null}
      {limitMessage ? <p className="mt-3 text-sm text-amber-600" role="alert">{limitMessage}</p> : null}
    </div>
  );
}
