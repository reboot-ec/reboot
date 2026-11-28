import type { Organization } from '@reboot/shared-types';
import { useOrganizations } from '../../context/useOrganizations';
import { OrganizationForm } from './OrganizationForm';

export function EditOrganizationForm({ organization, onSaved }: { organization: Organization; onSaved: (value: Organization) => void }) {
  const service = useOrganizations();

  return (
    <details className="card">
      <summary>Edit organization</summary>
      <OrganizationForm organization={organization} onSave={async input => {
        const changes = Object.fromEntries(
          (Object.keys(input) as (keyof typeof input)[]).flatMap(field => {
            const value = input[field];
            return value === undefined || value === organization[field] ? [] : [[field, value]];
          }),
        );
        if (Object.keys(changes).length) onSaved(await service.update(organization.id, changes));
      }} />
    </details>
  );
}
