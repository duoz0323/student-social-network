import { useEffect, useMemo, useState } from 'react';
import { Check, ChevronDown, LoaderCircle, Plus } from 'lucide-react';
import { adminApi } from '../../../api/adminApi.js';
import Modal from '../../../components/common/Modal.jsx';
import { useAuth } from '../../auth/hooks/useAuth.js';
import { useAdminToast } from '../hooks/useAdminToast.js';
import {
  ADMIN_PERMISSIONS,
  NON_DELEGABLE_ADMIN_PERMISSIONS,
  getAdminRoleLabel,
} from '../constants/adminRbac.js';

const NON_DELEGABLE_PERMISSION_SET = new Set(NON_DELEGABLE_ADMIN_PERMISSIONS);
const FIXED_SYSTEM_ROLES = new Set(['SUPER_ADMIN', 'COLLABORATOR']);

const MODULE_LABELS = Object.freeze({
  DASHBOARD: 'Tổng quan',
  USER_MANAGEMENT: 'Quản lý người dùng',
  POST_MANAGEMENT: 'Quản lý bài viết',
  HASHTAG_MANAGEMENT: 'Quản lý hashtag',
  REPORT_MANAGEMENT: 'Quản lý báo cáo',
  ADMIN_MANAGEMENT: 'Quản trị hệ thống',
});

const MODULE_ORDER = Object.freeze([
  'ADMIN_MANAGEMENT',
  'USER_MANAGEMENT',
  'POST_MANAGEMENT',
  'HASHTAG_MANAGEMENT',
  'REPORT_MANAGEMENT',
  'DASHBOARD',
]);

/** Ma trận role-permission; mỗi click checkbox lưu ngay một snapshot quyền nguyên tử. */
export default function AdminRolePermissionsPage() {
  const auth = useAuth();
  const { showToast } = useAdminToast();
  const [roles, setRoles] = useState([]);
  const [permissions, setPermissions] = useState([]);
  const [selectedRoleCode, setSelectedRoleCode] = useState('');
  const [savingCode, setSavingCode] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [roleName, setRoleName] = useState('');
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState('');
  const canUpdate = auth.hasAdminRole('SUPER_ADMIN');

  async function load() {
    setError('');
    try {
      const [roleList, permissionList] = await Promise.all([
        adminApi.getRolesForPermissionManagement(),
        adminApi.getPermissionCatalog(),
      ]);
      setRoles(roleList ?? []);
      setPermissions(permissionList ?? []);
      setSelectedRoleCode((current) => current || roleList?.[0]?.code || '');
    } catch (requestError) {
      setError(requestError.message || 'Không thể tải ma trận phân quyền.');
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(load, 0);
    return () => window.clearTimeout(timer);
  }, []);

  const selectedRole = roles.find((role) => role.code === selectedRoleCode);
  const selectedRoleLabel = selectedRole
    ? getAdminRoleLabel(selectedRole.code, selectedRole.displayName)
    : '';
  const groupedPermissions = useMemo(() => permissions.reduce((groups, permission) => {
    const module = permission.module || 'ADMIN_MANAGEMENT';
    return { ...groups, [module]: [...(groups[module] ?? []), permission] };
  }, {}), [permissions]);
  const permissionGroups = MODULE_ORDER
    .filter((module) => groupedPermissions[module]?.length)
    .map((module) => [module, groupedPermissions[module]]);

  async function togglePermission(permissionCode) {
    if (!selectedRole || !canUpdate || FIXED_SYSTEM_ROLES.has(selectedRole.code)
        || permissionCode === ADMIN_PERMISSIONS.DASHBOARD_BASIC_VIEW
        || NON_DELEGABLE_PERMISSION_SET.has(permissionCode) || savingCode) return;
    const current = new Set(selectedRole.permissions ?? []);
    const isRemoving = current.has(permissionCode);
    if (isRemoving) current.delete(permissionCode);
    else current.add(permissionCode);
    current.add(ADMIN_PERMISSIONS.DASHBOARD_BASIC_VIEW);
    const nextCodes = [...current];
    const previousRoles = roles;
    setRoles((items) => items.map((role) => role.code === selectedRole.code
      ? { ...role, permissions: nextCodes } : role));
    setSavingCode(permissionCode);
    setError('');
    try {
      const updated = await adminApi.updateRolePermissions(selectedRole.code, nextCodes);
      setRoles((items) => items.map((role) => role.code === updated.code ? updated : role));
      const permissionLabel = permissions.find((item) => item.code === permissionCode)?.description || 'quyền đã chọn';
      showToast(`${isRemoving ? 'Đã gỡ' : 'Đã cấp'} “${permissionLabel}” cho ${selectedRoleLabel}.`);
    } catch (requestError) {
      setRoles(previousRoles);
      showToast(requestError.message || 'Không thể cập nhật quyền.', { type: 'error' });
    } finally {
      setSavingCode('');
    }
  }

  async function createRole(event) {
    event.preventDefault();
    const name = roleName.trim().replace(/\s+/g, ' ');
    if (name.length < 2) {
      setError('Tên vai trò phải có ít nhất 2 ký tự.');
      return;
    }
    setCreating(true);
    setError('');
    try {
      const created = await adminApi.createAdminRole(name);
      setRoles((items) => [...items, created]);
      setSelectedRoleCode(created.code);
      setRoleName('');
      setShowCreate(false);
      showToast(`Đã tạo vai trò “${created.displayName}”.`);
    } catch (requestError) {
      setError(requestError.message || 'Không thể tạo vai trò mới.');
    } finally {
      setCreating(false);
    }
  }

  return (
    <section className="min-h-full rounded-2xl bg-white p-5 sm:p-7">
      <header className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-zinc-950">Phân quyền</h1>
          <p className="mt-1 text-sm text-zinc-500">Tạo vai trò mới và cấu hình quyền truy cập cho từng vai trò quản trị.</p>
        </div>
        {canUpdate && (
          <button type="button" onClick={() => setShowCreate(true)} className="flex items-center gap-2 rounded-lg bg-zinc-900 px-4 py-2 text-sm font-semibold text-white">
            <Plus size={16} /> Tạo vai trò
          </button>
        )}
      </header>

      {error && <p role="alert" className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</p>}

      <div className="mt-1 max-w-xs">
        <label htmlFor="admin-role-select" className="mb-2 block text-xs font-medium text-slate-600">
          Chọn vai trò
        </label>
        <div className="relative">
          <select
            id="admin-role-select"
            value={selectedRoleCode}
            onChange={(event) => setSelectedRoleCode(event.target.value)}
            className="h-11 w-full appearance-none rounded-lg border border-zinc-300 bg-white px-4 pr-10 text-sm font-semibold text-zinc-800 shadow-sm outline-none transition focus:border-zinc-900 focus:ring-2 focus:ring-zinc-100"
          >
            {roles.map((role) => (
              <option key={role.code} value={role.code}>
                {getAdminRoleLabel(role.code, role.displayName)}
              </option>
            ))}
          </select>
          <ChevronDown size={17} className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-slate-600" />
        </div>
      </div>

      {selectedRole && (
        <div className="mt-5 border-t border-slate-200 pt-5">
          {FIXED_SYSTEM_ROLES.has(selectedRole.code) && (
            <p className="mb-4 text-xs font-medium text-red-600">
              {selectedRole.code === 'SUPER_ADMIN'
                ? 'Master Admin luôn có toàn bộ quyền và không thể chỉnh sửa.'
                : 'Cộng tác viên là vai trò hệ thống; các chức năng và quyền được gắn cố định vào vai trò này.'}
            </p>
          )}

          <div className="space-y-3">
            {permissionGroups.map(([module, items], index) => (
              <details
                key={module}
                defaultOpen={index === 0}
                className="group overflow-hidden rounded-lg border border-zinc-300 bg-white open:border-zinc-900 open:ring-1 open:ring-zinc-900"
              >
                <summary className="flex cursor-pointer list-none items-center justify-between bg-white px-4 py-3 text-sm font-semibold text-zinc-900">
                  <span>{MODULE_LABELS[module] || module}</span>
                  <ChevronDown size={16} className="transition-transform duration-200 group-open:rotate-180" />
                </summary>
                <div className="grid gap-3 border-t border-zinc-200 p-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                  {items.map((permission) => {
                    const checked = selectedRole.code === 'SUPER_ADMIN'
                      || selectedRole.permissions?.includes(permission.code);
                    const locked = !canUpdate || FIXED_SYSTEM_ROLES.has(selectedRole.code)
                      || permission.code === ADMIN_PERMISSIONS.DASHBOARD_BASIC_VIEW
                      || NON_DELEGABLE_PERMISSION_SET.has(permission.code);
                    return (
                      <button
                        type="button"
                        key={permission.code}
                        disabled={locked || Boolean(savingCode)}
                        onClick={() => togglePermission(permission.code)}
                        className={`min-h-20 flex items-start gap-3 rounded-md border p-3 text-left transition ${checked
                          ? 'border-zinc-900 bg-zinc-100' : 'border-zinc-300 bg-white hover:border-zinc-500'} disabled:cursor-not-allowed`}
                      >
                        <span className={`mt-0.5 flex h-4 w-4 shrink-0 items-center justify-center rounded-sm border ${checked
                          ? 'border-zinc-900 bg-zinc-900 text-white' : 'border-zinc-400 bg-white'}`}
                        >
                          {savingCode === permission.code ? <LoaderCircle size={11} className="animate-spin" /> : checked ? <Check size={11} strokeWidth={3} /> : null}
                        </span>
                        <span>
                          <strong className="block text-sm leading-5 text-slate-900">{permission.description}</strong>
                          {NON_DELEGABLE_PERMISSION_SET.has(permission.code) && selectedRole.code !== 'SUPER_ADMIN' ? (
                            <small className="mt-1 block text-xs text-amber-700">Chỉ tài khoản Bootstrap</small>
                          ) : null}
                        </span>
                      </button>
                    );
                  })}
                </div>
              </details>
            ))}
          </div>
        </div>
      )}

      <Modal
        open={showCreate}
        title="Tạo vai trò quản trị"
        onClose={creating ? undefined : () => { setShowCreate(false); setRoleName(''); }}
        footer={(
          <>
            <button type="button" disabled={creating} onClick={() => { setShowCreate(false); setRoleName(''); }} className="rounded-lg border border-zinc-300 px-4 py-2 text-sm font-semibold text-zinc-700">Hủy</button>
            <button type="submit" form="create-admin-role-form" disabled={creating || roleName.trim().length < 2} className="rounded-lg bg-zinc-900 px-4 py-2 text-sm font-semibold text-white disabled:opacity-50">
              {creating ? 'Đang tạo...' : 'Tạo vai trò'}
            </button>
          </>
        )}
      >
        <form id="create-admin-role-form" onSubmit={createRole} className="space-y-3">
          <label htmlFor="admin-role-name" className="block text-sm font-semibold text-zinc-800">Tên vai trò</label>
          <input
            id="admin-role-name"
            required
            minLength={2}
            maxLength={100}
            value={roleName}
            onChange={(event) => setRoleName(event.target.value)}
            placeholder="Ví dụ: Quản lý sự kiện"
            autoComplete="off"
            className="w-full rounded-lg border border-zinc-300 px-3 py-2.5 outline-none focus:border-zinc-900 focus:ring-2 focus:ring-zinc-100"
          />
          <p className="text-xs leading-5 text-zinc-500">Vai trò mới sẽ có quyền xem Tổng quan. Sau khi tạo, bạn có thể bật thêm các quyền trong ma trận bên dưới.</p>
        </form>
      </Modal>
    </section>
  );
}
