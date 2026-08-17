import { useEffect, useMemo, useState } from 'react';
import { KeyRound, Save, ShieldCheck, UserRound } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import Avatar from '../../../../components/common/Avatar.jsx';
import Button from '../../../../components/common/Button.jsx';
import Input from '../../../../components/common/Input.jsx';
import Modal from '../../../../components/common/Modal.jsx';
import PublicIdentityBadge from '../../../../components/common/PublicIdentityBadge.jsx';
import { EmptyState, LoadingState } from '../../../../components/common/StateBlock.jsx';
import { adminApi } from '../../../../api/adminApi.js';
import { useAuth } from '../../../auth/hooks/useAuth.js';
import { getAdminRoleLabel, usesManagedIdentityPresentation } from '../../constants/adminRbac.js';
import { validateAdminPasswordDraft } from '../../utils/adminProfilePassword.js';
import {
  buildAdminProfilePayload,
  getLatestAdultBirthDate,
  validateAdminProfileDraft,
} from '../../utils/adminUserProfile.js';
import { collaboratorApi } from '../services/collaboratorApi.js';
import AdminPageHeader from '../../components/AdminPageHeader.jsx';

const EMPTY = Object.freeze({ displayName: '', avatarUrl: '', bio: '' });
const EMPTY_ACCOUNT_PROFILE = Object.freeze({ displayName: '', dateOfBirth: '', bio: '' });
const EMPTY_PASSWORD = Object.freeze({ currentPassword: '', newPassword: '', confirmPassword: '' });

/** Quản lý đúng user_profiles của Managed Social Identity hiện tại. */
export default function CollaboratorIdentityPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [account, setAccount] = useState(null);
  const [accountDraft, setAccountDraft] = useState(EMPTY_ACCOUNT_PROFILE);
  const [draft, setDraft] = useState(EMPTY);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [avatarUploading, setAvatarUploading] = useState(false);
  const [accountError, setAccountError] = useState('');
  const [accountSaving, setAccountSaving] = useState(false);
  const [password, setPassword] = useState(EMPTY_PASSWORD);
  const [passwordOpen, setPasswordOpen] = useState(false);
  const [passwordError, setPasswordError] = useState('');
  const [changingPassword, setChangingPassword] = useState(false);
  const latestAdultBirthDate = useMemo(() => getLatestAdultBirthDate(), []);

  useEffect(() => {
    const controller = new AbortController();
    collaboratorApi.getIdentity(controller.signal).then((data) => {
      setProfile(data);
      setDraft({ displayName: data.displayName || '', avatarUrl: data.avatarUrl || '', bio: data.bio || '' });
    }).catch((requestError) => !controller.signal.aborted && setError(requestError.message));
    adminApi.getProfile(controller.signal)
      .then((data) => {
        setAccount(data);
        setAccountDraft({
          displayName: data.displayName ?? '',
          dateOfBirth: data.dateOfBirth ?? '',
          bio: data.bio ?? '',
        });
      })
      .catch((requestError) => !controller.signal.aborted && setAccountError(requestError.message));
    return () => controller.abort();
  }, []);

  async function changePassword(event) {
    event.preventDefault();
    const validationError = validateAdminPasswordDraft(password);
    if (validationError) {
      setPasswordError(validationError);
      return;
    }
    setChangingPassword(true);
    setPasswordError('');
    try {
      await adminApi.changePassword(password);
      await auth.logout();
      navigate('/login?passwordChanged=1', { replace: true });
    } catch (requestError) {
      setPasswordError(requestError.message || 'Không thể đổi mật khẩu.');
    } finally {
      setChangingPassword(false);
    }
  }

  async function saveAccountProfile(event) {
    event.preventDefault();
    if (accountSaving) return;
    if (!validateAdminProfileDraft(accountDraft)) {
      setAccountError('Vui lòng kiểm tra tên hiển thị, ngày sinh và phần giới thiệu của hồ sơ quản trị.');
      return;
    }
    setAccountSaving(true);
    setAccountError('');
    try {
      const updated = await adminApi.updateProfile(buildAdminProfilePayload(accountDraft));
      setAccount(updated);
      setAccountDraft({
        displayName: updated.displayName ?? '',
        dateOfBirth: updated.dateOfBirth ?? '',
        bio: updated.bio ?? '',
      });
      // Admin đa vai trò dùng hồ sơ Admin trên shell; cộng tác viên thuần vẫn dùng Managed Identity.
      if (!usesManagedIdentityPresentation(auth.adminRoles)) {
        auth.updateCurrentUser({
          displayName: updated.displayName,
          avatarUrl: updated.avatarUrl,
          birthDate: updated.dateOfBirth,
          bio: updated.bio,
        });
      }
    } catch (requestError) {
      setAccountError(requestError.message || 'Không thể cập nhật hồ sơ quản trị.');
    } finally {
      setAccountSaving(false);
    }
  }

  function closePassword() {
    if (changingPassword) return;
    setPasswordOpen(false);
    setPassword(EMPTY_PASSWORD);
    setPasswordError('');
  }

  async function save(event) {
    event.preventDefault();
    if (saving) return;
    setSaving(true);
    setError('');
    try {
      const updated = await collaboratorApi.updateIdentity({
        displayName: draft.displayName,
        bio: draft.bio,
      });
      setProfile(updated);
      setDraft({ displayName: updated.displayName || '', avatarUrl: updated.avatarUrl || '', bio: updated.bio || '' });
      // Sidebar/Dashboard phải đổi ngay theo Managed Identity vừa lưu, không chờ tải lại trang.
      auth.updateCurrentUser({
        username: updated.username || '',
        displayName: updated.displayName || '',
        avatarUrl: updated.avatarUrl || '',
        bio: updated.bio || '',
        managedSocialUserId: updated.userId,
      });
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSaving(false);
    }
  }

  async function uploadAvatar(event) {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file || avatarUploading) return;
    setAvatarUploading(true);
    setError('');
    try {
      const updated = await collaboratorApi.uploadIdentityAvatar(file);
      setProfile(updated);
      setDraft((value) => ({ ...value, avatarUrl: updated.avatarUrl || '' }));
      auth.updateCurrentUser({
        username: updated.username || '',
        displayName: updated.displayName || '',
        avatarUrl: updated.avatarUrl || '',
        bio: updated.bio || '',
        managedSocialUserId: updated.userId,
      });
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setAvatarUploading(false);
    }
  }

  if (!profile && !error) return <LoadingState />;
  if (!profile) return <EmptyState title="Không thể tải danh tính công khai" description={error} />;

  return <section className="mx-auto max-w-2xl space-y-6">
    <AdminPageHeader
      icon={UserRound}
      title="Hồ sơ cộng tác viên"
      description="Quản lý hồ sơ công khai và thông tin quản trị của tài khoản cộng tác viên tại một nơi."
    />
    <form onSubmit={save} className="space-y-5 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
      <div><h2 className="font-bold text-zinc-950">Thông tin công khai</h2><p className="mt-1 text-sm text-zinc-500">Nội dung này xuất hiện trên Profile, Feed, Search và các tương tác xã hội.</p></div>
      <div className="flex items-center gap-4"><Avatar src={profile.avatarUrl} name={profile.displayName} size="lg" /><div><div className="flex items-center gap-1.5 text-lg font-bold">{profile.displayName || 'Tên hiển thị'}<PublicIdentityBadge badges={['COLLABORATOR']} /></div><p className="text-sm text-zinc-500">@{profile.username || 'username'}</p><p className="mt-1 text-xs text-zinc-400">Thông tin đang được hiển thị công khai</p></div></div>
      {error ? <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700">{error}</p> : null}
      <div className="rounded-xl bg-zinc-50 p-4">
        <p className="text-xs font-semibold uppercase tracking-wide text-zinc-500">Tên người dùng công khai</p>
        <p className="mt-1 font-medium text-zinc-900">@{profile.username}</p>
        <p className="mt-1 text-xs text-zinc-500">Username được tạo một lần và không thể thay đổi.</p>
      </div>
      <Field label="Tên hiển thị" value={draft.displayName} maxLength={100} disabled={saving} onChange={(displayName) => setDraft((value) => ({ ...value, displayName }))} />
      <label className="block text-sm font-semibold text-zinc-700">Ảnh đại diện<input type="file" accept="image/jpeg,image/png,image/webp" disabled={saving || avatarUploading} onChange={uploadAvatar} className="mt-2 block w-full text-sm font-normal text-zinc-600 file:mr-3 file:rounded-lg file:border-0 file:bg-zinc-950 file:px-4 file:py-2 file:font-semibold file:text-white hover:file:bg-zinc-800" /><span className="mt-1 block text-xs font-normal text-zinc-500">{avatarUploading ? 'Đang tải ảnh...' : 'JPG, PNG hoặc WebP; tối đa 10 MB.'}</span></label>
      <label className="block text-sm font-semibold text-zinc-700">Giới thiệu<textarea value={draft.bio} maxLength={500} disabled={saving} onChange={(event) => setDraft((value) => ({ ...value, bio: event.target.value }))} className="mt-2 min-h-28 w-full rounded-xl border border-zinc-300 px-3 py-2 font-normal outline-none transition focus:border-zinc-900 focus:ring-2 focus:ring-zinc-100" /></label>
      <Button type="submit" loading={saving} loadingLabel="Đang lưu..." disabled={saving || avatarUploading || !draft.displayName.trim()}>Lưu danh tính công khai</Button>
    </form>
    <section className="space-y-5 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <div><h2 className="flex items-center gap-2 font-bold text-zinc-950"><ShieldCheck size={18} /> Hồ sơ quản trị và bảo mật</h2><p className="mt-1 text-sm text-zinc-500">Thông tin này thuộc tài khoản Admin của cộng tác viên và không xuất hiện trên trang cá nhân công khai.</p></div>
        <Button type="button" onClick={() => setPasswordOpen(true)}><KeyRound size={16} /> Đổi mật khẩu</Button>
      </div>
      {accountError ? <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700">{accountError}</p> : null}
      {account ? <div className="grid gap-3 rounded-xl bg-zinc-50 p-4 text-sm sm:grid-cols-2">
        <AccountField label="Email đăng nhập" value={account.email} />
        <AccountField label="Định danh quản trị nội bộ" value={account.username} />
        <AccountField label="Trạng thái" value={account.status} />
        <AccountField label="Vai trò" value={(account.roles ?? []).map((role) => getAdminRoleLabel(role)).join(', ')} />
      </div> : null}
      {account ? <form onSubmit={saveAccountProfile} className="space-y-4 border-t border-zinc-200 pt-5">
        <div><h3 className="font-semibold text-zinc-900">Thông tin hồ sơ quản trị</h3><p className="mt-1 text-xs text-zinc-500">Có thể chỉnh độc lập với Managed Public Identity phía trên.</p></div>
        <div className="grid gap-4 sm:grid-cols-2">
          <Field label="Tên hiển thị quản trị" value={accountDraft.displayName} maxLength={100} disabled={accountSaving} onChange={(displayName) => setAccountDraft((value) => ({ ...value, displayName }))} />
          <Field label="Ngày sinh" type="date" max={latestAdultBirthDate} value={accountDraft.dateOfBirth} disabled={accountSaving} onChange={(dateOfBirth) => setAccountDraft((value) => ({ ...value, dateOfBirth }))} />
        </div>
        <label className="block text-sm font-semibold text-zinc-700">Giới thiệu quản trị<textarea value={accountDraft.bio} maxLength={500} disabled={accountSaving} onChange={(event) => setAccountDraft((value) => ({ ...value, bio: event.target.value }))} className="mt-2 min-h-24 w-full rounded-xl border border-zinc-300 px-3 py-2 font-normal outline-none transition focus:border-zinc-900 focus:ring-2 focus:ring-zinc-100" /><span className="mt-1 block text-right text-xs font-normal text-zinc-400">{accountDraft.bio.length}/500</span></label>
        <Button type="submit" loading={accountSaving} loadingLabel="Đang lưu hồ sơ quản trị..."><Save size={16} /> Lưu hồ sơ quản trị</Button>
      </form> : null}
    </section>
    <Modal open={passwordOpen} title="Đổi mật khẩu" onClose={closePassword} size="sm">
      <p className="mb-5 text-sm text-zinc-500">Sau khi đổi thành công, bạn sẽ phải đăng nhập lại trên tất cả thiết bị.</p>
      <form onSubmit={changePassword} className="space-y-4">
        {['currentPassword', 'newPassword', 'confirmPassword'].map((name, index) => <label key={name} className="block text-sm font-semibold text-zinc-700">
          {['Mật khẩu hiện tại', 'Mật khẩu mới', 'Xác nhận mật khẩu mới'][index]}
          <Input type="password" value={password[name]} onChange={(event) => setPassword((value) => ({ ...value, [name]: event.target.value }))} className="mt-2" />
        </label>)}
        {passwordError ? <p className="text-sm text-red-600">{passwordError}</p> : null}
        <div className="flex justify-end gap-3"><Button type="button" variant="secondary" onClick={closePassword} disabled={changingPassword}>Hủy</Button><Button type="submit" loading={changingPassword} loadingLabel="Đang đổi...">Đổi mật khẩu</Button></div>
      </form>
    </Modal>
  </section>;
}

function AccountField({ label, value }) {
  return <div><p className="text-xs font-semibold uppercase tracking-wide text-zinc-500">{label}</p><p className="mt-1 break-words font-medium text-zinc-900">{value || '—'}</p></div>;
}

function Field({ label, value, onChange, maxLength, max, type = 'text', disabled, required = true }) {
  return <label className="block text-sm font-semibold text-zinc-700">{label}<input type={type} value={value} required={required} maxLength={maxLength} max={max} disabled={disabled} onChange={(event) => onChange(event.target.value)} className="mt-2 h-11 w-full rounded-xl border border-zinc-300 px-3 font-normal outline-none transition focus:border-zinc-900 focus:ring-2 focus:ring-zinc-100" /></label>;
}
