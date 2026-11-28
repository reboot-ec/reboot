import type { OrganizationQuery } from '@reboot/api-client';
import { Button } from '../atoms/Button';
import { FormField } from './FormField';
import { SelectField } from './SelectField';
import { organizationTypes, organizationTypeLabels } from './organizationOptions';

export function SearchBar({ onSearch }: { onSearch: (filters: Pick<OrganizationQuery, 'name' | 'type'>) => void }) {
  return (
    <form className="search" role="search" onSubmit={event => {
      event.preventDefault();
      const fields = new FormData(event.currentTarget);
      const type = organizationTypes.find(value => value === fields.get('type'));
      onSearch({ name: String(fields.get('name')).trim(), type });
    }}>
      <FormField label="Organization name" type="search" name="name" maxLength={255} />
      <SelectField label="Organization type" name="type">
        <option value="">All types</option>
        {organizationTypes.map(type => <option key={type} value={type}>{organizationTypeLabels[type]}</option>)}
      </SelectField>
      <Button type="submit">Search</Button>
    </form>
  );
}
