import { Link } from 'react-router-dom';
import type { Organization } from '@reboot/shared-types';
import { Badge } from '../atoms/Badge';
import { organizationTypeLabels } from '../molecules/organizationOptions';

export function OrganizationDashboardHeader({ organization }: { organization: Organization }) {
  return (
    <header className="card directory-header">
      <div className="organization-hub-heading">
        <h1>{organization.name}</h1>
        <div className="badges">
          <Badge>{organizationTypeLabels[organization.organizationType]}</Badge>
          <Badge tone="positive">{organization.status}</Badge>
        </div>
      </div>
      <Link className="secondary-action" to={`/organizations/${organization.id}`}>Manage Profile &amp; Team</Link>
    </header>
  );
}
