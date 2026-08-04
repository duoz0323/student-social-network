import { RouterProvider } from 'react-router-dom';
import { AppProvider } from './contexts/AppContext.jsx';
import { AuthProvider } from './contexts/AuthContext.jsx';
import { NotificationProvider } from './contexts/NotificationContext.jsx';
import { RealtimeProvider } from './contexts/RealtimeContext.jsx';
import { MessagingProvider } from './contexts/MessagingContext.jsx';
import { RegistrationProvider } from './contexts/RegistrationContext.jsx';
import { router } from './router/index.jsx';

function App() {
  return (
    <AuthProvider>
      <RealtimeProvider>
        <NotificationProvider>
          <MessagingProvider>
            <RegistrationProvider>
              <AppProvider>
                <RouterProvider router={router} />
              </AppProvider>
            </RegistrationProvider>
          </MessagingProvider>
        </NotificationProvider>
      </RealtimeProvider>
    </AuthProvider>
  );
}

export default App;
