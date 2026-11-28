import { Link } from 'react-router-dom';
import type { Organization } from '@reboot/shared-types';
import { Badge } from '../atoms/Badge';
import { CardHeader } from '../molecules/CardHeader';

export function OrganizationCard({ organization }: { organization: Organization }) {
  return (
    <article className="card">
      <CardHeader title={<Link to={`/organizations/${organization.id}`}>{organization.name}</Link>}>
        <Badge>{organization.status}</Badge>
      </CardHeader>
      {organization.description && <p>{organization.description}</p>}
    </article>
  );
}
