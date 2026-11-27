CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    tax_id VARCHAR(100),
    organization_type VARCHAR(50) NOT NULL,
    description TEXT,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    address TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_by_user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_organizations_name UNIQUE (name),
    CONSTRAINT uk_organizations_tax_id UNIQUE (tax_id),
    CONSTRAINT chk_organizations_type CHECK (
        organization_type IN ('COMPANY', 'INSTITUTION', 'RECYCLING_CENTER', 'REPAIR_SHOP', 'NGO')
    ),
    CONSTRAINT chk_organizations_status CHECK (
        status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')
    )
);

CREATE INDEX idx_organizations_status ON organizations(status);
CREATE INDEX idx_organizations_type ON organizations(organization_type);

CREATE TABLE organization_memberships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    invited_by_user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_organization_memberships_organization_user UNIQUE (organization_id, user_id),
    CONSTRAINT fk_organization_memberships_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT chk_organization_memberships_role CHECK (
        role IN ('OWNER', 'ADMIN', 'MEMBER')
    ),
    CONSTRAINT chk_organization_memberships_status CHECK (
        status IN ('PENDING', 'ACTIVE', 'REVOKED')
    )
);

CREATE INDEX idx_organization_memberships_user_id ON organization_memberships(user_id);
CREATE INDEX idx_organization_memberships_status ON organization_memberships(status);
