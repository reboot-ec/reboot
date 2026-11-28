import { useMemo } from 'react';
import { organizationService } from '@reboot/api-client';
import { useAuth } from './AuthContext';

export function useOrganizations() {
  const { request } = useAuth();
  return useMemo(() => organizationService({ request }), [request]);
}
