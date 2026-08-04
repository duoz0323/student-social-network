import { useMemo, useState } from 'react';
import { X } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import {
  ADMIN_PROFILE_LIMITS,
  buildAdminProfilePayload,
  getLatestAdultBirthDate,
  validateAdminProfileDraft,
} from '../utils/adminUserProfile.js';

export default function AdminEditUserProfileDialog({ user, submitting, error, onClose, onSubmit }) {
  const [draft, setDraft] = useState({
    displayName: user.displayName ?? '',
    dateOfBirth: user.dateOfBirth ?? '',
    bio: user.bio ?? '',
  });
  const maximumBirthDate = useMemo(() => getLatestAdultBirthDate(), []);
  const displayNameLength = draft.displayName.trim().length;
  const canSubmit = validateAdminProfileDraft(draft) && !submitting;

  function updateField(field, value) {
    setDraft((current) => ({ ...current, [field]: value }));
  }

  function submit(event) {
    event.preventDefault();
    if (!canSubmit) return;
    onSubmit(buildAdminProfilePayload(draft));
  }

  return (
    <Modal
      open
      size="md"
      className="!max-w-[560px] !rounded-lg !bg-white"
      bodyClassName="bg-white px-6 pb-6 pt-5 !text-black [color-scheme:light]"
      customHeader={(
        <header className="flex shrink-0 items-start justify-between rounded-t-lg border-b border-slate-100 bg-white px-6 py-5">
          <div>
            <h2 className="text-base font-bold text-slate-800">Sửa hồ sơ người dùng</h2>
            <p className="mt-1 text-xs text-slate-500">Cập nhật nội dung hồ sơ của {user.displayName || `người dùng #${user.userId}`}.</p>
          </div>
          <button type="button" className="flex h-8 w-8 items-center justify-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-700" onClick={onClose} aria-label="Đóng form sửa hồ sơ">
            <X className="h-5 w-5" aria-hidden="true" />
          </button>
        </header>
      )}
      onClose={submitting ? undefined : onClose}
    >
      <form onSubmit={submit}>
        {error ? <p className="mb-4 rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{error}</p> : null}

        <label className="block text-sm font-semibold text-slate-700" htmlFor="admin-profile-display-name">
          Tên hiển thị
        </label>
        <input
          id="admin-profile-display-name"
          value={draft.displayName}
          maxLength={ADMIN_PROFILE_LIMITS.maximumDisplayNameLength}
          onChange={(event) => updateField('displayName', event.target.value)}
          className="mt-2 w-full rounded-lg border border-slate-200 bg-white px-3 py-2.5 text-sm !text-black placeholder:!text-slate-400 outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
          required
        />
        <p className={`mt-1 text-xs ${displayNameLength > 0 && displayNameLength < 2 ? 'text-red-600' : 'text-slate-400'}`}>
          {displayNameLength}/{ADMIN_PROFILE_LIMITS.maximumDisplayNameLength} ký tự; tối thiểu {ADMIN_PROFILE_LIMITS.minimumDisplayNameLength} ký tự.
        </p>

        <label className="mt-5 block text-sm font-semibold text-slate-700" htmlFor="admin-profile-birth-date">
          Ngày sinh
        </label>
        <input
          id="admin-profile-birth-date"
          type="date"
          value={draft.dateOfBirth}
          max={maximumBirthDate}
          onChange={(event) => updateField('dateOfBirth', event.target.value)}
          className="mt-2 w-full rounded-lg border border-slate-200 bg-white px-3 py-2.5 text-sm !text-black [color-scheme:light] outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
          required
        />
        <p className="mt-1 text-xs text-slate-400">Người dùng phải đủ 18 tuổi.</p>

        <div className="mt-5 flex items-center justify-between">
          <label className="text-sm font-semibold text-slate-700" htmlFor="admin-profile-bio">Giới thiệu</label>
          <span className={`text-xs ${draft.bio.length > ADMIN_PROFILE_LIMITS.maximumBioLength ? 'text-red-600' : 'text-slate-400'}`}>{draft.bio.length}/{ADMIN_PROFILE_LIMITS.maximumBioLength}</span>
        </div>
        <textarea
          id="admin-profile-bio"
          value={draft.bio}
          maxLength={ADMIN_PROFILE_LIMITS.maximumBioLength}
          rows={5}
          onChange={(event) => updateField('bio', event.target.value)}
          className="mt-2 w-full resize-none rounded-lg border border-slate-200 bg-white px-3 py-2.5 text-sm leading-5 !text-black placeholder:!text-slate-400 outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
          placeholder="Nhập phần giới thiệu người dùng"
        />

        <div className="mt-6 grid grid-cols-2 gap-3">
          <Button className="!h-10 !rounded-md !border-slate-200 !bg-white !text-slate-700 hover:!bg-slate-50" variant="secondary" disabled={submitting} onClick={onClose}>
            Hủy
          </Button>
          <Button className="!h-10 !rounded-md !border-blue-600 !bg-blue-600 !text-white hover:!bg-blue-700" type="submit" disabled={!canSubmit}>
            {submitting ? 'Đang lưu...' : 'Lưu thay đổi'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
