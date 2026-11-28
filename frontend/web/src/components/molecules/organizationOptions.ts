import type { Organization, OrganizationMembership } from '@reboot/shared-types';

export const organizationTypes = ['COMPANY', 'INSTITUTION', 'RECYCLING_CENTER', 'REPAIR_SHOP', 'NGO'] as const satisfies readonly Organization['organizationType'][];
export const organizationStatuses = ['ACTIVE', 'INACTIVE', 'SUSPENDED'] as const satisfies readonly Organization['status'][];
export const memberRoles = ['OWNER', 'ADMIN', 'MEMBER'] as const satisfies readonly OrganizationMembership['role'][];
export const memberStatuses = ['PENDING', 'ACTIVE', 'REVOKED'] as const satisfies readonly OrganizationMembership['status'][];
