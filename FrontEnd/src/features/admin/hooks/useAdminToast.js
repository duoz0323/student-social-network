import { useContext } from 'react';
import { AdminToastContext } from '../contexts/AdminToastContext.js';

export function useAdminToast() {
  const context = useContext(AdminToastContext);

  if (!context) {
    throw new Error('useAdminToast phải được sử dụng bên trong AdminToastProvider.');
  }

  return context;
}
