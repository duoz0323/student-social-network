import { useAuth } from '../hooks/useAuth.js';

export default function AuthBootstrap({ children }) {
  const { isInitializing } = useAuth();
  if (isInitializing) {
    return <main className="flex min-h-screen items-center justify-center text-sm font-semibold text-zinc-600">Đang khôi phục phiên đăng nhập...</main>;
  }
  return children;
}
