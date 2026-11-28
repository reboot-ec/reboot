ALTER TABLE organizations
    ADD CONSTRAINT uk_organizations_created_by_user UNIQUE (created_by_user_id);
