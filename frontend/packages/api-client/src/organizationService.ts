import type { ApiResponse, Organization, OrganizationMembership } from '@reboot/shared-types';
import type { createApiClient } from './index.ts';

export type OrganizationInput = Pick<Organization, 'name' | 'organizationType'> &
  Partial<Pick<Organization, 'taxId' | 'description' | 'contactEmail' | 'contactPhone' | 'address'>>;
export type OrganizationPatch = Partial<OrganizationInput & Pick<Organization, 'status'>>;
export type MemberInput = Pick<OrganizationMembership, 'userId' | 'role'>;
export type MemberPatch = Partial<Pick<OrganizationMembership, 'role' | 'status'>>;

export interface OrganizationQuery {
  page?: number;
  pageSize?: number;
  name?: string;
  type?: Organization['organizationType'] | '';
  types?: readonly Organization['organizationType'][];
}

interface OrganizationPage {
  items: Organization[];
  page: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}

export function organizationService({ request }: Pick<ReturnType<typeof createApiClient>, 'request'>) {
  const base = '/api/v1/organizations';
  const organization = (id: string) => `${base}/${encodeURIComponent(id)}`;
  const members = (id: string) => `${organization(id)}/members`;

  async function data<T>(response: Promise<ApiResponse<T> | undefined>) {
    const result = await response;
    if (!result) throw new Error('Missing organization service response');
    return result.data;
  }

  return {
    list({ page = 0, pageSize = 20, name, type, types }: OrganizationQuery = {}, signal?: AbortSignal) {
      const params = new URLSearchParams({ page: String(page), pageSize: String(pageSize) });
      if (name?.trim()) params.set('name', name.trim());
      for (const value of types ?? (type ? [type] : [])) params.append('type', value);
      return data(request<OrganizationPage>(`${base}?${params}`, { signal }));
    },
    create(json: OrganizationInput) {
      return data(request<Organization>(base, { method: 'POST', json }));
    },
    listMine(page = 0, pageSize = 20, signal?: AbortSignal) {
      const params = new URLSearchParams({ page: String(page), pageSize: String(pageSize) });
      return data(request<OrganizationPage>(`${base}/mine?${params}`, { signal }));
    },
    get(id: string, signal?: AbortSignal) {
      return data(request<Organization>(organization(id), { signal, authenticated: false }));
    },
    update(id: string, json: OrganizationPatch) {
      return data(request<Organization>(organization(id), { method: 'PATCH', json }));
    },
    listMembers(id: string, signal?: AbortSignal) {
      return data(request<OrganizationMembership[]>(members(id), { signal }));
    },
    addMember(id: string, json: MemberInput) {
      return data(request<OrganizationMembership>(members(id), { method: 'POST', json }));
    },
    updateMember(id: string, memberId: string, json: MemberPatch) {
      return data(request<OrganizationMembership>(`${members(id)}/${encodeURIComponent(memberId)}`, { method: 'PATCH', json }));
    },
  };
}
