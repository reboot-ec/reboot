import { useCallback } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useOrganizations } from '../context/useOrganizations';
import { Spinner } from '../components/atoms/Spinner';
import { ErrorBanner } from '../components/molecules/ErrorBanner';
import { Pagination } from '../components/molecules/Pagination';
import { SearchBar } from '../components/molecules/SearchBar';
import { organizationTypes } from '../components/molecules/organizationOptions';
import { OrganizationCard } from '../components/organisms/OrganizationCard';
import { useApiQuery } from '../hooks/useApiQuery';

export function OrganizationsPage() {
  const [params, setParams] = useSearchParams();
  const name = params.get('name') ?? '';
  const types = organizationTypes.filter(type => params.getAll('type').includes(type));
  const service = useOrganizations();
  const load = useCallback((signal: AbortSignal) => {
    const page = Number(params.get('page'));
    return service.list({
      page: Number.isSafeInteger(page) && page >= 0 && page <= 2147483647 ? page : 0,
      name: params.get('name') ?? '',
      types: organizationTypes.filter(type => params.getAll('type').includes(type)),
    }, signal);
  }, [service, params]);
  const { data, error, pending } = useApiQuery(load);

  return (
    <>
      <header className="directory-header">
        <h1>Explore Centers &amp; Partners</h1>
        <Link className="secondary-action" to="/organizations/new">Register an Organization</Link>
      </header>
      <SearchBar key={JSON.stringify([name, types])} name={name} types={types}
        onSearch={filters => {
          const next = new URLSearchParams();
          if (filters.name) next.set('name', filters.name);
          for (const type of filters.types ?? []) next.append('type', type);
          setParams(next);
        }} />
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
            onPage={page => {
              const next = new URLSearchParams(params);
              next.set('page', String(page));
              setParams(next);
            }} />
        </>
      )}
    </>
  );
}
