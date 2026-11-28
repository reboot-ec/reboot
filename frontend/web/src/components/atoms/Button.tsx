import type { ComponentPropsWithoutRef } from 'react';

export function Button({ type = 'button', ...props }: ComponentPropsWithoutRef<'button'>) {
  return <button type={type} {...props} />;
}
