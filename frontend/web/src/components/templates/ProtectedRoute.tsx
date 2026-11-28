import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Spinner } from '../atoms/Spinner';

export function ProtectedRoute() {
  const { user, loading } = useAuth();
  const { pathname, search } = useLocation();

  if (loading) return <main className="container dashboard"><Spinner /></main>;
  if (!user) return <Navigate replace to="/login" state={{ from: pathname + search }} />;

  return <Outlet />;
}
