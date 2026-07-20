import { RouterProvider } from 'react-router-dom';
import { AppProvider } from './contexts/AppContext.jsx';
import { AuthProvider } from './contexts/AuthContext.jsx';
import { RegistrationProvider } from './contexts/RegistrationContext.jsx';
import { router } from './router/index.jsx';

function App() {
  return (
    <AuthProvider>
      <RegistrationProvider>
        <AppProvider>
          <RouterProvider router={router} />
        </AppProvider>
      </RegistrationProvider>
    </AuthProvider>
  );
}

export default App;
