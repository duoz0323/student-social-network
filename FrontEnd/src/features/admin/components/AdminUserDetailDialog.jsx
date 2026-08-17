import { Mail, UserRound, X } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { LoadingState } from '../../../components/common/StateBlock.jsx';
import { formatDateTime } from '../../../utils/formatters.js';
import { getAdminUserBlockReasonLabel } from '../constants/adminUserBlockReasons.js';
import AdminStatusBadge from './AdminStatusBadge.jsx';

function formatOptionalDateTime(value) {
  return value ? formatDateTime(value) : '—';
}

function DetailField({ label, value, wide = false }) {
  return (
    <div className={`rounded-lg bg-slate-50 px-3 py-3 ${wide ? 'sm:col-span-2' : ''}`}>
      <dt className="text-[10px] font-bold uppercase tracking-[0.08em] text-slate-400">{label}</dt>
      <dd className="mt-1 break-words text-sm font-semibold text-slate-800">{value || '—'}</dd>
    </div>
  );
}

function ProfileImage({ src, name }) {
  const initial = name?.trim()?.charAt(0)?.toUpperCase() || '?';
  return (
    <div className="flex h-20 w-20 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-slate-200 bg-slate-50 text-xl font-bold text-slate-500">
      {src ? <img src={src} alt={`Ảnh đại diện của ${name}`} className="h-full w-full object-cover" /> : initial}
    </div>
  );
}

export default function AdminUserDetailDialog({
  detail,
  loading,
  error,
  actionPending,
  onClose,
  onRetry,
  onEdit,
  onStatusAction,
  canEdit = false,
  canStatusAction = false,
}) {
  const displayName = detail?.displayName?.trim() || 'Chưa cập nhật tên';
  const isBlocked = detail?.status === 'BLOCKED';

  return (
    <Modal
      open
      size="md"
      className="admin-theme !max-w-[560px] !rounded-lg !bg-[var(--admin-surface)]"
      bodyClassName="bg-[var(--admin-surface)] px-6 pb-6 pt-5 text-slate-800"
      customHeader={(
        <header className="flex shrink-0 items-center justify-between rounded-t-lg border-b border-slate-100 bg-white px-6 py-5">
          <h2 className="text-base font-bold text-slate-800">Chi tiết người dùng</h2>
          <button
            type="button"
            className="flex h-8 w-8 items-center justify-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-700"
            onClick={onClose}
            aria-label="Đóng chi tiết người dùng"
          >
            <X className="h-5 w-5" aria-hidden="true" />
          </button>
        </header>
      )}
      onClose={onClose}
    >
      {loading ? (
        <div className="[&>div]:!border-slate-100 [&>div]:!bg-white">
          <LoadingState message="Đang tải thông tin người dùng..." />
        </div>
      ) : error ? (
        <div className="py-8 text-center" role="alert">
          <p className="text-sm text-red-700">{error}</p>
          <Button className="mt-4" variant="secondary" onClick={onRetry}>Thử lại</Button>
        </div>
      ) : detail ? (
        <div>
          <div className="flex items-center gap-5 border-b border-slate-100 pb-6">
            <ProfileImage src={detail.avatarUrl} name={displayName} />
            <div className="min-w-0 flex-1">
              <div className="flex flex-wrap items-center gap-2">
                <h3 className="truncate text-xl font-bold text-slate-800">{displayName}</h3>
                <AdminStatusBadge status={detail.status} />
              </div>
              <p className="mt-2 flex items-center gap-2 truncate text-xs text-slate-500">
                <Mail className="h-3.5 w-3.5 shrink-0" aria-hidden="true" />
                {detail.email || 'Không có email'}
              </p>
            </div>
          </div>

          <dl className="mt-5 grid gap-3 sm:grid-cols-2">
            <DetailField label="Mã người dùng" value={detail.userId} />
            <DetailField label="Trạng thái hồ sơ" value={detail.profileCompleted ? 'Đã hoàn tất' : 'Chưa hoàn tất'} />
            <DetailField label="Hoàn tất hồ sơ lúc" value={formatOptionalDateTime(detail.profileCompletedAt)} />
            <DetailField label="Ngày sinh" value={detail.dateOfBirth || '—'} />
            <DetailField label="Ngày tạo tài khoản" value={formatOptionalDateTime(detail.createdAt)} wide />
            <DetailField label="Cập nhật gần nhất" value={formatOptionalDateTime(detail.updatedAt)} wide />
            {isBlocked ? <DetailField label="Bị khóa lúc" value={formatOptionalDateTime(detail.blockedAt)} /> : null}
            {isBlocked ? <DetailField label="Lý do khóa" value={getAdminUserBlockReasonLabel(detail.blockedReason)} /> : null}
          </dl>

          <div className="mt-5 rounded-lg border border-slate-100 bg-white p-4 shadow-sm">
            <h4 className="flex items-center gap-2 text-[10px] font-bold uppercase tracking-[0.08em] text-slate-500">
              <UserRound className="h-3.5 w-3.5" aria-hidden="true" />
              Giới thiệu
            </h4>
            <p className="mt-2 whitespace-pre-wrap text-xs leading-5 text-slate-600">
              {detail.bio?.trim() || 'Người dùng chưa cập nhật phần giới thiệu.'}
            </p>
          </div>

          {canEdit || canStatusAction ? (
            <div className="mt-5 grid grid-cols-2 gap-3">
              {canEdit && <Button
                className="!h-10 w-full !rounded-md !px-3"
                onClick={onEdit}
              >
                Sửa thông tin
              </Button>}
              {canStatusAction && <Button
                className="!h-10 w-full !rounded-md !border-slate-200 !bg-slate-100 !px-3 !text-slate-700 hover:!bg-slate-200"
                variant="secondary"
                disabled={actionPending}
                onClick={onStatusAction}
              >
                {actionPending ? 'Đang xử lý...' : isBlocked ? 'Mở khóa tài khoản' : 'Khóa tài khoản'}
              </Button>}
            </div>
          ) : null}
        </div>
      ) : null}
    </Modal>
  );
}
