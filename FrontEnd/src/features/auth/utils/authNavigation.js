export function getAuthenticatedHome({ role, profileCompleted }) {
  if (!profileCompleted) return '/onboarding/profile';
  return role === 'ADMIN' ? '/admin' : '/feed/for-you';
}

const RETURN_ROUTE_PREFIXES = ['/feed/', '/posts/', '/profile/', '/admin/'];
const RETURN_ROUTE_EXACT = new Set(['/saved', '/search', '/admin']);

export function getSafeReturnPath(requestedPath, session) {
  if (typeof requestedPath !== 'string' || !requestedPath.startsWith('/') || requestedPath.startsWith('//') || requestedPath.includes('\\')) return null;
  if (!session.profileCompleted || requestedPath.startsWith('/onboarding')) return null;
  if (!RETURN_ROUTE_EXACT.has(requestedPath) && !RETURN_ROUTE_PREFIXES.some((prefix) => requestedPath.startsWith(prefix))) return null;
  if (requestedPath.startsWith('/admin') && session.user.role !== 'ADMIN') return null;
  return requestedPath;
}
