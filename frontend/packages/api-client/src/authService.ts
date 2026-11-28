import type { AuthSession, LoginCredentials, Registration, User } from '@reboot/shared-types';
import { ApiError } from './index.ts';
import type { createApiClient } from './index.ts';

export function authService(client: ReturnType<typeof createApiClient>) {
  const base = '/api/v1/identity';

  return {
    async login(credentials: LoginCredentials) {
      const response = await client.request<AuthSession>(`${base}/login`, {
        method: 'POST', authenticated: false, json: { ...credentials, clientType: 'WEB' },
      });
      if (!response?.data.user) throw new Error('Missing login response');
      return response.data;
    },
    register(registration: Registration) {
      return client.request<User>(`${base}/register`, {
        method: 'POST', authenticated: false, json: registration,
      });
    },
    refresh: client.refresh,
    async logout() {
      function revoke(refreshToken: string) {
        return client.request(`${base}/logout`, {
          method: 'POST', retryUnauthorized: false, json: { refreshToken },
        });
      }

      await client.pendingRefresh();
      const tokens = client.getTokens();
      if (!tokens) return;
      try {
        await revoke(tokens.refreshToken);
      } catch (error) {
        if (!(error instanceof ApiError) || error.status !== 401) throw error;
        if (client.getTokens() === tokens) await client.refresh();
      }
      await client.pendingRefresh();
      const updated = client.getTokens();
      if (updated && updated !== tokens) await revoke(updated.refreshToken);
    },
    async me(signal?: AbortSignal) {
      const response = await client.request<User>(`${base}/me`, { signal });
      if (!response?.data) throw new Error('Missing profile response');
      return response.data;
    },
  };
}
