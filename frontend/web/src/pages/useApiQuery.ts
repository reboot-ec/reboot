import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';

export function useApiQuery<T>(path: string): { data?: T; error?: unknown; pending: boolean } {
  const { request } = useAuth();
  const [result, setResult] = useState<{ path: string; data?: T; error?: unknown }>();

  useEffect(() => {
    const controller = new AbortController();
    request<T>(path, { signal: controller.signal }).then(
      response => {
        if (!controller.signal.aborted) setResult({ path, data: response?.data });
      },
      error => {
        if (!controller.signal.aborted) setResult({ path, error });
      },
    );
    return () => controller.abort();
  }, [path, request]);

  return result?.path === path ? { ...result, pending: false } : { pending: true };
}
