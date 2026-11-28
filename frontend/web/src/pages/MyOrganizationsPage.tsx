import { useCallback, useState } from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import { useOrganizations } from '../context/useOrganizations';
import { Spinner } from '../components/atoms/Spinner';
import { ErrorBanner } from '../components/molecules/ErrorBanner';
import { Pagination } from '../components/molecules/Pagination';
import { OrganizationCard } from '../components/organisms/OrganizationCard';
import { useApiQuery } from './useApiQuery';

export function MyOrganizationsPage() {
  const { state } = useLocation();
  const [page, setPage] = useState(0);
  const service = useOrganizations();
  const load = useCallback((signal: AbortSignal) => service.listMine(page, 20, signal), [service, page]);
  const { data, error, pending } = useApiQuery(load);

  if (state?.checkOnboarding === true && data?.totalElements === 0) {
    return <Navigate replace to="/organizations/new" />;
  }

  return (
    <>
      <header className="directory-header">
        <h1>My Organizations</h1>
        <Link className="secondary-action" to="/organizations/new">Register an Organization</Link>
      </header>
      {pending && <Spinner />}
      <ErrorBanner error={error} />
      {data && (
        <>
          {data.items.length ? (
            <div className="card-grid">
              {data.items.map(organization => <OrganizationCard key={organization.id} organization={organization} />)}
            </div>
          ) : <p>You do not have any active organization memberships.</p>}
          <Pagination page={data.page} totalPages={data.totalPages} onPage={setPage} />
        </>
      )}
    </>
  );
}
