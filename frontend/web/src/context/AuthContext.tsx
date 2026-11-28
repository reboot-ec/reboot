import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import type { AuthTokens, LoginCredentials, User } from '@reboot/shared-types';
import { organizationService } from '@reboot/api-client';
import { getProfileType } from './profileIntent';
import type { ProfileIntent, ProfileType } from './profileIntent';
import { api, identity } from './api';

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  sessionError: unknown;
  login: (credentials: LoginCredentials, registration?: { userId: string; intent: ProfileIntent }) => Promise<void>;
  profileType: ProfileType | null;
  organizationId: string | null;
  organizationPending: boolean;
  organizationError: unknown;
  refreshOrganization: () => void;
  logout: () => Promise<void>;
  request: typeof api.request;
}

interface StoredSession {
  tokens: AuthTokens;
  expiresAt: number;
}

const storageKey = 'reboot.session';
const AuthContext = createContext<AuthContextValue | null>(null);

function readSession(): StoredSession | null {
  try {
    const saved: StoredSession | null = JSON.parse(sessionStorage.getItem(storageKey) ?? 'null');
    if (typeof saved?.tokens?.accessToken === 'string' && saved.tokens.accessToken &&
        typeof saved.tokens.refreshToken === 'string' && saved.tokens.refreshToken &&
        Number.isFinite(saved.tokens.expiresIn) && saved.tokens.expiresIn > 0 &&
        Number.isFinite(saved.expiresAt)) return saved;
  } catch {
    // The browser can deny storage access.
  }
  return null;
}

function persistSession(session: StoredSession | null) {
  try {
    if (session) sessionStorage.setItem(storageKey, JSON.stringify(session));
    else sessionStorage.removeItem(storageKey);
  } catch {
    // Authentication can continue in memory.
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [initialSession] = useState(readSession);
  const [session, setSession] = useState(initialSession);
  const [user, setUser] = useState<User | null>(null);
  const [profileType, setProfileType] = useState<ProfileType | null>(null);
  const [organizationVersion, setOrganizationVersion] = useState(0);
  const [organization, setOrganization] = useState<{ userId: string; version: number; id: string | null; error?: unknown }>();
  const [loading, setLoading] = useState(Boolean(initialSession));
  const [sessionError, setSessionError] = useState<unknown>(null);

  const syncSession = useCallback(() => {
    const tokens = api.getTokens();
    if (!tokens) {
      setUser(null);
      setProfileType(null);
    }
    setSession(current => {
      if (current?.tokens === tokens || (!current && !tokens)) return current;
      return tokens ? { tokens, expiresAt: Date.now() + tokens.expiresIn * 1000 } : null;
    });
  }, []);

  useEffect(() => {
    if (!initialSession) return;
    const controller = new AbortController();
    api.setTokens(initialSession.tokens);

    identity.me(controller.signal).then(
      profile => {
        if (!controller.signal.aborted) {
          setUser(profile);
          setProfileType(getProfileType(profile.id));
        }
      },
      error => {
        if (controller.signal.aborted) return;
        setSessionError(error);
        api.setTokens(null);
      },
    ).finally(() => {
      if (controller.signal.aborted) return;
      syncSession();
      setLoading(false);
    });
    return () => controller.abort();
  }, [initialSession, syncSession]);

  useEffect(() => {
    persistSession(session);
  }, [session]);

  useEffect(() => {
    if (!session || !user || loading) return;
    let active = true;
    const timer = window.setTimeout(async () => {
      if (api.getTokens() !== session.tokens) {
        syncSession();
        return;
      }
      try {
        await identity.refresh();
      } catch (error) {
        if (active && (!api.getTokens() || api.getTokens() === session.tokens)) {
          api.setTokens(null);
          setSessionError(error);
        }
      } finally {
        if (active) syncSession();
      }
    }, Math.max(0, session.expiresAt - Date.now()));
    return () => {
      active = false;
      window.clearTimeout(timer);
    };
  }, [session, user, loading, syncSession]);

  async function login(credentials: LoginCredentials, registration?: { userId: string; intent: ProfileIntent }) {
    const { user: profile, ...tokens } = await identity.login(credentials);
    api.setTokens(tokens);
    setSessionError(null);
    syncSession();
    setOrganization(undefined);
    setUser(profile);
    setProfileType(getProfileType(profile.id, registration));
  }

  const request = useCallback(async <T,>(...args: Parameters<typeof api.request>) => {
    try {
      return await api.request<T>(...args);
    } catch (error) {
      if (!api.getTokens()) setSessionError(error);
      throw error;
    } finally {
      syncSession();
    }
  }, [syncSession]);

  const representativeId = profileType === 'ORG_REPRESENTATIVE' ? user?.id : undefined;
  useEffect(() => {
    if (!representativeId) return;
    const controller = new AbortController();
    const service = organizationService({ request });
    service.listMine(0, 1, controller.signal).then(
      result => {
        if (!controller.signal.aborted) setOrganization({ userId: representativeId, version: organizationVersion, id: result.items[0]?.id ?? null });
      },
      error => {
        if (!controller.signal.aborted) setOrganization({ userId: representativeId, version: organizationVersion, id: null, error });
      },
    );
    return () => controller.abort();
  }, [representativeId, organizationVersion, request]);

  const currentOrganization = representativeId && organization?.userId === representativeId &&
    organization.version === organizationVersion ? organization : undefined;

  async function logout() {
    try {
      await identity.logout();
      api.setTokens(null);
      setSessionError(null);
    } finally {
      syncSession();
    }
  }

  return <AuthContext.Provider value={{
    user, loading, sessionError, login, logout, request, profileType,
    organizationId: currentOrganization?.id ?? null,
    organizationPending: Boolean(representativeId && !currentOrganization),
    organizationError: currentOrganization?.error,
    refreshOrganization: () => setOrganizationVersion(version => version + 1),
  }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth requires AuthProvider');
  return context;
}
