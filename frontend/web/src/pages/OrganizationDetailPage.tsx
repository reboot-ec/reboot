import { Link, useParams } from 'react-router-dom';
import type { Organization, OrganizationMembership } from '@reboot/shared-types';
import { Badge } from '../components/atoms/Badge';
import { Spinner } from '../components/atoms/Spinner';
import { ErrorBanner } from '../components/molecules/ErrorBanner';
import { MemberTable } from '../components/organisms/MemberTable';
import { useApiQuery } from './useApiQuery';

function OrganizationMembers({ path }: { path: string }) {
  const { data, error, pending } = useApiQuery<OrganizationMembership[]>(`${path}/members`);

  return (
    <section className="card">
      <h2>Miembros</h2>
      {pending && <Spinner />}
      <ErrorBanner error={error} />
      {data && <MemberTable members={data} />}
    </section>
  );
}

export function OrganizationDetailPage() {
  const { organizationId = '' } = useParams();
  const path = `/api/v1/organizations/${encodeURIComponent(organizationId)}`;
  const { data, error, pending } = useApiQuery<Organization>(path);

  return (
    <>
      <Link to="/organizations">Volver a organizaciones</Link>
      {pending && <Spinner />}
      <ErrorBanner error={error} />
      {data && (
        <>
          <div className="card">
            <h1>{data.name}</h1>
            <Badge>{data.status}</Badge>
            {data.description && <p>{data.description}</p>}
          </div>
          <OrganizationMembers key={path} path={path} />
        </>
      )}
    </>
  );
}
