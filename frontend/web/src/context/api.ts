/// <reference types="vite/client" />

import { authService, createApiClient } from '@reboot/api-client';

export const api = createApiClient({
  VITE_API_BASE_URL: import.meta.env.VITE_API_BASE_URL,
  VITE_IDENTITY_API_URL: import.meta.env.VITE_IDENTITY_API_URL,
  VITE_ORGANIZATION_API_URL: import.meta.env.VITE_ORGANIZATION_API_URL,
});

export const identity = authService(api);
