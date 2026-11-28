import { useId, useState } from 'react';
import type { ComponentPropsWithoutRef } from 'react';
import { Input } from '../atoms/Input';
import { Label } from '../atoms/Label';

export function FormField({
  label, error, onInvalid, onChange, 'aria-describedby': describedBy, ...input
}: ComponentPropsWithoutRef<'input'> & { label: string; error?: string }) {
  const id = useId();
  const [validationError, setValidationError] = useState('');
  const message = validationError || error;
  const description = [describedBy, message && `${id}-error`].filter(Boolean).join(' ');

  return (
    <div className="field">
      <Label htmlFor={id}>{label}</Label>
      <Input {...input} id={id} aria-invalid={Boolean(message)} aria-describedby={description || undefined}
        onInvalid={event => {
          setValidationError(event.currentTarget.validationMessage);
          onInvalid?.(event);
        }}
        onChange={event => {
          if (validationError) setValidationError(event.currentTarget.validationMessage);
          onChange?.(event);
        }}
      />
      {message && <small className="field-error" id={`${id}-error`}>{message}</small>}
    </div>
  );
}
