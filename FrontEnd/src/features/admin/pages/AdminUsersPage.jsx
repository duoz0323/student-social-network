import { useEffect, useRef, useState } from 'react';
import { Search } from 'lucide-react';
import { adminApi } from '../../../api/index.js';
import DataTable from '../../../components/common/DataTable.jsx';
import { LoadingState } from '../../../components/common/StateBlock.jsx';
import BlockUserDialog from '../components/BlockUserDialog.jsx';
import AdminEditUserProfileDialog from '../components/AdminEditUserProfileDialog.jsx';
import AdminUserDetailDialog from '../components/AdminUserDetailDialog.jsx';
import AdminUserAnalytics from '../components/AdminUserAnalytics.jsx';
import AdminStatusBadge from '../components/AdminStatusBadge.jsx';
import { useAdminToast } from '../hooks/useAdminToast.js';
import { useAuth } from '../../auth/hooks/useAuth.js';
import { ADMIN_PERMISSIONS } from '../constants/adminRbac.js';
import { getAdminStatusLabel } from '../constants/adminStatus.js';

export default function AdminUsersPage() {
  const { showToast } = useAdminToast();
  const auth = useAuth();
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [result, setResult] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [blockTarget, setBlockTarget] = useState(null);
  const [actionUserId, setActionUserId] = useState(null);
  const [selectedUserId, setSelectedUserId] = useState(null);
  const [userDetail, setUserDetail] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState('');
  const [editTarget, setEditTarget] = useState(null);
  const [editSubmitting, setEditSubmitting] = useState(false);
  const [editError, setEditError] = useState('');
  const [statisticsRevision, setStatisticsRevision] = useState(0);
  const detailRequestRef = useRef(null);

  async function load(signal) {
    setLoading(true);
    try {
      setResult(await adminApi.getUsers({ keyword: query.trim(), page: page - 1, size: pageSize }, signal));
      setError('');
    } catch (requestError) {
      if (requestError.code !== 'ERR_CANCELED') setError(requestError.message);
    } finally { setLoading(false); }
  }

  useEffect(() => {
    const controller = new AbortController();
    const timer = setTimeout(() => {
      adminApi.getUsers({ keyword: query.trim(), page: page - 1, size: pageSize }, controller.signal)
        .then((response) => { setResult(response); setError(''); })
        .catch((requestError) => { if (requestError.code !== 'ERR_CANCELED') setError(requestError.message); })
        .finally(() => setLoading(false));
    }, 250);
    return () => { clearTimeout(timer); controller.abort(); };
  }, [query, page, pageSize]);

  useEffect(() => () => detailRequestRef.current?.abort(), []);

  async function loadUserDetail(userId) {
    detailRequestRef.current?.abort();
    const controller = new AbortController();
    detailRequestRef.current = controller;
    setDetailLoading(true);
    setDetailError('');
    try {
      const detail = await adminApi.getUser(userId, controller.signal);
      if (!controller.signal.aborted) setUserDetail(detail);
    } catch (requestError) {
      if (requestError.code !== 'ERR_CANCELED') setDetailError(requestError.message);
    } finally {
      if (!controller.signal.aborted) setDetailLoading(false);
    }
  }

  function openUserDetail(user) {
    setSelectedUserId(user.userId);
    setUserDetail(null);
    loadUserDetail(user.userId);
  }

  function closeUserDetail() {
    detailRequestRef.current?.abort();
    setSelectedUserId(null);
    setUserDetail(null);
    setDetailError('');
    setDetailLoading(false);
  }

  function openEditProfile() {
    if (!userDetail) return;
    const target = userDetail;
    closeUserDetail();
    setEditTarget(target);
    setEditError('');
  }

  function closeEditProfile() {
    if (editSubmitting) return;
    const target = editTarget;
    setEditTarget(null);
    setEditError('');
    if (target) {
      setSelectedUserId(target.userId);
      setUserDetail(target);
    }
  }

  async function saveEditedProfile(payload, avatarChange) {
    if (!editTarget) return;
    setEditSubmitting(true);
    setEditError('');
    try {
      const updatedDetail = await adminApi.updateUserProfile(editTarget.userId, payload, avatarChange);
      setEditTarget(null);
      setSelectedUserId(updatedDetail.userId);
      setUserDetail(updatedDetail);
      setDetailError('');
      await load();
      showToast('Cập nhật thông tin người dùng thành công!');
    } catch (requestError) {
      setEditError(requestError.message);
      showToast(requestError.message || 'Không thể cập nhật thông tin người dùng.', { type: 'error' });
    } finally {
      setEditSubmitting(false);
    }
  }

  async function changeDetailStatus() {
    if (!userDetail) return;
    if (userDetail.status === 'ACTIVE') {
      const target = userDetail;
      closeUserDetail();
      setBlockTarget(target);
      return;
    }
    closeUserDetail();
    await unblockUser(userDetail);
  }

  async function confirmBlock(reasonCode) {
    if (!blockTarget) return;
    try {
      await adminApi.blockUser(blockTarget.userId, reasonCode);
      setBlockTarget(null);
      await load();
      setStatisticsRevision((current) => current + 1);
      showToast('Khóa tài khoản thành công!');
    } catch (requestError) {
      setError(requestError.message);
      showToast(requestError.message || 'Không thể khóa tài khoản.', { type: 'error' });
      throw requestError;
    }
  }

  async function unblockUser(user) {
    setActionUserId(user.userId);
    try {
      await adminApi.unblockUser(user.userId);
      await load();
      setStatisticsRevision((current) => current + 1);
      showToast('Mở khóa tài khoản thành công!');
    } catch (requestError) {
      setError(requestError.message);
      showToast(requestError.message || 'Không thể mở khóa tài khoản.', { type: 'error' });
    } finally {
      setActionUserId(null);
    }
  }

  return (
    <>
      <section className="grid h-[calc(100vh-4rem)] min-h-0 min-w-0 items-start gap-6 overflow-hidden lg:h-[calc(100vh-6rem)] 2xl:grid-cols-[minmax(0,1fr)_16rem]">
        <div className="flex h-full min-h-0 min-w-0 flex-col">
          <div className="mb-4 flex shrink-0 items-center gap-3 rounded-xl border bg-white p-3">
            <Search size={16} />
            <input value={query} onChange={(event) => { setQuery(event.target.value); setPage(1); }} placeholder="Tìm theo tên hiển thị hoặc email..." className="flex-1 bg-transparent outline-none" />
            <span className="text-sm text-gray-500">Tổng: {result.totalElements}</span>
          </div>
          {error && <p className="mb-4 rounded-xl bg-red-50 p-3 text-red-700">{error}</p>}
          <div className="min-h-0 flex-1 [&>div]:h-full [&>div]:max-h-none">
            {loading ? <LoadingState /> : (
              <DataTable rows={result.content} onRowClick={openUserDetail} pagination={{ currentPage: page, totalPages: result.totalPages, onPageChange: setPage,
                totalItems: result.totalElements, pageSize, onPageSizeChange: (size) => { setPageSize(size); setPage(1); } }} columns={[
                { key: 'displayName', label: 'Người dùng', sortType: 'text', sortValue: (row) => row.displayName || row.email, render: (row) => <div><strong>{row.displayName || 'Chưa cập nhật tên'}</strong><small className="block text-gray-500">{row.email}</small></div> },
                { key: 'profileCompleted', label: 'Hồ sơ', sortType: 'boolean', render: (row) => row.profileCompleted ? 'Đã hoàn tất' : 'Chưa hoàn tất' },
                { key: 'status', label: 'Trạng thái', sortType: 'text', sortValue: (row) => getAdminStatusLabel(row.status), render: (row) => <AdminStatusBadge status={row.status} /> },
              ]} />
            )}
          </div>
        </div>
        <div className="hidden h-full min-h-0 overflow-hidden 2xl:block">
          <AdminUserAnalytics refreshKey={statisticsRevision} />
        </div>
      </section>
      {blockTarget ? (
        <BlockUserDialog
          key={blockTarget.userId}
          user={blockTarget}
          onClose={() => setBlockTarget(null)}
          onConfirm={confirmBlock}
        />
      ) : null}
      {selectedUserId ? (
        <AdminUserDetailDialog
          detail={userDetail}
          loading={detailLoading}
          error={detailError}
          actionPending={String(actionUserId) === String(selectedUserId)}
          onClose={closeUserDetail}
          onRetry={() => loadUserDetail(selectedUserId)}
          onEdit={openEditProfile}
          onStatusAction={changeDetailStatus}
          canEdit={auth.hasAdminRole('SUPER_ADMIN')
            || auth.hasPermission(ADMIN_PERMISSIONS.USER_PROFILE_UPDATE)}
          canStatusAction={userDetail?.status === 'BLOCKED'
            ? auth.hasPermission(ADMIN_PERMISSIONS.USER_UNBLOCK)
            : auth.hasPermission(ADMIN_PERMISSIONS.USER_BLOCK)}
        />
      ) : null}
      {editTarget ? (
        <AdminEditUserProfileDialog
          key={editTarget.userId}
          user={editTarget}
          submitting={editSubmitting}
          error={editError}
          onClose={closeEditProfile}
          onSubmit={saveEditedProfile}
        />
      ) : null}
    </>
  );
}
