import { useId } from 'react';
import type { ComponentPropsWithoutRef } from 'react';
import { Label } from '../atoms/Label';

export function SelectField({ label, error, ...props }: ComponentPropsWithoutRef<'select'> & { label: string; error?: string }) {
  const id = useId();
  return (
    <div className="field">
      <Label htmlFor={id}>{label}</Label>
      <select {...props} id={id} aria-invalid={Boolean(error)} aria-describedby={error ? `${id}-error` : undefined} />
      {error && <small className="field-error" id={`${id}-error`}>{error}</small>}
    </div>
  );
}
