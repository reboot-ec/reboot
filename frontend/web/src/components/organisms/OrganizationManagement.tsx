import { useCallback } from 'react';
import { ApiError } from '@reboot/api-client';
import type { Organization, User } from '@reboot/shared-types';
import { useOrganizations } from '../../context/useOrganizations';
import { useApiQuery } from '../../hooks/useApiQuery';
import { ErrorBanner } from '../molecules/ErrorBanner';
import { EditOrganizationForm } from './EditOrganizationForm';
import { MemberTable } from './MemberTable';

export function OrganizationManagement({ organization, user, onSaved }: {
  organization: Organization;
  user: User;
  onSaved: (value: Organization) => void;
}) {
  const service = useOrganizations();
  const { id } = organization;
  const loadMembers = useCallback((signal: AbortSignal) => service.listMembers(id, signal), [service, id]);
  const members = useApiQuery(loadMembers);
  if (members.pending || (members.error instanceof ApiError && members.error.status === 403)) return null;
  if (!members.data) return <ErrorBanner error={members.error} />;

  const role = members.data.find(member => member.userId === user.id && member.status === 'ACTIVE')?.role;
  const managerRole = role === 'OWNER' || role === 'ADMIN' ? role : undefined;
  const systemAdmin = user.roles.some(value => value === 'ADMIN' || value === 'ROLE_ADMIN');

  return (
    <>
      {(managerRole || systemAdmin) && <EditOrganizationForm organization={organization} onSaved={onSaved} />}
      <section className="card">
        <h2>Members</h2>
        <MemberTable organizationId={id} members={members.data} managerRole={managerRole}
          onSaved={saved => {
            if (saved.userId === user.id && saved.status !== 'ACTIVE' && !systemAdmin) {
              members.reload();
              return;
            }
            members.updateData(current => current.some(member => member.id === saved.id)
              ? current.map(member => member.id === saved.id ? saved : member)
              : [...current, saved]);
          }} />
      </section>
    </>
  );
}
