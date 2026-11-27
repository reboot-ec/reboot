package com.reboot.organization.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "organizations",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_organizations_name", columnNames = "name"),
        @UniqueConstraint(name = "uk_organizations_tax_id", columnNames = "tax_id")
    }
)
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Size(max = 100)
    @Column(name = "tax_id", length = 100)
    private String taxId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "organization_type", nullable = false, length = 50)
    private OrganizationType organizationType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Email
    @Size(max = 255)
    @Column(name = "contact_email")
    private String contactEmail;

    @Size(max = 50)
    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    @NotNull
    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Organization() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(OrganizationType organizationType) {
        this.organizationType = organizationType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public OrganizationStatus getStatus() {
        return status;
    }

    public void setStatus(OrganizationStatus status) {
        this.status = status;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(UUID createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
