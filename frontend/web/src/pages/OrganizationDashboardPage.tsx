import { useCallback } from 'react';
import { Navigate } from 'react-router-dom';
import { Spinner } from '../components/atoms/Spinner';
import { ErrorBanner } from '../components/molecules/ErrorBanner';
import { OperationalActionCard } from '../components/molecules/OperationalActionCard';
import { OrganizationDashboardHeader } from '../components/organisms/OrganizationDashboardHeader';
import { OrganizationOperationalMetrics } from '../components/organisms/OrganizationOperationalMetrics';
import { useAuth } from '../context/AuthContext';
import { useOrganizations } from '../context/useOrganizations';
import { useApiQuery } from '../hooks/useApiQuery';

function OrganizationDashboard({ id }: { id: string }) {
  const service = useOrganizations();
  const load = useCallback((signal: AbortSignal) => service.get(id, signal), [service, id]);
  const { data, error, pending } = useApiQuery(load);

  if (pending) return <Spinner />;
  if (!data) return <ErrorBanner error={error} />;
  if (data.status !== 'ACTIVE') return <Navigate replace to={`/organizations/${data.id}`} />;

  return (
    <>
      <OrganizationDashboardHeader organization={data} />
      <section className="card-grid" aria-label="Organization operations">
        <OperationalActionCard title="Incoming Device Drop-offs / Donations"
          description="Review and receive electronics donated or sent for recycling by citizens" status="0 pending" />
        <OperationalActionCard title="Corporate Bulk Donation"
          description="Publish batches of retired or refurbished company electronics for NGOs and institutions" />
        <OperationalActionCard title="Recovery & Recycling Log"
          description="Track processed devices, recovered components, and reported e-waste weight" />
      </section>
      <OrganizationOperationalMetrics organizationId={data.id} />
    </>
  );
}

export function OrganizationDashboardPage() {
  const { profileType, organizationId, organizationPending, organizationError } = useAuth();

  if (profileType !== 'ORG_REPRESENTATIVE') return <Navigate replace to="/dashboard" />;
  if (organizationPending) return <Spinner />;
  if (organizationError) return <ErrorBanner error={organizationError} />;
  if (!organizationId) return <Navigate replace to="/organizations/new" />;

  return <OrganizationDashboard key={organizationId} id={organizationId} />;
}
