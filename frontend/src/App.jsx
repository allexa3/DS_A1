import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth.jsx';
import Login from './pages/Login.jsx';
import AdminPage from './pages/AdminPage.jsx';
import ClientPage from './pages/ClientPage.jsx';

const homeFor = (role) => (role === 'ADMIN' ? '/admin' : '/client');

/** Role-based access: a page is only rendered for the role it belongs to. */
function RequireRole({ role, children }) {
  const { session } = useAuth();
  if (!session) return <Navigate to="/login" replace />;
  if (session.role !== role) return <Navigate to={homeFor(session.role)} replace />;
  return children;
}

export default function App() {
  const { session } = useAuth();
  return (
    <Routes>
      <Route path="/login" element={session ? <Navigate to={homeFor(session.role)} replace /> : <Login />} />
      <Route path="/admin" element={<RequireRole role="ADMIN"><AdminPage /></RequireRole>} />
      <Route path="/client" element={<RequireRole role="CLIENT"><ClientPage /></RequireRole>} />
      <Route path="*" element={<Navigate to={session ? homeFor(session.role) : '/login'} replace />} />
    </Routes>
  );
}
