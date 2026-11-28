import type { ReactNode } from 'react';

export function CardHeader({ title, children }: { title: ReactNode; children?: ReactNode }) {
  return <header className="card-header"><h2>{title}</h2>{children}</header>;
}
