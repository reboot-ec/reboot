import type { OrganizationQuery } from '@reboot/api-client';
import { Button } from '../atoms/Button';
import { FormField } from './FormField';
import { SelectField } from './SelectField';
import { organizationTypes } from './organizationOptions';

export function SearchBar({ onSearch }: { onSearch: (filters: Pick<OrganizationQuery, 'name' | 'type'>) => void }) {
  return (
    <form className="search" role="search" onSubmit={event => {
      event.preventDefault();
      const fields = new FormData(event.currentTarget);
      const type = organizationTypes.find(value => value === fields.get('type'));
      onSearch({ name: String(fields.get('name')).trim(), type });
    }}>
      <FormField label="Nombre de la organización" type="search" name="name" maxLength={255} />
      <SelectField label="Tipo de organización" name="type">
        <option value="">Todos los tipos</option>
        {organizationTypes.map(type => <option key={type}>{type}</option>)}
      </SelectField>
      <Button type="submit">Buscar</Button>
    </form>
  );
}
