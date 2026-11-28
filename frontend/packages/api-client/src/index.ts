import type { ApiErrorResponse, ApiResponse, AuthTokens } from '@reboot/shared-types';

export interface ApiEnvironment {
  VITE_API_BASE_URL?: string;
  VITE_IDENTITY_API_URL?: string;
  VITE_ORGANIZATION_API_URL?: string;
}

interface RequestOptions extends Omit<RequestInit, 'body' | 'redirect'> {
  json?: unknown;
  authenticated?: boolean;
  retryUnauthorized?: boolean;
}

const refreshPath = '/api/v1/identity/refresh';

export class ApiError extends Error {
  constructor(public readonly status: number, public readonly payload: ApiErrorResponse) {
    super(payload.error.message);
    this.name = 'ApiError';
  }
}

export function createApiClient(environment: ApiEnvironment = {}) {
  const gateway = environment.VITE_API_BASE_URL?.trim();
  let tokens: AuthTokens | null = null;
  let session = 0;
  let renewal: Promise<void> | undefined;

  function setTokens(value: AuthTokens | null) {
    session += 1;
    tokens = value;
    renewal = undefined;
  }

  function resolveUrl(path: string) {
    const resource = /^\/api\/v1\/([a-z-]+)(?=\/|\?|$)/.exec(path)?.[1];
    if (!resource) {
      throw new Error('Expected a relative /api/v1 resource path');
    }

    const localUrls: Record<string, string | undefined> = {
      identity: environment.VITE_IDENTITY_API_URL?.trim() || 'http://localhost:8081',
      organizations: environment.VITE_ORGANIZATION_API_URL?.trim() || 'http://localhost:8082',
    };
    const base = gateway || localUrls[resource];
    if (!base) {
      throw new Error(`Configure VITE_API_BASE_URL for resource "${resource}"`);
    }

    const url = new URL(base);
    if (!['http:', 'https:'].includes(url.protocol) || url.username || url.password || url.search || url.hash) {
      throw new Error('Expected an HTTP(S) API base URL without credentials, query or fragment');
    }
    return url.href.replace(/\/+$/, '') + path;
  }

  async function send(path: string, options: Omit<RequestOptions, 'authenticated' | 'retryUnauthorized'>, accessToken?: string) {
    const { json, ...init } = options;
    const headers = new Headers(init.headers);
    const requestId = crypto.randomUUID();
    headers.set('Accept', 'application/json');
    headers.set('X-Request-Id', requestId);
    headers.delete('Authorization');
    if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`);
    if (json !== undefined) headers.set('Content-Type', 'application/json');

    const response = await fetch(resolveUrl(path), {
      ...init,
      headers,
      body: json === undefined ? undefined : JSON.stringify(json),
      redirect: 'error',
    });
    return { response, requestId };
  }

  async function readError(response: Response, requestId: string) {
    const payload: ApiErrorResponse | null = await response.json().catch(() => null);
    if (typeof payload?.error?.message === 'string' && typeof payload.error.code === 'string') {
      return new ApiError(response.status, payload);
    }
    return new ApiError(response.status, {
      error: {
        code: `HTTP_${response.status}`,
        message: response.statusText || `HTTP ${response.status}`,
        details: [],
        requestId: response.headers.get('X-Request-Id') || requestId,
      },
    });
  }

  function renew(expectedSession: number, refreshToken: string) {
    renewal ??= (async () => {
      const { response, requestId } = await send(refreshPath, {
        method: 'POST',
        json: { refreshToken },
      });
      if (!response.ok) {
        if (response.status === 401 && session === expectedSession) setTokens(null);
        throw await readError(response, requestId);
      }

      const { data: { accessToken, refreshToken: rotatedToken, expiresIn } }: ApiResponse<AuthTokens> = await response.json();
      if (session === expectedSession) tokens = { accessToken, refreshToken: rotatedToken, expiresIn };
    })().finally(() => {
      if (session === expectedSession) renewal = undefined;
    });
    return renewal;
  }

  async function refresh() {
    if (tokens) await renew(session, tokens.refreshToken);
  }

  async function request<T>(path: string, { authenticated = true, retryUnauthorized = true, ...options }: RequestOptions = {}): Promise<ApiResponse<T> | undefined> {
    const expectedSession = session;
    const auth = authenticated && path.split('?')[0] !== refreshPath;
    const initialTokens = auth ? tokens : null;
    let result = await send(path, options, initialTokens?.accessToken);
    if (retryUnauthorized && result.response.status === 401 && initialTokens && session === expectedSession) {
      options.signal?.throwIfAborted();
      if (tokens === initialTokens) await renew(expectedSession, initialTokens.refreshToken);
      if (session === expectedSession && tokens) {
        result = await send(path, options, tokens.accessToken);
        if (result.response.status === 401 && session === expectedSession) setTokens(null);
      }
    }

    if (!result.response.ok) throw await readError(result.response, result.requestId);
    if (result.response.status === 204) return undefined;
    return result.response.json();
  }

  return { request, refresh, setTokens, getTokens: () => tokens, pendingRefresh: () => renewal };
}

export { authService } from './authService.ts';
