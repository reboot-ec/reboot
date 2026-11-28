import { useCallback, useState } from 'react';
import { Link } from 'react-router-dom';
import type { OrganizationQuery } from '@reboot/api-client';
import { useOrganizations } from '../context/useOrganizations';
import { Spinner } from '../components/atoms/Spinner';
import { ErrorBanner } from '../components/molecules/ErrorBanner';
import { Pagination } from '../components/molecules/Pagination';
import { SearchBar } from '../components/molecules/SearchBar';
import { OrganizationCard } from '../components/organisms/OrganizationCard';
import { useApiQuery } from './useApiQuery';

export function OrganizationsPage() {
  const [query, setQuery] = useState<OrganizationQuery>({ page: 0, pageSize: 20 });
  const service = useOrganizations();
  const load = useCallback((signal: AbortSignal) => service.list(query, signal), [service, query]);
  const { data, error, pending } = useApiQuery(load);

  return (
    <>
      <header className="directory-header">
        <h1>Explore Centers &amp; Partners</h1>
        <Link className="secondary-action" to="/organizations/new">Register an Organization</Link>
      </header>
      <SearchBar onSearch={filters => setQuery(current => ({ ...current, ...filters, page: 0 }))} />
      <ErrorBanner error={error} />
      {pending && <Spinner />}
      {data && (
        <>
          {data.items.length ? (
            <div className="card-grid">{data.items.map(organization =>
              <OrganizationCard key={organization.id} organization={organization} />
            )}</div>
          ) : <p>No organizations found.</p>}
          <Pagination page={data.page} totalPages={data.totalPages}
            onPage={page => setQuery(current => ({ ...current, page }))} />
        </>
      )}
    </>
  );
}
