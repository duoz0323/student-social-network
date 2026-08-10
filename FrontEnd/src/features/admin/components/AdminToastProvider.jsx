import { useCallback, useMemo, useRef, useState } from 'react';
import Toast from '../../../components/common/Toast.jsx';
import { AdminToastContext } from '../contexts/AdminToastContext.js';
import { createAdminToast } from '../utils/adminToast.js';

const MAX_VISIBLE_TOASTS = 3;

export default function AdminToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const nextToastId = useRef(0);

  const dismissToast = useCallback((toastId) => {
    setToasts((current) => current.filter((toast) => toast.id !== toastId));
  }, []);

  const showToast = useCallback((message, options) => {
    const toast = {
      id: ++nextToastId.current,
      ...createAdminToast(message, options),
    };

    // Chỉ giữ các phản hồi mới nhất để nhiều thao tác liên tiếp không che nội dung quản trị.
    setToasts((current) => [...current, toast].slice(-MAX_VISIBLE_TOASTS));
    return toast.id;
  }, []);

  const contextValue = useMemo(() => ({ showToast, dismissToast }), [dismissToast, showToast]);

  return (
    <AdminToastContext.Provider value={contextValue}>
      {children}
      <div className="pointer-events-none fixed bottom-[calc(1rem+env(safe-area-inset-bottom))] left-1/2 z-[100] flex -translate-x-1/2 flex-col items-center gap-2 sm:bottom-6">
        {toasts.map((toast) => (
          <div key={toast.id} className="pointer-events-auto">
            <Toast
              message={toast.message}
              type={toast.type}
              duration={toast.duration}
              positioned={false}
              onClose={() => dismissToast(toast.id)}
            />
          </div>
        ))}
      </div>
    </AdminToastContext.Provider>
  );
}
