import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Spinner } from '../atoms/Spinner';

export function AuthLayout() {
  const { user, loading } = useAuth();
  if (user) return <Navigate replace to="/organizations" />;

  return <main className="auth-layout"><span className="brand">REBOOT</span>{loading ? <Spinner /> : <Outlet />}</main>;
}
