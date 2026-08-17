export function hasCollaboratorBadge(badges) {
  return Array.isArray(badges) && badges.includes('COLLABORATOR');
}
