import { useEffect, useMemo, useRef, useState } from 'react';
import { isRequestCanceled } from '../../../api/apiError.js';
import { academicProfileService } from '../services/academicProfileService.js';
import {
  createEntryYears,
  nextAcademicDropdownOpen,
  shouldSearchAcademic,
  selectFaculty,
  selectMajor,
  selectSchool,
} from '../utils/academicProfileUtils.js';

const SEARCH_DELAY_MS = 450;

function optionLabel(option, kind) {
  if (!option) return '';
  if (kind === 'school' && option.shortName) return `${option.shortName} — ${option.name}`;
  return option.name;
}

function loadOptions(kind, parentId, keyword, signal) {
  if (kind === 'school') return academicProfileService.searchSchools(keyword, signal);
  if (kind === 'faculty') return academicProfileService.searchFaculties(parentId, keyword, signal);
  return academicProfileService.searchMajors(parentId, keyword, signal);
}

function AcademicAutocomplete({
  id,
  label,
  kind,
  value,
  parentId,
  disabled,
  placeholder,
  onSelect,
}) {
  const containerRef = useRef(null);
  const [query, setQuery] = useState('');
  const [searchActive, setSearchActive] = useState(false);
  const [options, setOptions] = useState([]);
  const [status, setStatus] = useState('idle');
  const [retryKey, setRetryKey] = useState(0);

  useEffect(() => {
    function handleOutsidePointerDown(event) {
      if (containerRef.current?.contains(event.target)) return;
      setSearchActive(false);
      setOptions([]);
      setStatus('idle');
    }

    document.addEventListener('pointerdown', handleOutsidePointerDown);
    return () => document.removeEventListener('pointerdown', handleOutsidePointerDown);
  }, []);

  useEffect(() => {
    const keyword = query.trim();
    if (!shouldSearchAcademic({ kind, parentId, keyword, searchActive, disabled })) return undefined;

    const controller = new AbortController();
    const timer = setTimeout(async () => {
      setStatus('loading');
      try {
        const result = await loadOptions(kind, parentId, keyword, controller.signal);
        setOptions(Array.isArray(result) ? result : []);
        setStatus(Array.isArray(result) && result.length ? 'success' : 'empty');
      } catch (error) {
        if (!isRequestCanceled(error)) {
          setOptions([]);
          setStatus('error');
        }
      }
    }, SEARCH_DELAY_MS);

    return () => {
      clearTimeout(timer);
      controller.abort();
    };
  }, [disabled, kind, parentId, query, retryKey, searchActive]);

  function handleQueryChange(event) {
    const nextQuery = event.target.value;
    setQuery(nextQuery);
    setSearchActive(true);
    setStatus('loading');
    if (value) onSelect(null);
  }

  function openDropdown() {
    if (disabled || searchActive) return;
    setQuery(value ? optionLabel(value, kind) : '');
    setSearchActive(true);
    setStatus('loading');
    // Mỗi lần mở lại tạo một request lifecycle mới, kể cả query không đổi.
    setRetryKey((key) => key + 1);
  }

  function closeDropdown() {
    setSearchActive(false);
    setOptions([]);
    setStatus('idle');
  }

  function handleInputClick() {
    const nextOpen = nextAcademicDropdownOpen(searchActive, 'toggle');
    if (nextOpen) openDropdown();
    else closeDropdown();
  }

  function handleInputKeyDown(event) {
    if (event.key === 'Escape') {
      event.preventDefault();
      closeDropdown();
    }
  }

  function chooseOption(option) {
    onSelect(option);
    setQuery(optionLabel(option, kind));
    closeDropdown();
  }

  const showDropdown = searchActive && !disabled;

  return (
    <div ref={containerRef} className="relative">
      <label htmlFor={id} className="mb-2 block text-sm font-medium text-inherit">{label}</label>
      <input
        id={id}
        type="search"
        value={searchActive ? query : optionLabel(value, kind)}
        disabled={disabled}
        placeholder={placeholder}
        autoComplete="off"
        onClick={handleInputClick}
        onKeyDown={handleInputKeyDown}
        onChange={handleQueryChange}
        className="app-field h-12 w-full rounded-xl border px-3.5 text-[15px] outline-none transition disabled:cursor-not-allowed disabled:opacity-55"
        aria-autocomplete="list"
        aria-expanded={showDropdown}
      />
      {showDropdown ? (
        <div className="absolute z-30 mt-1 max-h-56 w-full overflow-y-auto rounded-xl border border-[var(--app-border)] bg-[var(--app-surface)] p-1.5 shadow-lg" role="listbox">
          {status === 'loading' ? <p className="px-3 py-3 text-sm text-[var(--app-muted)]">Đang tìm kiếm...</p> : null}
          {status === 'empty' ? <p className="px-3 py-3 text-sm text-[var(--app-muted)]">Không tìm thấy dữ liệu phù hợp.</p> : null}
          {status === 'error' ? (
            <div className="px-3 py-3 text-sm text-red-600">
              <p>Không thể tải dữ liệu.</p>
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
          {status === 'success' ? options.map((option) => (
            <button
              key={option.id}
              type="button"
              role="option"
              aria-selected={String(value?.id) === String(option.id)}
              onClick={() => chooseOption(option)}
              className="block w-full rounded-lg px-3 py-2.5 text-left text-sm text-[var(--app-text)] hover:bg-[var(--app-surface-soft)]"
            >
              {optionLabel(option, kind)}
            </button>
          )) : null}
        </div>
      ) : null}
    </div>
  );
}

export default function AcademicProfileFields({ value, onChange, disabled = false, idPrefix = 'academic' }) {
  const entryYears = useMemo(() => createEntryYears(), []);

  return (
    <div className="space-y-5">
      <AcademicAutocomplete
        id={`${idPrefix}-school`}
        label="Trường"
        kind="school"
        value={value.school}
        disabled={disabled}
        placeholder="Chọn hoặc nhập tên trường"
        onSelect={(school) => onChange(selectSchool(value, school))}
      />
      <AcademicAutocomplete
        key={`faculty-${value.school?.id ?? 'none'}`}
        id={`${idPrefix}-faculty`}
        label="Khoa"
        kind="faculty"
        value={value.faculty}
        parentId={value.school?.id}
        disabled={disabled || !value.school?.id}
        placeholder={value.school?.id ? 'Chọn hoặc nhập tên khoa' : 'Chọn trường trước'}
        onSelect={(faculty) => onChange(selectFaculty(value, faculty))}
      />
      <AcademicAutocomplete
        key={`major-${value.faculty?.id ?? 'none'}`}
        id={`${idPrefix}-major`}
        label="Ngành"
        kind="major"
        value={value.major}
        parentId={value.faculty?.id}
        disabled={disabled || !value.faculty?.id}
        placeholder={value.faculty?.id ? 'Chọn hoặc nhập tên ngành' : 'Chọn khoa trước'}
        onSelect={(major) => onChange(selectMajor(value, major))}
      />
      <div>
        <label htmlFor={`${idPrefix}-entry-year`} className="mb-2 block text-sm font-medium text-inherit">Năm nhập học</label>
        <select
          id={`${idPrefix}-entry-year`}
          value={value.entryYear ?? ''}
          disabled={disabled}
          onChange={(event) => onChange({ ...value, entryYear: event.target.value ? Number(event.target.value) : null })}
          className="app-field h-12 w-full rounded-xl border px-3.5 text-[15px] outline-none transition disabled:opacity-55"
        >
          <option value="">Chưa chọn</option>
          {entryYears.map((year) => <option key={year} value={year}>{year}</option>)}
        </select>
      </div>
    </div>
  );
}
