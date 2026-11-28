import { Link } from 'react-router-dom';
import type { Organization } from '@reboot/shared-types';
import { Badge } from '../atoms/Badge';
import { organizationTypeLabels, statusLabels } from '../molecules/organizationOptions';
import { CardHeader } from '../molecules/CardHeader';

export function OrganizationCard({ organization }: { organization: Organization }) {
  return (
    <article className="card">
      <CardHeader title={<Link to={`/organizations/${organization.id}`}>{organization.name}</Link>}>
        <div className="badges">
          <Badge>{organizationTypeLabels[organization.organizationType]}</Badge>
          <Badge tone={organization.status === 'ACTIVE' ? 'positive' : 'muted'}>{statusLabels[organization.status]}</Badge>
        </div>
      </CardHeader>
      {organization.description && <p>{organization.description}</p>}
    </article>
  );
}
