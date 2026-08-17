import { useEffect, useMemo, useRef, useState } from 'react';
import { ChevronRight, KeyRound, Plus, RefreshCw, ShieldCheck, UserCheck, UsersRound, UserX } from 'lucide-react';
import { adminApi } from '../../../api/adminApi.js';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import Pagination from '../../../components/common/Pagination.jsx';
import { useAuth } from '../../auth/hooks/useAuth.js';
import { ADMIN_PERMISSIONS, getAdminRoleLabel } from '../constants/adminRbac.js';
import { useAdminToast } from '../hooks/useAdminToast.js';
import AdminStatusBadge from '../components/AdminStatusBadge.jsx';
import AdminPageHeader from '../components/AdminPageHeader.jsx';
import { canManageMasterProtectedAccount, isMasterAdmin } from '../utils/adminAccountPolicy.js';

const EMPTY_FORM = {
  email: '', password: '', confirmPassword: '', username: '', displayName: '', dateOfBirth: '', roleCodes: [],
};

const ADMIN_PAGE_SIZE = 10;
const EMPTY_PASSWORD_FORM = { newPassword: '', confirmPassword: '' };

const CREATE_FIELD_LABELS = Object.freeze({
  email: 'Email',
  username: 'Tên người dùng',
  displayName: 'Tên hiển thị',
  dateOfBirth: 'Ngày sinh',
  password: 'Mật khẩu',
  confirmPassword: 'Xác nhận mật khẩu',
});

/** Màn hình quản lý admin, kết hợp permission actor với chính sách bảo vệ Master Admin. */
export default function AdminManagementPage() {
  const auth = useAuth();
  const { showToast } = useAdminToast();
  const [admins, setAdmins] = useState([]);
  const [page, setPage] = useState(1);
  const [pagination, setPagination] = useState({ totalElements: 0, totalPages: 0 });
  const [roles, setRoles] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [form, setForm] = useState(EMPTY_FORM);
  const [showCreate, setShowCreate] = useState(false);
  const [selectedAdminId, setSelectedAdminId] = useState(null);
  const [selectedAdmin, setSelectedAdmin] = useState(null);
  const [detailBusy, setDetailBusy] = useState(false);
  const [detailError, setDetailError] = useState('');
  const [showPasswordForm, setShowPasswordForm] = useState(false);
  const [passwordForm, setPasswordForm] = useState(EMPTY_PASSWORD_FORM);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const listRef = useRef(null);

  const canCreate = auth.hasPermission(ADMIN_PERMISSIONS.ADMIN_CREATE);
  const canDisable = auth.hasPermission(ADMIN_PERMISSIONS.ADMIN_DISABLE);
  const canEnable = auth.hasPermission(ADMIN_PERMISSIONS.ADMIN_ENABLE);
  const canViewDetail = auth.hasPermission(ADMIN_PERMISSIONS.ADMIN_DETAIL_VIEW);
  const canResetPassword = auth.hasPermission(ADMIN_PERMISSIONS.ADMIN_PASSWORD_RESET);
  const canAssign = auth.hasPermission(ADMIN_PERMISSIONS.ADMIN_ROLE_ASSIGN);
  const canRevoke = auth.hasPermission(ADMIN_PERMISSIONS.ADMIN_ROLE_REVOKE);
  // Master Admin chỉ được xem tại màn hình này; mật khẩu do chính tài khoản đổi trong Hồ sơ.
  const selectedAdminIsMaster = isMasterAdmin(selectedAdmin);
  const canManageSelectedAdmin = canManageMasterProtectedAccount(selectedAdmin);

  async function load(requestedPage = page) {
    setBusy(true);
    setError('');
    try {
      const [adminPage, roleCatalog] = await Promise.all([
        adminApi.getAdmins({ keyword: keyword.trim() || undefined, page: requestedPage - 1, size: ADMIN_PAGE_SIZE }),
        adminApi.getAdminRoleCatalog(),
      ]);
      setAdmins(adminPage.content ?? []);
      setPagination({
        totalElements: adminPage.totalElements ?? 0,
        totalPages: adminPage.totalPages ?? 0,
      });
      setRoles(roleCatalog ?? []);
    } catch (requestError) {
      setError(requestError.message || 'Không thể tải dữ liệu quản trị viên.');
    } finally {
      setBusy(false);
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(load, 0);
    return () => window.clearTimeout(timer);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const availableRoles = useMemo(
    () => roles.map((role) => role.code).filter((roleCode) => roleCode !== 'SUPER_ADMIN'),
    [roles],
  );

  function changePage(nextPage) {
    if (busy) return;
    setPage(nextPage);
    listRef.current?.scrollTo({ top: 0, behavior: 'smooth' });
    load(nextPage);
  }

  function searchAdmins(event) {
    event.preventDefault();
    setPage(1);
    listRef.current?.scrollTo({ top: 0 });
    load(1);
  }

  async function submitCreate(event) {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      await adminApi.createAdmin(form);
      setForm(EMPTY_FORM);
      setShowCreate(false);
      showToast('Tạo tài khoản quản trị viên thành công.');
      setPage(1);
      await load(1);
    } catch (requestError) {
      showToast(requestError.message || 'Không thể tạo quản trị viên.', { type: 'error' });
      setBusy(false);
    }
  }

  async function changeRole(admin, roleCode, assigned) {
    if (!canManageMasterProtectedAccount(admin)) return;
    setBusy(true);
    setError('');
    try {
      const updated = assigned
        ? await adminApi.revokeAdminRole(admin.id, roleCode)
        : await adminApi.assignAdminRole(admin.id, roleCode);
      setSelectedAdmin(updated);
      showToast(roleCode === 'COLLABORATOR'
        ? assigned
          ? 'Đã thu hồi vai trò Cộng tác viên và vô hiệu hóa Managed Social Identity.'
          : 'Đã gán vai trò Cộng tác viên và kích hoạt đầy đủ chức năng cùng Managed Social Identity.'
        : `Đã ${assigned ? 'thu hồi' : 'gán'} vai trò ${getAdminRoleLabel(roleCode)}.`);
      await load();
    } catch (requestError) {
      setError(requestError.message || 'Không thể thay đổi vai trò.');
      setBusy(false);
    }
  }

  async function disable(admin) {
    if (!canManageMasterProtectedAccount(admin)) return;
    if (!window.confirm(`Vô hiệu hóa tài khoản ${admin.email}?`)) return;
    setBusy(true);
    setError('');
    try {
      const updated = await adminApi.disableAdmin(admin.id);
      setSelectedAdmin(updated);
      showToast('Đã vô hiệu hóa tài khoản quản trị viên.');
      await load();
    } catch (requestError) {
      setError(requestError.message || 'Không thể vô hiệu hóa quản trị viên.');
      setBusy(false);
    }
  }

  async function enable(admin) {
    if (!window.confirm(`Mở khóa tài khoản ${admin.email}?`)) return;
    setBusy(true);
    setError('');
    try {
      const updated = await adminApi.enableAdmin(admin.id);
      setSelectedAdmin(updated);
      showToast('Đã mở khóa tài khoản quản trị viên.');
      await load();
    } catch (requestError) {
      setError(requestError.message || 'Không thể mở khóa quản trị viên.');
      setBusy(false);
    }
  }

  function toggleFormRole(roleCode) {
    setForm((current) => ({
      ...current,
      roleCodes: current.roleCodes.includes(roleCode)
        ? current.roleCodes.filter((code) => code !== roleCode)
        : [...current.roleCodes, roleCode],
    }));
  }

  async function openAdminDetail(adminId) {
    if (!canViewDetail) return;
    setSelectedAdminId(adminId);
    setSelectedAdmin(null);
    setDetailError('');
    setShowPasswordForm(false);
    setPasswordForm(EMPTY_PASSWORD_FORM);
    setDetailBusy(true);
    try {
      setSelectedAdmin(await adminApi.getAdmin(adminId));
    } catch (requestError) {
      setDetailError(requestError.message || 'Không thể tải chi tiết quản trị viên.');
    } finally {
      setDetailBusy(false);
    }
  }

  function closeAdminDetail() {
    if (busy) return;
    setSelectedAdminId(null);
    setSelectedAdmin(null);
    setDetailError('');
    setShowPasswordForm(false);
    setPasswordForm(EMPTY_PASSWORD_FORM);
  }

  async function submitPasswordReset(event) {
    event.preventDefault();
    if (!canManageMasterProtectedAccount(selectedAdmin)) return;
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setDetailError('Xác nhận mật khẩu không khớp.');
      return;
    }
    setBusy(true);
    setDetailError('');
    try {
      await adminApi.resetAdminPassword(selectedAdmin.id, passwordForm);
      setPasswordForm(EMPTY_PASSWORD_FORM);
      setShowPasswordForm(false);
      showToast('Đã cấp lại mật khẩu và thu hồi các phiên đăng nhập của tài khoản.');
    } catch (requestError) {
      setDetailError(requestError.message || 'Không thể cấp lại mật khẩu quản trị viên.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="flex h-[calc(100vh-4rem)] min-h-0 flex-col gap-4 overflow-hidden lg:h-[calc(100vh-6rem)]">
      <AdminPageHeader
        icon={UsersRound}
        title="Quản trị viên"
        description="Quản lý tài khoản, vai trò và trạng thái của đội ngũ quản trị."
        actions={(
          <>
            <Button variant="secondary" size="sm" onClick={() => load(page)} disabled={busy} title="Tải lại danh sách" aria-label="Tải lại danh sách">
              <RefreshCw size={16} />
            </Button>
            {canCreate ? <Button size="sm" onClick={() => setShowCreate(true)}><Plus size={16} /> Tạo quản trị viên</Button> : null}
          </>
        )}
      />

      {error && <div role="alert" className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</div>}

      <form onSubmit={searchAdmins} className="flex shrink-0 gap-2">
        <input value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="Tìm email, username, tên..." className="w-full rounded-lg border border-zinc-300 px-3 py-2" />
        <button type="submit" disabled={busy} className="rounded-lg border px-4 py-2">Tìm</button>
      </form>

      <div ref={listRef} className="min-h-0 flex-1 space-y-3 overflow-y-auto overscroll-contain pr-2">
        {admins.map((admin) => (
          <button
            type="button"
            key={admin.id}
            disabled={!canViewDetail}
            onClick={() => openAdminDetail(admin.id)}
            className="block w-full rounded-xl border border-zinc-200 p-5 text-left transition hover:border-zinc-400 hover:bg-zinc-50 disabled:cursor-default disabled:hover:border-zinc-200 disabled:hover:bg-white"
          >
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <h2 className="font-semibold text-zinc-950">{admin.displayName || admin.username || admin.email}</h2>
                <div className="mt-1 flex flex-wrap items-center gap-2 text-sm text-zinc-500">
                  <span>{admin.email}</span>
                  <AdminStatusBadge status={admin.status} />
                </div>
              </div>
              {canViewDetail && <ChevronRight size={18} className="text-zinc-400" aria-hidden="true" />}
            </div>
            <div className="mt-4 flex flex-wrap gap-2">
              {(admin.roles || []).map((roleCode) => (
                <span key={roleCode} className="flex items-center gap-1 rounded-full border border-zinc-300 bg-zinc-50 px-3 py-1 text-xs text-zinc-700">
                  <ShieldCheck size={13} /> {getAdminRoleLabel(
                    roleCode, roles.find((role) => role.code === roleCode)?.displayName,
                  )}
                </span>
              ))}
            </div>
          </button>
        ))}
        {!busy && admins.length === 0 && <p className="py-8 text-center text-zinc-500">Không có quản trị viên phù hợp.</p>}
      </div>

      {pagination.totalPages > 1 && (
        <div className="shrink-0 overflow-hidden rounded-lg border border-zinc-200">
          <Pagination
            currentPage={page}
            totalPages={pagination.totalPages}
            totalItems={pagination.totalElements}
            pageSize={ADMIN_PAGE_SIZE}
            onPageChange={changePage}
          />
        </div>
      )}

      <Modal
        open={selectedAdminId !== null}
        title="Thông tin quản trị viên"
        size="lg"
        onClose={closeAdminDetail}
        footer={<button type="button" disabled={busy} onClick={closeAdminDetail} className="rounded-lg border border-zinc-300 px-4 py-2 text-sm font-semibold">Đóng</button>}
      >
        {detailBusy && <p className="py-10 text-center text-sm text-zinc-500">Đang tải thông tin...</p>}
        {detailError && <div role="alert" className="mb-4 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">{detailError}</div>}
        {!detailBusy && selectedAdmin && (
          <div className="space-y-5">
            <dl className="grid gap-3 rounded-xl bg-zinc-50 p-4 text-sm sm:grid-cols-2">
              <div><dt className="text-zinc-500">Tên hiển thị</dt><dd className="font-semibold text-zinc-950">{selectedAdmin.displayName || '—'}</dd></div>
              <div><dt className="text-zinc-500">Username</dt><dd className="font-semibold text-zinc-950">{selectedAdmin.username ? `@${selectedAdmin.username}` : '—'}</dd></div>
              <div><dt className="text-zinc-500">Email</dt><dd className="break-all font-semibold text-zinc-950">{selectedAdmin.email || '—'}</dd></div>
              <div><dt className="text-zinc-500">Trạng thái</dt><dd className="mt-1"><AdminStatusBadge status={selectedAdmin.status} /></dd></div>
              <div><dt className="text-zinc-500">Ngày sinh</dt><dd className="font-semibold text-zinc-950">{selectedAdmin.dateOfBirth || '—'}</dd></div>
              <div><dt className="text-zinc-500">Ngày tạo</dt><dd className="font-semibold text-zinc-950">{selectedAdmin.createdAt ? new Date(selectedAdmin.createdAt).toLocaleString('vi-VN') : '—'}</dd></div>
            </dl>

            <section>
              <h3 className="mb-2 text-sm font-semibold text-zinc-900">Vai trò và quyền</h3>
              {selectedAdminIsMaster ? (
                <div className="mb-2 inline-flex items-center gap-1 rounded-full border border-amber-300 bg-amber-50 px-3 py-1.5 text-xs font-semibold text-amber-800">
                  <ShieldCheck size={13} /> Master Admin · vai trò và quyền cố định
                </div>
              ) : null}
              <div className="flex flex-wrap gap-2">
                {availableRoles.map((roleCode) => {
                  const assigned = selectedAdmin.roles?.includes(roleCode);
                  const allowed = canManageSelectedAdmin && (assigned ? canRevoke : canAssign);
                  const roleDefinition = roles.find((role) => role.code === roleCode);
                  return (
                    <button
                      type="button"
                      key={roleCode}
                      disabled={!allowed || busy}
                      onClick={() => changeRole(selectedAdmin, roleCode, assigned)}
                      title={`${getAdminRoleLabel(roleCode, roleDefinition?.displayName)}: ${(roleDefinition?.permissions || []).join(', ') || 'Chưa có quyền chuyên biệt'}`}
                      className={`flex items-center gap-1 rounded-full border px-3 py-1.5 text-xs ${assigned ? 'border-zinc-900 bg-zinc-900 text-white' : 'border-zinc-300 text-zinc-600'} disabled:opacity-50`}
                    >
                      <ShieldCheck size={13} /> {getAdminRoleLabel(roleCode, roleDefinition?.displayName)}
                    </button>
                  );
                })}
              </div>
            </section>

            {selectedAdminIsMaster && (
              <p className="rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
                Master Admin không thể được phân quyền, cấp lại mật khẩu hoặc vô hiệu hóa tại đây. Tài khoản này vẫn có thể tự đổi mật khẩu trong Hồ sơ.
              </p>
            )}

            <section className="flex flex-wrap gap-2 border-t border-zinc-200 pt-4">
              {canManageSelectedAdmin && canDisable && selectedAdmin.status === 'ACTIVE' && (
                <button type="button" onClick={() => disable(selectedAdmin)} disabled={busy} className="flex items-center gap-2 rounded-lg border border-red-200 px-3 py-2 text-sm text-red-700"><UserX size={15} /> Vô hiệu hóa</button>
              )}
              {canEnable && selectedAdmin.status === 'BLOCKED' && (
                <button type="button" onClick={() => enable(selectedAdmin)} disabled={busy} className="flex items-center gap-2 rounded-lg border border-emerald-200 px-3 py-2 text-sm text-emerald-700"><UserCheck size={15} /> Mở khóa</button>
              )}
              {canManageSelectedAdmin && canResetPassword && (
                <button type="button" onClick={() => setShowPasswordForm((current) => !current)} disabled={busy} className="flex items-center gap-2 rounded-lg border border-amber-200 px-3 py-2 text-sm text-amber-800"><KeyRound size={15} /> Cấp lại mật khẩu</button>
              )}
            </section>

            {showPasswordForm && (
              <form onSubmit={submitPasswordReset} className="space-y-3 rounded-xl border border-amber-200 bg-amber-50 p-4">
                <div>
                  <h3 className="font-semibold text-zinc-950">Cấp mật khẩu mới</h3>
                  <p className="text-sm text-zinc-600">Sau khi lưu, toàn bộ phiên đăng nhập hiện tại của tài khoản sẽ bị thu hồi.</p>
                </div>
                <div className="grid gap-3 sm:grid-cols-2">
                  <label className="text-sm font-medium text-zinc-700">Mật khẩu mới
                    <input required minLength={8} maxLength={72} type="password" autoComplete="new-password" value={passwordForm.newPassword} onChange={(event) => setPasswordForm((current) => ({ ...current, newPassword: event.target.value }))} className="mt-1.5 w-full rounded-lg border border-zinc-300 bg-white px-3 py-2.5" />
                  </label>
                  <label className="text-sm font-medium text-zinc-700">Xác nhận mật khẩu
                    <input required type="password" autoComplete="new-password" value={passwordForm.confirmPassword} onChange={(event) => setPasswordForm((current) => ({ ...current, confirmPassword: event.target.value }))} className="mt-1.5 w-full rounded-lg border border-zinc-300 bg-white px-3 py-2.5" />
                  </label>
                </div>
                <div className="flex justify-end gap-2">
                  <button type="button" disabled={busy} onClick={() => { setShowPasswordForm(false); setPasswordForm(EMPTY_PASSWORD_FORM); }} className="rounded-lg border border-zinc-300 bg-white px-4 py-2 text-sm font-semibold">Hủy</button>
                  <button type="submit" disabled={busy} className="rounded-lg bg-zinc-900 px-4 py-2 text-sm font-semibold text-white disabled:opacity-50">{busy ? 'Đang lưu...' : 'Xác nhận cấp lại'}</button>
                </div>
              </form>
            )}
          </div>
        )}
      </Modal>

      <Modal
        open={showCreate}
        title="Tạo tài khoản quản trị viên"
        size="lg"
        onClose={busy ? undefined : () => { setShowCreate(false); setForm(EMPTY_FORM); }}
        footer={(
          <>
            <button
              type="button"
              disabled={busy}
              onClick={() => { setShowCreate(false); setForm(EMPTY_FORM); }}
              className="rounded-lg border border-zinc-300 px-4 py-2 text-sm font-semibold text-zinc-700 hover:bg-zinc-50 disabled:opacity-50"
            >
              Hủy
            </button>
            <button
              type="submit"
              form="create-admin-form"
              disabled={busy || form.roleCodes.length === 0}
              className="rounded-lg bg-zinc-900 px-5 py-2 text-sm font-semibold text-white hover:bg-zinc-800 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {busy ? 'Đang tạo...' : 'Tạo quản trị viên'}
            </button>
          </>
        )}
      >
        <form id="create-admin-form" onSubmit={submitCreate} className="grid gap-4 md:grid-cols-2">
          {Object.keys(CREATE_FIELD_LABELS).map((field) => (
            <label key={field} className="text-sm font-medium text-zinc-700">
              {CREATE_FIELD_LABELS[field]}
              <input
                required
                autoComplete={field === 'email' ? 'email' : field === 'password' ? 'new-password' : field === 'confirmPassword' ? 'new-password' : 'off'}
                type={field === 'password' || field === 'confirmPassword' ? 'password' : field === 'dateOfBirth' ? 'date' : field === 'email' ? 'email' : 'text'}
                value={form[field]}
                onChange={(event) => setForm((current) => ({ ...current, [field]: event.target.value }))}
                className="mt-1.5 w-full rounded-lg border border-zinc-300 bg-white px-3 py-2.5 outline-none transition focus:border-zinc-900 focus:ring-2 focus:ring-zinc-100"
              />
            </label>
          ))}
          <fieldset className="md:col-span-2">
            <legend className="mb-2 text-sm font-semibold text-zinc-800">Vai trò</legend>
            <p className="mb-3 text-xs text-zinc-500">SUPER_ADMIN chỉ dành cho tài khoản Bootstrap và không thể gán tại đây.</p>
            <div className="grid gap-2 sm:grid-cols-2">{availableRoles.map((role) => (
              <label key={role} className={`flex cursor-pointer items-center gap-2 rounded-lg border px-3 py-2.5 text-sm transition ${form.roleCodes.includes(role) ? 'border-zinc-900 bg-zinc-50' : 'border-zinc-300 bg-white hover:border-zinc-500'}`}>
                <input type="checkbox" checked={form.roleCodes.includes(role)} onChange={() => toggleFormRole(role)} />
                {getAdminRoleLabel(role)}
              </label>
            ))}</div>
          </fieldset>
        </form>
      </Modal>
    </section>
  );
}
