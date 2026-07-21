import { useContext } from 'react';
import { RegistrationContext } from '../../../contexts/RegistrationContext.jsx';

export function useRegistration() {
  const context = useContext(RegistrationContext);
  if (!context) throw new Error('useRegistration phải được sử dụng bên trong RegistrationProvider.');
  return context;
}
