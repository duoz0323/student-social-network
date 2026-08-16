import { useEffect, useMemo, useState } from 'react';
import { Eye, EyeOff, KeyRound, Save, ShieldCheck, UserCircle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { adminApi } from '../../../api/adminApi.js';
import { isRequestCanceled } from '../../../api/apiError.js';
import Button from '../../../components/common/Button.jsx';
import Input from '../../../components/common/Input.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { useAuth } from '../../auth/hooks/useAuth.js';
import { getAdminRoleLabel } from '../constants/adminRbac.js';
import { useAdminToast } from '../hooks/useAdminToast.js';
import AdminStatusBadge from '../components/AdminStatusBadge.jsx';
import {
  buildAdminProfilePayload,
  getLatestAdultBirthDate,
  validateAdminProfileDraft,
} from '../utils/adminUserProfile.js';
import { validateAdminPasswordDraft } from '../utils/adminProfilePassword.js';

const EMPTY_PROFILE = Object.freeze({ displayName: '', dateOfBirth: '', bio: '' });
const EMPTY_PASSWORD = Object.freeze({ currentPassword: '', newPassword: '', confirmPassword: '' });

/** Hồ sơ tự quản lý của ADMIN; các trường định danh và quyền chỉ được hiển thị để tránh tự nâng quyền. */
export default function AdminProfilePage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const { showToast } = useAdminToast();
  const [profile, setProfile] = useState(null);
  const [draft, setDraft] = useState(EMPTY_PROFILE);
  const [password, setPassword] = useState(EMPTY_PASSWORD);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [changingPassword, setChangingPassword] = useState(false);
  const [showPasswordPanel, setShowPasswordPanel] = useState(false);
  const [showPasswords, setShowPasswords] = useState(false);
  const [error, setError] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const latestAdultBirthDate = useMemo(() => getLatestAdultBirthDate(), []);

  useEffect(() => {
    const controller = new AbortController();
    adminApi.getProfile(controller.signal)
      .then((data) => {
        setProfile(data);
        setDraft({
          displayName: data.displayName ?? '',
          dateOfBirth: data.dateOfBirth ?? '',
          bio: data.bio ?? '',
        });
      })
      .catch((requestError) => {
        // React StrictMode có thể hủy request của vòng effect thử; đây không phải lỗi cần hiển thị cho người dùng.
        if (!isRequestCanceled(requestError)) {
          setError(requestError.message || 'Không thể tải hồ sơ quản trị viên.');
        }
      })
      .finally(() => {
        // Không để request cũ đã hủy kết thúc trạng thái loading của request mới đang chạy.
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, []);

  async function saveProfile(event) {
    event.preventDefault();
    if (!validateAdminProfileDraft(draft)) {
      setError('Vui lòng kiểm tra tên hiển thị, ngày sinh và phần giới thiệu.');
      return;
    }
    setSaving(true);
    setError('');
    try {
      const updated = await adminApi.updateProfile(buildAdminProfilePayload(draft));
      setProfile(updated);
      setDraft({ displayName: updated.displayName, dateOfBirth: updated.dateOfBirth, bio: updated.bio ?? '' });
      auth.updateCurrentUser({
        displayName: updated.displayName,
        avatarUrl: updated.avatarUrl,
        birthDate: updated.dateOfBirth,
        bio: updated.bio,
      });
      showToast('Đã cập nhật hồ sơ quản trị viên.');
    } catch (requestError) {
      setError(requestError.message || 'Không thể cập nhật hồ sơ quản trị viên.');
    } finally {
      setSaving(false);
    }
  }

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

  function closePasswordModal() {
    if (changingPassword) return;
    setShowPasswordPanel(false);
    setPassword(EMPTY_PASSWORD);
    setPasswordError('');
    setShowPasswords(false);
  }

  if (loading) return <div className="py-20 text-center text-sm text-zinc-500">Đang tải hồ sơ quản trị viên...</div>;
  if (!profile) return <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{error}</div>;

  return (
    <div className="space-y-8">
      <header>
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-zinc-500">Tài khoản quản trị viên</p>
      </header>

      <div>
      <section className="min-w-0 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
        <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-4">
            <div className="flex h-16 w-16 items-center justify-center overflow-hidden rounded-full bg-zinc-100 text-zinc-500">
              {profile.avatarUrl ? <img src={profile.avatarUrl} alt="Ảnh đại diện" className="h-full w-full object-cover" /> : <UserCircle size={38} />}
            </div>
            <div>
              <h2 className="text-xl font-semibold text-zinc-950">{profile.displayName}</h2>
              <p className="text-sm text-zinc-500">@{profile.username}</p>
            </div>
          </div>
          <Button
            onClick={() => {
              setShowPasswordPanel(true);
              setPasswordError('');
            }}
            aria-haspopup="dialog"
            className="shrink-0 sm:self-center"
          >
            <KeyRound size={17} /> Đổi mật khẩu
          </Button>
        </div>

        <div className="mb-6 grid gap-4 rounded-xl bg-zinc-50 p-4 sm:grid-cols-2">
          <ReadOnlyField label="Email" value={profile.email} />
          <ReadOnlyField label="Trạng thái" value={<AdminStatusBadge status={profile.status} />} />
          <ReadOnlyField label="Vai trò" value={[...profile.roles].map((role) => getAdminRoleLabel(role)).join(', ')} />
          <ReadOnlyField label="Tên đăng nhập" value={`@${profile.username}`} />
        </div>

        <form onSubmit={saveProfile} className="space-y-5">
          <div className="grid gap-5 sm:grid-cols-2">
            <Field label="Tên hiển thị">
              <Input value={draft.displayName} maxLength={100} onChange={(event) => setDraft((current) => ({ ...current, displayName: event.target.value }))} />
            </Field>
            <Field label="Ngày sinh">
              <Input type="date" max={latestAdultBirthDate} value={draft.dateOfBirth} onChange={(event) => setDraft((current) => ({ ...current, dateOfBirth: event.target.value }))} />
            </Field>
          </div>
          <Field label="Giới thiệu">
            <textarea className="app-field min-h-28 w-full rounded-xl border p-3 text-sm outline-none" maxLength={500} value={draft.bio} onChange={(event) => setDraft((current) => ({ ...current, bio: event.target.value }))} />
            <p className="mt-1 text-right text-xs text-zinc-400">{draft.bio.length}/500</p>
          </Field>
          {error ? <p className="text-sm text-red-600">{error}</p> : null}
          <Button type="submit" loading={saving} loadingLabel="Đang lưu..."><Save size={16} /> Lưu thay đổi</Button>
        </form>
      </section>
      <Modal open={showPasswordPanel} title="Đổi mật khẩu" onClose={closePasswordModal} size="sm">
        <p className="mb-5 text-sm text-zinc-500">Sau khi đổi thành công, bạn sẽ phải đăng nhập lại trên tất cả thiết bị.</p>
        <form onSubmit={changePassword} className="space-y-4">
          {['currentPassword', 'newPassword', 'confirmPassword'].map((name, index) => (
            <Field key={name} label={['Mật khẩu hiện tại', 'Mật khẩu mới', 'Xác nhận mật khẩu mới'][index]}>
              <div className="relative">
                <Input type={showPasswords ? 'text' : 'password'} autoComplete={index ? 'new-password' : 'current-password'} value={password[name]} maxLength={72} onChange={(event) => setPassword((current) => ({ ...current, [name]: event.target.value }))} className="pr-11" />
                <button type="button" aria-label={showPasswords ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'} onClick={() => setShowPasswords((current) => !current)} className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-500">{showPasswords ? <EyeOff size={18} /> : <Eye size={18} />}</button>
              </div>
            </Field>
          ))}
          {passwordError ? <p className="text-sm text-red-600">{passwordError}</p> : null}
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={closePasswordModal} disabled={changingPassword}>Hủy</Button>
            <Button type="submit" loading={changingPassword} loadingLabel="Đang đổi mật khẩu..."><ShieldCheck size={16} /> Đổi mật khẩu</Button>
          </div>
        </form>
      </Modal>
      </div>
    </div>
  );
}

function Field({ label, children }) {
  return <label className="block"><span className="mb-2 block text-sm font-semibold text-zinc-700">{label}</span>{children}</label>;
}

function ReadOnlyField({ label, value }) {
  return <div><p className="text-xs font-semibold uppercase tracking-wide text-zinc-400">{label}</p><p className="mt-1 text-sm font-medium text-zinc-800">{value || 'Chưa cập nhật'}</p></div>;
}
