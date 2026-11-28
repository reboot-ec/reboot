import type { OrganizationQuery } from '@reboot/api-client';
import { Button } from '../atoms/Button';
import { FormField } from './FormField';
import { SelectField } from './SelectField';
import { organizationTypes, organizationTypeLabels } from './organizationOptions';

type Filters = Pick<OrganizationQuery, 'name' | 'types'>;

export function SearchBar({ name, types = [], onSearch }: Filters & { onSearch: (filters: Filters) => void }) {
  const typeSelection = types.join(',');

  return (
    <form className="search" role="search" onSubmit={event => {
      event.preventDefault();
      const fields = new FormData(event.currentTarget);
      const selected = String(fields.get('type')).split(',');
      onSearch({ name: String(fields.get('name')).trim(), types: organizationTypes.filter(type => selected.includes(type)) });
    }}>
      <FormField label="Organization name" type="search" name="name" maxLength={255} defaultValue={name} />
      <SelectField label="Organization type" name="type" defaultValue={typeSelection}>
        <option value="">All types</option>
        {types.length > 1 && <option value={typeSelection}>{types.map(type => organizationTypeLabels[type]).join(' and ')}</option>}
        {organizationTypes.map(type => <option key={type} value={type}>{organizationTypeLabels[type]}</option>)}
      </SelectField>
      <Button type="submit">Search</Button>
    </form>
  );
}
