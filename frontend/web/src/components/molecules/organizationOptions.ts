import type { Organization, OrganizationMembership } from '@reboot/shared-types';

export const organizationTypes = ['COMPANY', 'INSTITUTION', 'RECYCLING_CENTER', 'REPAIR_SHOP', 'NGO'] as const satisfies readonly Organization['organizationType'][];
export const organizationStatuses = ['ACTIVE', 'INACTIVE', 'SUSPENDED'] as const satisfies readonly Organization['status'][];
export const memberRoles = ['OWNER', 'ADMIN', 'MEMBER'] as const satisfies readonly OrganizationMembership['role'][];
export const memberStatuses = ['PENDING', 'ACTIVE', 'REVOKED'] as const satisfies readonly OrganizationMembership['status'][];

export const organizationTypeLabels: Record<Organization['organizationType'], string> = {
  COMPANY: 'Company',
  INSTITUTION: 'Institution',
  RECYCLING_CENTER: 'Recycling center',
  REPAIR_SHOP: 'Repair shop',
  NGO: 'NGO',
};

export const statusLabels: Record<Organization['status'] | OrganizationMembership['status'], string> = {
  ACTIVE: 'Active',
  INACTIVE: 'Inactive',
  SUSPENDED: 'Suspended',
  PENDING: 'Pending',
  REVOKED: 'Revoked',
};

export const roleLabels: Record<OrganizationMembership['role'], string> = {
  OWNER: 'Owner',
  ADMIN: 'Administrator',
  MEMBER: 'Member',
};
