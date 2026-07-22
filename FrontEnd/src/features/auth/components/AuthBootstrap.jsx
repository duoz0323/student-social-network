import logo from '../../../assets/brand/logo.png';
import { useAuth } from '../hooks/useAuth.js';

export default function AuthBootstrap({ children }) {
  const { isInitializing } = useAuth();
  if (isInitializing) {
    return (
      <main
        className="flex min-h-screen items-center justify-center bg-slate-950 px-6"
        aria-busy="true"
        aria-live="polite"
      >
        <div className="relative flex h-28 w-28 items-center justify-center">
          {/* Vòng sáng chuyển động giúp báo trạng thái mà không làm logo bị xoay. */}
          <span className="absolute inset-0 rounded-full border border-white/10" aria-hidden="true" />
          <span
            className="absolute inset-0 animate-spin rounded-full border-2 border-transparent border-t-violet-500 border-r-blue-500"
            aria-hidden="true"
          />
          <span className="absolute inset-3 animate-pulse rounded-full bg-violet-500/10 blur-md" aria-hidden="true" />
          <div className="relative flex h-20 w-20 items-center justify-center rounded-full bg-white shadow-[0_10px_35px_rgba(79,70,229,0.3)]">
            <img className="h-14 w-14 object-contain" src={logo} alt="UniShare" />
          </div>
        </div>
        <span className="sr-only">Đang khôi phục phiên đăng nhập...</span>
      </main>
    );
  }
  return children;
}
