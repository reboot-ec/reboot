import { useState } from 'react';
import type { Organization } from '@reboot/shared-types';
import { Button } from '../components/atoms/Button';
import { Spinner } from '../components/atoms/Spinner';
import { ErrorBanner } from '../components/molecules/ErrorBanner';
import { SearchBar } from '../components/molecules/SearchBar';
import { OrganizationCard } from '../components/organisms/OrganizationCard';
import { useApiQuery } from './useApiQuery';

export function OrganizationsPage() {
  const [{ name, page }, setQuery] = useState({ name: '', page: 0 });
  const params = new URLSearchParams({ page: String(page), pageSize: '20' });
  if (name) params.set('name', name);
  const { data, error, pending } = useApiQuery<{ items: Organization[]; totalPages: number }>(`/api/v1/organizations?${params}`);

  return (
    <>
      <h1>Organizaciones</h1>
      <SearchBar onSearch={value => setQuery({ name: value, page: 0 })} />
      <ErrorBanner error={error} />
      {pending && <Spinner />}
      {data && (
        <>
          {data.items.length ? <div className="card-grid">{data.items.map(organization =>
            <OrganizationCard key={organization.id} organization={organization} />)}</div>
            : <p>No se encontraron organizaciones.</p>}
          {(page > 0 || data.totalPages > 1) && (
            <nav className="pagination" aria-label="Paginación">
              <Button disabled={page === 0} onClick={() => setQuery({ name, page: page - 1 })}>Anterior</Button>
              <span>Página {page + 1}</span>
              <Button disabled={page + 1 >= data.totalPages} onClick={() => setQuery({ name, page: page + 1 })}>Siguiente</Button>
            </nav>
          )}
        </>
      )}
    </>
  );
}
