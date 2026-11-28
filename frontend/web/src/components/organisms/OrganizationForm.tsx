import { useState } from 'react';
import type { FormEvent } from 'react';
import type { Organization } from '@reboot/shared-types';
import type { OrganizationInput, OrganizationPatch } from '@reboot/api-client';
import { Button } from '../atoms/Button';
import { ErrorBanner, fieldError } from '../molecules/ErrorBanner';
import { FormField } from '../molecules/FormField';
import { SelectField } from '../molecules/SelectField';
import { organizationStatuses, organizationTypes, organizationTypeLabels, statusLabels } from '../molecules/organizationOptions';

interface Props {
  organization?: Organization;
  onSave: (data: OrganizationInput & Pick<OrganizationPatch, 'status'>) => Promise<void>;
}

export function OrganizationForm({ organization, onSave }: Props) {
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const [saved, setSaved] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const fields = new FormData(event.currentTarget);
    const value = (field: string) => String(fields.get(field) ?? '').trim();
    const organizationType = organizationTypes.find(type => type === fields.get('organizationType'));
    if (!organizationType) return;
    setPending(true);
    setError(null);
    setSaved(false);
    try {
      await onSave({
        name: value('name'),
        organizationType,
        taxId: value('taxId') || (organization?.taxId ? '' : undefined),
        description: value('description') || (organization?.description ? '' : undefined),
        contactEmail: value('contactEmail') || (organization?.contactEmail ? '' : undefined),
        contactPhone: value('contactPhone') || (organization?.contactPhone ? '' : undefined),
        address: value('address') || (organization?.address ? '' : undefined),
        status: organizationStatuses.find(status => status === fields.get('status')),
      });
      setSaved(true);
    } catch (failure) {
      setError(failure);
    } finally {
      setPending(false);
    }
  }

  return (
    <form onSubmit={submit} className="management-form">
      <ErrorBanner error={error} />
      {saved && <p role="status">Changes saved.</p>}
      <fieldset disabled={pending} onChange={() => setSaved(false)}>
        <FormField label="Name" name="name" defaultValue={organization?.name} maxLength={255} pattern={/.*\S.*/.source} required error={fieldError(error, 'name')} />
        <SelectField label="Type" name="organizationType" defaultValue={organization?.organizationType ?? ''} required error={fieldError(error, 'organizationType')}>
          <option value="" disabled>Select a type</option>
          {organizationTypes.map(type => <option key={type} value={type}>{organizationTypeLabels[type]}</option>)}
        </SelectField>
        <FormField label="Tax ID" name="taxId" defaultValue={organization?.taxId ?? ''} maxLength={100} error={fieldError(error, 'taxId')} />
        <FormField label="Description" name="description" defaultValue={organization?.description ?? ''} error={fieldError(error, 'description')} />
        <FormField label="Contact email" name="contactEmail" type="email" defaultValue={organization?.contactEmail ?? ''} maxLength={255} error={fieldError(error, 'contactEmail')} />
        <FormField label="Contact phone" name="contactPhone" type="tel" defaultValue={organization?.contactPhone ?? ''} maxLength={50} error={fieldError(error, 'contactPhone')} />
        <FormField label="Address" name="address" defaultValue={organization?.address ?? ''} error={fieldError(error, 'address')} />
        {organization && (
          <SelectField label="Status" name="status" defaultValue={organization.status} error={fieldError(error, 'status')}>
            {organizationStatuses.map(status => <option key={status} value={status}>{statusLabels[status]}</option>)}
          </SelectField>
        )}
        <Button type="submit">{pending ? 'Saving…' : organization ? 'Save changes' : 'Register organization'}</Button>
      </fieldset>
    </form>
  );
}
