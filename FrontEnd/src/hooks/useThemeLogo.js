import { useSyncExternalStore } from 'react';
import logoLight from '../assets/brand/logo-light.jpg';
import logoDark from '../assets/brand/logo-dark.jpg';

/* Media query singleton để tránh tạo lại mỗi lần render */
const darkMQ = typeof window !== 'undefined'
  ? window.matchMedia('(prefers-color-scheme: dark)')
  : null;

function subscribe(callback) {
  darkMQ?.addEventListener('change', callback);
  return () => darkMQ?.removeEventListener('change', callback);
}

function getSnapshot() {
  return darkMQ?.matches ?? false;
}

function getServerSnapshot() {
  return false;
}

/**
 * Theo dõi prefers-color-scheme và trả về logo phù hợp:
 * - Giao diện sáng (Light UI): dùng logo NỀN ĐEN (logoDark).
 * - Giao diện tối (Dark UI): dùng logo NỀN TRẮNG (logoLight).
 */
export default function useThemeLogo() {
  const isDark = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
  return isDark ? logoLight : logoDark;
}
