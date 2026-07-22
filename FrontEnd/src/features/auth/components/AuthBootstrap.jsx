import LogoLoader from '../../../components/common/LogoLoader.jsx';
import { useAuth } from '../hooks/useAuth.js';

export default function AuthBootstrap({ children }) {
  const { isInitializing } = useAuth();
  if (isInitializing) {
    return <LogoLoader fullScreen size="lg" message="Đang khôi phục phiên đăng nhập..." />;
  }
  return children;
}
