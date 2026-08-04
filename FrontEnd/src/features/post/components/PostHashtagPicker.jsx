import { useEffect, useState } from 'react';
import { postApi } from '../../../api/index.js';

/**
 * Bộ chọn hashtag dùng chung cho form tạo và sửa bài viết.
 * Backend vẫn là nơi chuẩn hóa và quyết định dùng hashtag cũ hay tạo hashtag mới.
 */
export default function PostHashtagPicker({ value, onChange, disabled = false }) {
  const [query, setQuery] = useState('');
  const [open, setOpen] = useState(false);
  const [suggestions, setSuggestions] = useState([]);
  const [searchResult, setSearchResult] = useState(null);
  const [searching, setSearching] = useState(false);

  useEffect(() => {
    const keyword = query.trim();
    if (!keyword || !open) return undefined;
    const controller = new AbortController();
    const timer = setTimeout(() => {
      setSearching(true);
      postApi.suggestHashtags(keyword, controller.signal)
        .then((response) => {
          setSearchResult(response);
          setSuggestions(response.suggestions ?? []);
        })
        .catch((error) => {
          if (error.code !== 'ERR_CANCELED') {
            setSearchResult(null);
            setSuggestions([]);
          }
        })
        .finally(() => {
          if (!controller.signal.aborted) setSearching(false);
        });
    }, 250);
    return () => {
      clearTimeout(timer);
      controller.abort();
    };
  }, [open, query]);

  function openPicker() {
    if (disabled) return;
    setQuery(value ?? '');
    setSearchResult(null);
    setSuggestions([]);
    setOpen(true);
  }

  function changeQuery(event) {
    setQuery(event.target.value);
    setSearchResult(null);
    setSuggestions([]);
  }

  function selectHashtag(name) {
    onChange(name);
    setQuery('');
    setOpen(false);
    setSearchResult(null);
    setSuggestions([]);
  }

  function removeHashtag() {
    if (disabled) return;
    onChange(null);
    setQuery('');
    setOpen(false);
    setSearchResult(null);
    setSuggestions([]);
  }

  return (
    <div className="flex items-center gap-1">
      <span className="text-xs text-[var(--app-muted)]">›</span>
      <div className="relative min-w-0">
        {open ? (
          <input
            value={query}
            onChange={changeQuery}
            maxLength={100}
            disabled={disabled}
            placeholder="Cộng đồng hoặc chủ đề"
            className="w-[180px] max-w-[38vw] border-none bg-transparent p-0 text-[15px] font-semibold text-[var(--app-brand)] outline-none placeholder:font-normal placeholder:text-[var(--app-muted)] disabled:opacity-50"
            autoFocus
          />
        ) : (
          <button
            type="button"
            onClick={openPicker}
            disabled={disabled}
            className={`block max-w-[220px] truncate text-left text-[15px] transition hover:underline disabled:opacity-50 ${
              value ? 'font-semibold text-[var(--app-brand)]' : 'text-[var(--app-muted)]'
            }`}
          >
            {value || 'Cộng đồng hoặc chủ đề'}
          </button>
        )}

        {open && (
          <div className="absolute left-0 top-7 z-50 w-[330px] max-w-[calc(100vw-88px)] overflow-hidden rounded-xl border border-[var(--app-border)] bg-[var(--app-surface)] shadow-[0_16px_45px_rgba(0,0,0,0.22)]">
            <div className="max-h-[350px] overflow-y-auto">
              {!query.trim() && (
                <p className="px-4 py-5 text-sm text-[var(--app-muted)]">
                  Nhập tên để tìm hoặc gắn một chủ đề mới.
                </p>
              )}

              {searching && query.trim() && (
                <p className="px-4 py-5 text-sm text-[var(--app-muted)]">Đang tìm chủ đề...</p>
              )}

              {!searching && suggestions.map((item) => (
                <button
                  type="button"
                  key={item.hashtagId}
                  onClick={() => selectHashtag(item.name)}
                  className="block w-full border-b border-[var(--app-border)] px-4 py-3.5 text-left transition last:border-b-0 hover:bg-[var(--app-surface-soft)]"
                >
                  <span className="flex items-center gap-2 text-[15px] font-semibold text-[var(--app-brand)]">
                    <svg width="10" height="10" viewBox="0 0 10 10" fill="currentColor" aria-hidden="true">
                      <circle cx="2" cy="5" r="2" />
                      <circle cx="6.5" cy="2" r="1.5" />
                      <circle cx="7.5" cy="7" r="1.5" />
                    </svg>
                    <span className="truncate">{item.name}</span>
                  </span>
                  <span className="mt-1 block text-[13px] text-[var(--app-muted)]">{item.postCount} bài viết</span>
                </button>
              ))}

              {!searching && searchResult?.canUseAsNewHashtag && searchResult.normalizedKeyword && (
                <button
                  type="button"
                  onClick={() => selectHashtag(searchResult.normalizedKeyword)}
                  className="block w-full border-b border-[var(--app-border)] px-4 py-3.5 text-left transition hover:bg-[var(--app-surface-soft)]"
                >
                  <span className="block truncate text-[15px] font-semibold text-[var(--app-text)]">
                    {searchResult.normalizedKeyword}
                  </span>
                  <span className="mt-0.5 block text-[13px] text-[var(--app-muted)]">+ Gắn thẻ chủ đề mới</span>
                </button>
              )}
            </div>
          </div>
        )}
      </div>

      {value && (
        <button type="button" onClick={removeHashtag} disabled={disabled}
          className="flex h-6 w-6 items-center justify-center rounded-full text-[var(--app-muted)] hover:bg-[var(--app-surface-soft)] disabled:opacity-50"
          aria-label="Bỏ chủ đề đã chọn">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
            <path d="M18 6 6 18M6 6l12 12" />
          </svg>
        </button>
      )}
    </div>
  );
}
