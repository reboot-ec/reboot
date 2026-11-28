import { useNavigate } from 'react-router-dom';
import { useOrganizations } from '../../context/useOrganizations';
import { OrganizationForm } from './OrganizationForm';

export function CreateOrganizationForm() {
  const service = useOrganizations();
  const navigate = useNavigate();

  return <OrganizationForm onSave={async input => {
    const organization = await service.create(input);
    navigate(`/organizations/${organization.id}`, { replace: true });
  }} />;
}
