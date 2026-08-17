import Avatar from '../../../components/common/Avatar.jsx';
import { getAdminRoleLabel } from '../constants/adminRbac.js';
import { getSuggestionActorRoles } from './moderationSuggestion.js';

/** Hiển thị nhất quán danh tính và vai trò của người tham gia xử lý đề xuất. */
export default function SuggestionActor({ actor, fallbackRole, emptyText = 'Chưa có người xử lý' }) {
  if (!actor) return <span className="text-sm text-[var(--app-muted)]">{emptyText}</span>;

  const displayName = actor.displayName || actor.username || `Admin #${actor.adminId}`;
  const roles = getSuggestionActorRoles(actor, fallbackRole);

  return (
    <div className="flex min-w-0 items-center gap-3">
      <Avatar src={actor.avatarUrl} name={displayName} size="sm" />
      <div className="min-w-0">
        <p className="truncate text-sm font-semibold text-[var(--app-text)]">{displayName}</p>
        {actor.username ? <p className="truncate text-xs text-[var(--app-muted)]">@{actor.username}</p> : null}
        <div className="mt-1 flex flex-wrap gap-1">
          {(roles.length ? roles : ['ADMIN']).map((role) => (
            <span key={role} className="rounded-full bg-violet-100 px-2 py-0.5 text-[11px] font-semibold text-violet-700">
              {role === 'ADMIN' ? 'Quản trị viên' : getAdminRoleLabel(role)}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
}
