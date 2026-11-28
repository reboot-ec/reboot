import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import type { AuthTokens, LoginCredentials, User } from '@reboot/shared-types';
import { api, identity } from './api';

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  sessionError: unknown;
  login: (credentials: LoginCredentials) => Promise<User>;
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
  const [loading, setLoading] = useState(Boolean(initialSession));
  const [sessionError, setSessionError] = useState<unknown>(null);

  const syncSession = useCallback(() => {
    const tokens = api.getTokens();
    if (!tokens) setUser(null);
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
        if (!controller.signal.aborted) setUser(profile);
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

  async function login(credentials: LoginCredentials) {
    const { user: profile, ...tokens } = await identity.login(credentials);
    api.setTokens(tokens);
    setSessionError(null);
    syncSession();
    setUser(profile);
    return profile;
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

  async function logout() {
    try {
      await identity.logout();
      api.setTokens(null);
      setSessionError(null);
    } finally {
      syncSession();
    }
  }

  return <AuthContext.Provider value={{ user, loading, sessionError, login, logout, request }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth requires AuthProvider');
  return context;
}
