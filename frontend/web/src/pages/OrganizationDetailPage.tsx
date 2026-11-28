import { useCallback } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useOrganizations } from '../context/useOrganizations';
import { Badge } from '../components/atoms/Badge';
import { Spinner } from '../components/atoms/Spinner';
import { organizationTypeLabels, statusLabels } from '../components/molecules/organizationOptions';
import { ErrorBanner } from '../components/molecules/ErrorBanner';
import { EditOrganizationForm } from '../components/organisms/EditOrganizationForm';
import { MemberTable } from '../components/organisms/MemberTable';
import { useApiQuery } from './useApiQuery';

function OrganizationDetail({ id }: { id: string }) {
  const { user } = useAuth();
  const service = useOrganizations();
  const loadOrganization = useCallback((signal: AbortSignal) => service.get(id, signal), [service, id]);
  const loadMembers = useCallback((signal: AbortSignal) => service.listMembers(id, signal), [service, id]);
  const organization = useApiQuery(loadOrganization);
  const members = useApiQuery(loadMembers);
  const myRole = members.data?.find(member => member.userId === user?.id && member.status === 'ACTIVE')?.role;
  const managerRole = myRole === 'OWNER' || myRole === 'ADMIN' ? myRole : undefined;
  const systemAdmin = user?.roles.some(role => role === 'ADMIN' || role === 'ROLE_ADMIN');
  const data = organization.data;

  return (
    <>
      <Link to="/organizations">Back to organizations</Link>
      {organization.pending && <Spinner />}
      <ErrorBanner error={organization.error} />
      {data && (
        <>
          <div className="card">
            <h1>{data.name}</h1>
            <div className="badges">
              <Badge>{organizationTypeLabels[data.organizationType]}</Badge>
              <Badge tone={data.status === 'ACTIVE' ? 'positive' : 'muted'}>{statusLabels[data.status]}</Badge>
            </div>
            {data.description && <p>{data.description}</p>}
            <dl className="organization-info">
              {data.taxId && <><dt>Tax ID</dt><dd>{data.taxId}</dd></>}
              {data.contactEmail && <><dt>Contact email</dt><dd>{data.contactEmail}</dd></>}
              {data.contactPhone && <><dt>Contact phone</dt><dd>{data.contactPhone}</dd></>}
              {data.address && <><dt>Address</dt><dd>{data.address}</dd></>}
            </dl>
          </div>
          {(managerRole || systemAdmin) && (
            <EditOrganizationForm organization={data} onSaved={saved => organization.updateData(() => saved)} />
          )}
          <section className="card">
            <h2>Members</h2>
            {members.pending && <Spinner />}
            <ErrorBanner error={members.error} />
            {members.data && (
              <MemberTable organizationId={id} members={members.data} managerRole={managerRole}
                onSaved={saved => {
                  if (saved.userId === user?.id && saved.status !== 'ACTIVE' && !systemAdmin) {
                    members.reload();
                    return;
                  }
                  members.updateData(current => current.some(member => member.id === saved.id)
                    ? current.map(member => member.id === saved.id ? saved : member)
                    : [...current, saved]);
                }} />
            )}
          </section>
        </>
      )}
    </>
  );
}

export function OrganizationDetailPage() {
  const { id } = useParams();
  if (!id) return <Link to="/organizations">Back to organizations</Link>;
  return <OrganizationDetail key={id} id={id} />;
}
