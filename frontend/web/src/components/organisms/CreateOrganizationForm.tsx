import { useNavigate } from 'react-router-dom';
import { useOrganizations } from '../../context/useOrganizations';
import { useAuth } from '../../context/AuthContext';
import { OrganizationForm } from './OrganizationForm';

export function CreateOrganizationForm() {
  const service = useOrganizations();
  const { refreshOrganization } = useAuth();
  const navigate = useNavigate();

  return <OrganizationForm onSave={async input => {
    const organization = await service.create(input);
    refreshOrganization();
    navigate(`/organizations/${organization.id}`, { replace: true });
  }} />;
}
