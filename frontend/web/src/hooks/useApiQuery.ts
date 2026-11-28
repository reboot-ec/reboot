import { useEffect, useState } from 'react';

export function useApiQuery<T>(load: (signal: AbortSignal) => Promise<T>) {
  const [version, setVersion] = useState(0);
  const [result, setResult] = useState<{ load: typeof load; version: number; data?: T; error?: unknown }>();
  const current = result?.load === load && result.version === version ? result : undefined;

  useEffect(() => {
    const controller = new AbortController();
    load(controller.signal).then(
      data => {
        if (!controller.signal.aborted) setResult({ load, version, data });
      },
      error => {
        if (!controller.signal.aborted) setResult({ load, version, error });
      },
    );
    return () => controller.abort();
  }, [load, version]);

  function updateData(update: (value: T) => T) {
    setResult(previous => previous?.load === load && previous.version === version && previous.data !== undefined
      ? { load, version, data: update(previous.data) }
      : previous);
  }

  return { data: current?.data, error: current?.error, pending: !current, updateData, reload: () => setVersion(value => value + 1) };
}
