import { Link } from 'react-router-dom';
import { CreateOrganizationForm } from '../components/organisms/CreateOrganizationForm';

export function CreateOrganizationPage() {
  return (
    <section className="card">
      <header className="directory-header">
        <h1>Register an Organization</h1>
        <Link className="secondary-action" to="/organizations">Cancel</Link>
      </header>
      <CreateOrganizationForm />
    </section>
  );
}
