import Badge from '../../../components/common/Badge.jsx';
import { getAdminStatusMeta } from '../constants/adminStatus.js';

/** Badge dùng chung cho trạng thái tài khoản và bài viết trong khu vực quản trị. */
export default function AdminStatusBadge({ status, className = '' }) {
  const meta = getAdminStatusMeta(status);

  return (
    <Badge tone={meta.tone} className={`gap-1.5 ${className}`} title={status || undefined}>
      <span className={`h-2 w-2 shrink-0 rounded-full ${meta.dotClassName}`} aria-hidden="true" />
      {meta.label}
    </Badge>
  );
}
