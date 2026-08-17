import { BadgeCheck } from 'lucide-react';
import { hasCollaboratorBadge } from './publicIdentityBadge.js';

/** Render badge từ contract Backend; Client không suy luận từ route, username hay account type. */
export default function PublicIdentityBadge({ badges = [], className = '' }) {
  if (!hasCollaboratorBadge(badges)) return null;
  return (
    <span
      className={`inline-flex shrink-0 items-center text-indigo-600 ${className}`}
      title="Cộng tác viên chính thức"
      aria-label="Cộng tác viên chính thức"
    >
      <BadgeCheck size={16} aria-hidden="true" />
    </span>
  );
}
