import { Link, Navigate } from 'react-router-dom';
import { Spinner } from '../components/atoms/Spinner';
import { ErrorBanner } from '../components/molecules/ErrorBanner';
import { CreateOrganizationForm } from '../components/organisms/CreateOrganizationForm';
import { useAuth } from '../context/AuthContext';

export function CreateOrganizationPage() {
  const { profileType, organizationId, organizationPending, organizationError } = useAuth();
  if (profileType !== 'ORG_REPRESENTATIVE') return <Navigate replace to="/dashboard" />;
  if (organizationPending) return <Spinner />;
  if (organizationError) return <ErrorBanner error={organizationError} />;
  if (organizationId) return <Navigate replace to={`/organizations/${organizationId}`} />;

  return (
    <section className="card">
      <header className="directory-header">
        <h1>Register Organization</h1>
        <Link className="secondary-action" to="/organizations">Cancel</Link>
      </header>
      <CreateOrganizationForm />
    </section>
  );
}
