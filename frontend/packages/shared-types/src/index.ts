export interface User {
  id: string;
  email: string;
  username: string;
  fullName: string;
  authProvider: 'LOCAL' | 'ACTIVE_DIRECTORY';
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED';
  roles: string[];
  createdAt: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface Organization {
  id: string;
  name: string;
  taxId: string | null;
  organizationType: 'COMPANY' | 'INSTITUTION' | 'RECYCLING_CENTER' | 'REPAIR_SHOP' | 'NGO';
  description: string | null;
  contactEmail: string | null;
  contactPhone: string | null;
  address: string | null;
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  createdByUserId: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrganizationMembership {
  id: string;
  organizationId: string;
  userId: string;
  role: 'OWNER' | 'ADMIN' | 'MEMBER';
  status: 'PENDING' | 'ACTIVE' | 'REVOKED';
  invitedByUserId: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ApiResponse<T> {
  data: T;
  meta: { requestId: string };
}

export interface ApiErrorResponse {
  error: {
    code: string;
    message: string;
    details: string[];
    requestId: string;
  };
}
