import { Badge } from '../atoms/Badge';

export function OperationalActionCard({ title, description, status }: {
  title: string;
  description: string;
  status?: string;
}) {
  return (
    <article className="card">
      <h2>{title}</h2>
      <p>{description}</p>
      {status && <Badge>{status}</Badge>}
    </article>
  );
}
