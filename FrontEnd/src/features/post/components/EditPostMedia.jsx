import { useEffect, useRef, useState } from 'react';
import { POST_MEDIA_LIMITS, readVideoDuration, validatePostMediaFiles } from '../utils/postMediaValidation.js';

function toExistingItem(item) {
  return { ...item, source: 'EXISTING', mediaType: item.mediaType ?? 'IMAGE' };
}

function selectionOf(items) {
  return {
    keepMediaIds: items.filter((item) => item.source === 'EXISTING').map((item) => item.id),
    newMediaFiles: items.filter((item) => item.source === 'NEW').map((item) => item.file),
    totalCount: items.length,
  };
}

/** Cho phép giữ/gỡ media cũ và thêm ảnh hoặc video mới khi sửa bài viết. */
export default function EditPostMedia({ media = [], disabled = false, onChange, onBusyChange }) {
  const [items, setItems] = useState(() => media.map(toExistingItem));
  const [error, setError] = useState('');
  const [processing, setProcessing] = useState(false);
  const fileInputRef = useRef(null);
  const itemsRef = useRef(items);

  useEffect(() => {
    itemsRef.current = items;
  }, [items]);

  useEffect(() => () => {
    itemsRef.current.filter((item) => item.source === 'NEW')
      .forEach((item) => URL.revokeObjectURL(item.url));
  }, []);

  function updateItems(nextItems) {
    setItems(nextItems);
    onChange(selectionOf(nextItems));
  }

  function removeItem(indexToRemove) {
    if (disabled || processing) return;
    const removed = items[indexToRemove];
    if (removed?.source === 'NEW') URL.revokeObjectURL(removed.url);
    updateItems(items.filter((_, index) => index !== indexToRemove));
    setError('');
  }

  async function addFiles(event) {
    const files = Array.from(event.target.files ?? []);
    event.target.value = '';
    if (!files.length || disabled || processing) return;

    setProcessing(true);
    onBusyChange?.(true);
    setError('');
    const createdItems = [];
    try {
      const typedFiles = validatePostMediaFiles(items, files);
      for (const typedFile of typedFiles) {
        const url = URL.createObjectURL(typedFile.file);
        if (typedFile.mediaType === 'VIDEO') {
          const duration = await readVideoDuration(url);
          if (!Number.isFinite(duration) || duration <= 0 || duration > POST_MEDIA_LIMITS.maxVideoDurationSeconds) {
            URL.revokeObjectURL(url);
            throw new Error('Video không được dài quá 3 phút.');
          }
        }
        createdItems.push({ ...typedFile, source: 'NEW', url });
      }
      updateItems([...items, ...createdItems]);
    } catch (validationError) {
      createdItems.forEach((item) => URL.revokeObjectURL(item.url));
      setError(validationError.message);
    } finally {
      setProcessing(false);
      onBusyChange?.(false);
    }
  }

  return (
    <div className="mt-4">
      <div className="mb-2 flex items-center justify-between">
        <p className="text-sm font-semibold text-[var(--app-text)]">Ảnh và video</p>
        <span className="text-xs text-[var(--app-muted)]">{items.length}/4 media</span>
      </div>

      {items.length > 0 && (
        <div className={`grid gap-2 ${items.length > 1 ? 'grid-cols-2' : 'grid-cols-1'}`}>
          {items.map((item, index) => (
            <div key={item.source === 'EXISTING' ? `existing-${item.id}` : `new-${item.file.name}-${item.file.lastModified}-${index}`}
              className="relative overflow-hidden rounded-xl border border-[var(--app-border)] bg-black">
              {item.mediaType === 'VIDEO' ? (
                <video src={item.url} poster={item.thumbnailUrl || undefined} controls preload="metadata"
                  className="aspect-square max-h-64 w-full object-contain" />
              ) : (
                <img src={item.url} alt={`Media bài viết ${index + 1}`} className="aspect-square max-h-64 w-full object-cover" />
              )}
              <button type="button" onClick={() => removeItem(index)} disabled={disabled || processing}
                className="absolute right-2 top-2 flex h-7 w-7 items-center justify-center rounded-full bg-zinc-900/60 text-white transition hover:bg-zinc-900/80 disabled:opacity-50"
                aria-label={`Gỡ media ${index + 1}`}>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M18 6 6 18" /><path d="m6 6 12 12" />
                </svg>
              </button>
            </div>
          ))}
        </div>
      )}

      <button type="button" disabled={disabled || processing || items.length >= POST_MEDIA_LIMITS.maxMedia}
        onClick={() => fileInputRef.current?.click()}
        className="mt-3 rounded-xl border border-dashed border-[var(--app-border-strong)] px-4 py-2 text-sm font-semibold text-[var(--app-brand)] hover:bg-[var(--app-surface-soft)] disabled:cursor-not-allowed disabled:opacity-50">
        {processing ? 'Đang kiểm tra media...' : 'Thêm ảnh hoặc video'}
      </button>
      <input ref={fileInputRef} type="file" hidden multiple
        accept="image/jpeg,image/png,image/webp,video/mp4,video/webm" onChange={addFiles} />
      <p className="mt-2 text-xs text-[var(--app-muted)]">Tối đa 4 media, trong đó tối đa 1 video dài 3 phút.</p>
      {error && <p className="app-error mt-2 rounded-xl px-3 py-2 text-sm">{error}</p>}
    </div>
  );
}
