import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { Login } from './pages/Login';
import { DashboardLayout } from './layouts/DashboardLayout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Dashboard } from './pages/Dashboard';
import { Records } from './pages/Records';
import { AdminUsers } from './pages/AdminUsers';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

export const App = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <Toaster position="top-right" />
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<Navigate to="/login" replace />} />

          {/* ADMIN Routes */}
          <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route path="/admin" element={<DashboardLayout />}>
              <Route path="dashboard" element={<Dashboard />} />
              <Route path="records" element={<Records />} />
              <Route path="users" element={<AdminUsers />} />
            </Route>
          </Route>

          {/* ANALYST Routes */}
          <Route element={<ProtectedRoute allowedRoles={['ANALYST']} />}>
            <Route path="/analyst" element={<DashboardLayout />}>
              <Route path="dashboard" element={<Dashboard />} />
              <Route path="records" element={<Records />} />
            </Route>
          </Route>

          {/* VIEWER Routes */}
          <Route element={<ProtectedRoute allowedRoles={['VIEWER']} />}>
            <Route path="/viewer" element={<DashboardLayout />}>
              <Route path="dashboard" element={<Dashboard />} />
              <Route path="records" element={<Records />} />
            </Route>
          </Route>

          {/* Fallback route */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

export default App;
