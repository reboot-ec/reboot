import type { Organization } from '@reboot/shared-types';
import { Badge } from '../atoms/Badge';
import { organizationTypeLabels, statusLabels } from '../molecules/organizationOptions';

export function OrganizationProfile({ organization }: { organization: Organization }) {
  const { name, organizationType, status, description, contactEmail, contactPhone, address } = organization;
  const email = contactEmail?.trim();
  const phone = contactPhone?.trim().replace(/[^+\d]/g, '');
  const contactUrl = email ? `mailto:${encodeURIComponent(email).replace(/%40/g, '@')}`
    : phone ? `tel:${phone}` : undefined;

  return (
    <section className="card">
      <h1>{name}</h1>
      <div className="badges">
        <Badge>{organizationTypeLabels[organizationType]}</Badge>
        <Badge tone={status === 'ACTIVE' ? 'positive' : 'muted'}>{statusLabels[status]}</Badge>
      </div>
      {description && <p>{description}</p>}
      {(email || contactPhone || address) && (
        <dl className="organization-info">
          {email && <><dt>Contact email</dt><dd>{email}</dd></>}
          {contactPhone && <><dt>Contact phone</dt><dd>{contactPhone}</dd></>}
          {address && <><dt>Address</dt><dd>{address}</dd></>}
        </dl>
      )}
      {contactUrl
        ? <div><a className="secondary-action" href={contactUrl}>Contact Organization</a></div>
        : <p>No contact information is listed.</p>}
    </section>
  );
}
