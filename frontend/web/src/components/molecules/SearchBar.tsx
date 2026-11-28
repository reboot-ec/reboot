import { Button } from '../atoms/Button';
import { FormField } from './FormField';

export function SearchBar({ onSearch }: { onSearch: (value: string) => void }) {
  return (
    <form className="search" role="search" onSubmit={event => {
      event.preventDefault();
      onSearch(String(new FormData(event.currentTarget).get('name')).trim());
    }}>
      <FormField label="Nombre de la organización" type="search" name="name" />
      <Button type="submit">Buscar</Button>
    </form>
  );
}
