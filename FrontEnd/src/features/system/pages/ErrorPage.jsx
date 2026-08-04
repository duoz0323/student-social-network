import { useNavigate } from 'react-router-dom';
import { ArrowLeft, SearchX, ShieldAlert, ServerCrash, AlertTriangle } from 'lucide-react';
import useThemeLogo from '../../../hooks/useThemeLogo.js';
import Button from '../../../components/common/Button.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';

export default function ErrorPage({ code, title, description }) {
  const logo = useThemeLogo();
  const navigate = useNavigate();
  const { currentUser } = useApp();

  // Determine icon based on code
  let ErrorIcon = AlertTriangle;
  if (code === '404') ErrorIcon = SearchX;
  if (code === '403') ErrorIcon = ShieldAlert;
  if (code === '500') ErrorIcon = ServerCrash;

  const handleHomeClick = () => {
    if (!currentUser) navigate('/login');
    else if (currentUser.role === 'ADMIN') navigate('/admin');
    else navigate('/feed/for-you');
  };

  return (
    <main className="flex min-h-screen flex-col items-center justify-center bg-[var(--app-bg)] px-4 py-12 relative overflow-hidden">
      {/* Background decoration */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] bg-blue-50/50 rounded-full blur-3xl -z-10 opacity-50 dark:opacity-5 pointer-events-none"></div>

      <div className="w-full max-w-md flex flex-col items-center z-10">
        {/* Top Logo */}
        <div className="flex items-center gap-2 mb-12 sm:mb-16 animate-slide-up">
          <img src={logo} alt="UniShare" className="h-8 w-8 rounded-xl object-cover shadow-sm" />
          <span className="text-xl font-extrabold tracking-tight text-[var(--app-text)]">UniShare</span>
        </div>

        {/* Error Code */}
        <div className="animate-slide-up-delayed-1 select-none flex justify-center items-center">
          <p className="text-[120px] sm:text-[180px] font-medium text-gray-200 dark:text-gray-800 leading-none tracking-tighter">
            {code}
          </p>
        </div>

        {/* Text Content */}
        <div className="animate-slide-up-delayed-2 mt-8 mb-8 text-center">
          <h1 className="text-2xl sm:text-3xl font-bold text-[var(--app-text)] tracking-tight">{title}</h1>
          <p className="mt-3 text-sm sm:text-base text-[var(--app-muted)] max-w-[320px] mx-auto leading-relaxed">
            {description}
          </p>
        </div>

        {/* Illustration Icon */}
        <div className="mb-10 text-gray-400 dark:text-gray-500 animate-slide-up-delayed-3 flex justify-center">
          <ErrorIcon size={72} strokeWidth={1.5} />
        </div>

        {/* Action Buttons */}
        <div className="flex flex-col gap-3 w-full max-w-[280px] animate-slide-up-delayed-4">
          <Button onClick={handleHomeClick} className="w-full justify-center py-2.5 shadow-sm hover:-translate-y-0.5 transition-transform">
            Về trang chủ
          </Button>
          <Button variant="ghost" onClick={() => navigate(-1)} className="w-full justify-center py-2.5 text-[var(--app-muted)] hover:bg-[var(--app-surface-soft)]">
            <ArrowLeft size={16} className="mr-2" /> Quay lại trang trước
          </Button>
        </div>
      </div>
    </main>
  );
}
