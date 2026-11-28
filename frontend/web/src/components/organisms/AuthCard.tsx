import type { FormEventHandler, ReactNode } from 'react';
import { Button } from '../atoms/Button';
import { ErrorBanner } from '../molecules/ErrorBanner';

interface AuthCardProps {
  title: string;
  submitLabel: string;
  onSubmit: FormEventHandler<HTMLFormElement>;
  pending: boolean;
  error: unknown;
  children: ReactNode;
  footer: ReactNode;
}

export function AuthCard({ title, submitLabel, onSubmit, pending, error, children, footer }: AuthCardProps) {
  return (
    <section className="card auth-card">
      <h1>{title}</h1>
      <ErrorBanner error={error} />
      <form onSubmit={onSubmit}>
        <fieldset disabled={pending}>
          {children}
          <Button type="submit">{pending ? 'Enviando…' : submitLabel}</Button>
        </fieldset>
      </form>
      <footer>{footer}</footer>
    </section>
  );
}
