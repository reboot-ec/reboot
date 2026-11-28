import { useCallback } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Spinner } from '../components/atoms/Spinner';
import { ErrorBanner } from '../components/molecules/ErrorBanner';
import { OrganizationManagement } from '../components/organisms/OrganizationManagement';
import { OrganizationProfile } from '../components/organisms/OrganizationProfile';
import { useAuth } from '../context/AuthContext';
import { useOrganizations } from '../context/useOrganizations';
import { useApiQuery } from '../hooks/useApiQuery';

function OrganizationDetail({ id }: { id: string }) {
  const { user } = useAuth();
  const service = useOrganizations();
  const load = useCallback((signal: AbortSignal) => service.get(id, signal), [service, id]);
  const organization = useApiQuery(load);
  const data = organization.data;

  return (
    <>
      <Link to="/organizations">Back to organizations</Link>
      {organization.pending && <Spinner />}
      <ErrorBanner error={organization.error} />
      {data && <OrganizationProfile organization={data} />}
      {data && user && (
        <OrganizationManagement key={user.id} organization={data} user={user}
          onSaved={saved => organization.updateData(() => saved)} />
      )}
    </>
  );
}

export function OrganizationDetailPage() {
  const { id } = useParams();
  if (!id) return <Link to="/organizations">Back to organizations</Link>;
  return <OrganizationDetail key={id} id={id} />;
}
