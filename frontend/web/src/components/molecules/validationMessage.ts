export function focusFirstInvalidField(field: HTMLInputElement | HTMLSelectElement) {
  if (field.form?.querySelector('input:invalid, select:invalid, textarea:invalid') === field) field.focus();
}

export function validationMessage(field: HTMLInputElement | HTMLSelectElement) {
  const { validity } = field;
  if (validity.valid) return '';
  if (validity.valueMissing) return field instanceof HTMLSelectElement ? 'Select an option.' : 'This field is required.';
  if (field instanceof HTMLInputElement) {
    if (validity.typeMismatch && field.type === 'email') return 'Enter a valid email address.';
    if (validity.tooShort) return `Enter at least ${field.minLength} characters.`;
    if (validity.tooLong) return `Enter no more than ${field.maxLength} characters.`;
    if (validity.patternMismatch && field.name === 'userId') return 'Enter a valid user ID (UUID).';
    if (validity.patternMismatch && field.name === 'name') return 'Enter an organization name.';
  }
  return 'Check the value and try again.';
}
