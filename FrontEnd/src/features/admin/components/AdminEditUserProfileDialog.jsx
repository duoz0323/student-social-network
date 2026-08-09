import { useMemo, useState } from 'react';
import { Camera, Trash2, UserRound, X } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import {
  ADMIN_PROFILE_LIMITS,
  buildAdminProfilePayload,
  getLatestAdultBirthDate,
  validateAdminAvatarFile,
  validateAdminProfileDraft,
} from '../utils/adminUserProfile.js';

export default function AdminEditUserProfileDialog({ user, submitting, error, onClose, onSubmit }) {
  const [draft, setDraft] = useState({
    displayName: user.displayName ?? '',
    dateOfBirth: user.dateOfBirth ?? '',
    bio: user.bio ?? '',
  });
  const [avatarPreview, setAvatarPreview] = useState(user.avatarUrl ?? '');
  const [avatarFile, setAvatarFile] = useState(null);
  const [avatarAction, setAvatarAction] = useState('KEEP');
  const [avatarError, setAvatarError] = useState('');
  const maximumBirthDate = useMemo(() => getLatestAdultBirthDate(), []);
  const displayNameLength = draft.displayName.trim().length;
  const canSubmit = validateAdminProfileDraft(draft) && !submitting;

  function updateField(field, value) {
    setDraft((current) => ({ ...current, [field]: value }));
  }

  function submit(event) {
    event.preventDefault();
    if (!canSubmit) return;
    onSubmit(buildAdminProfilePayload(draft), { avatarAction, avatarFile });
  }

  function selectAvatar(event) {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;
    const validationError = validateAdminAvatarFile(file);
    if (validationError) {
      setAvatarError(validationError);
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      setAvatarPreview(String(reader.result));
      setAvatarFile(file);
      setAvatarAction('REPLACE');
      setAvatarError('');
    };
    reader.readAsDataURL(file);
  }

  function removeAvatar() {
    setAvatarPreview('');
    setAvatarFile(null);
    setAvatarAction(user.avatarUrl ? 'REMOVE' : 'KEEP');
    setAvatarError('');
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

        <div className="mb-5 flex items-center gap-4 rounded-xl border border-slate-200 bg-slate-50 p-4">
          <div className="flex h-20 w-20 shrink-0 items-center justify-center overflow-hidden rounded-full bg-slate-200 text-slate-500 ring-2 ring-white">
            {avatarPreview ? (
              <img src={avatarPreview} alt="Ảnh đại diện đang chọn" className="h-full w-full object-cover" />
            ) : (
              <UserRound className="h-9 w-9" aria-hidden="true" />
            )}
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-sm font-semibold text-slate-700">Ảnh đại diện</p>
            <p className="mt-1 text-xs text-slate-500">JPG, JPEG, PNG hoặc WEBP; tối đa 10 MB.</p>
            <div className="mt-3 flex flex-wrap gap-2">
              <label className={`inline-flex h-9 items-center gap-2 rounded-md bg-blue-600 px-3 text-xs font-semibold text-white transition hover:bg-blue-700 ${submitting ? 'pointer-events-none opacity-60' : 'cursor-pointer'}`}>
                <Camera className="h-4 w-4" aria-hidden="true" />
                Chọn ảnh
                <input type="file" accept="image/jpeg,image/png,image/webp" hidden disabled={submitting} onChange={selectAvatar} />
              </label>
              {avatarPreview ? (
                <button type="button" disabled={submitting} onClick={removeAvatar} className="inline-flex h-9 items-center gap-2 rounded-md border border-red-200 bg-white px-3 text-xs font-semibold text-red-600 transition hover:bg-red-50 disabled:opacity-60">
                  <Trash2 className="h-4 w-4" aria-hidden="true" />
                  Xóa ảnh
                </button>
              ) : null}
            </div>
            {avatarError ? <p className="mt-2 text-xs text-red-600" role="alert">{avatarError}</p> : null}
          </div>
        </div>

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
