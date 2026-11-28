import { ApiError } from '@reboot/api-client';
import { Alert } from '../atoms/Alert';

export function fieldError(error: unknown, field: string) {
  if (!(error instanceof ApiError) || error.payload.error.code !== 'VALIDATION_FAILED') return;
  const prefix = `${field}: `;
  return error.payload.error.details?.find(detail => detail.startsWith(prefix))?.slice(prefix.length);
}

export function ErrorBanner({ error }: { error: unknown }) {
  if (error == null) return null;
  const response = error instanceof ApiError ? error.payload.error : undefined;

  return (
    <Alert>
      <p>{response?.message ?? 'Something went wrong. Please try again.'}</p>
      {response?.details?.length ? <ul>{response.details.map((detail, index) => <li key={index}>{detail}</li>)}</ul> : null}
      {response?.requestId && <small>Request ID <code>{response.requestId}</code></small>}
    </Alert>
  );
}
