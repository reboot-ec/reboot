import { Link } from 'react-router-dom';

export function DashboardActionCard({ title, description, to }: { title: string; description: string; to: string }) {
  return (
    <article className="card">
      <h2><Link to={to}>{title}</Link></h2>
      <p>{description}</p>
    </article>
  );
}
