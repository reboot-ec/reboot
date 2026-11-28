import { Navigate } from 'react-router-dom';
import { Spinner } from '../components/atoms/Spinner';
import { ErrorBanner } from '../components/molecules/ErrorBanner';
import { useAuth } from '../context/AuthContext';

export function MyOrganizationsPage() {
  const { profileType, organizationId, organizationPending, organizationError } = useAuth();

  if (profileType !== 'ORG_REPRESENTATIVE') return <Navigate replace to="/dashboard" />;
  if (organizationPending) return <Spinner />;
  if (organizationError) return <ErrorBanner error={organizationError} />;

  return <Navigate replace to={organizationId ? `/organizations/${organizationId}` : '/organizations/new'} />;
}
