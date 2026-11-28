import { useNavigate } from 'react-router-dom';
import { useOrganizations } from '../../context/useOrganizations';
import { OrganizationForm } from './OrganizationForm';

export function CreateOrganizationForm() {
  const service = useOrganizations();
  const navigate = useNavigate();

  return (
    <details className="card">
      <summary>Registrar organización</summary>
      <OrganizationForm onSave={async input => {
        const organization = await service.create(input);
        navigate(`/organizations/${organization.id}`);
      }} />
    </details>
  );
}
