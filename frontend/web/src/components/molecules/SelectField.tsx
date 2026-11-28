import { useId, useState } from 'react';
import type { ComponentPropsWithoutRef } from 'react';
import { Label } from '../atoms/Label';
import { focusFirstInvalidField, validationMessage } from './validationMessage';

export function SelectField({ label, error, onInvalid, onChange, ...props }: ComponentPropsWithoutRef<'select'> & { label: string; error?: string }) {
  const id = useId();
  const [validationError, setValidationError] = useState('');
  const message = validationError || error;
  return (
    <div className="field">
      <Label htmlFor={id}>{label}</Label>
      <select {...props} id={id} aria-invalid={Boolean(message)} aria-describedby={message ? `${id}-error` : undefined}
        onInvalid={event => {
          event.preventDefault();
          const field = event.currentTarget;
          setValidationError(validationMessage(field));
          focusFirstInvalidField(field);
          onInvalid?.(event);
        }}
        onChange={event => {
          if (validationError) setValidationError(validationMessage(event.currentTarget));
          onChange?.(event);
        }}
      />
      {message && <small className="field-error" id={`${id}-error`}>{message}</small>}
    </div>
  );
}
