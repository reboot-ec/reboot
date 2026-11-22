# REBOOT - Terraform Infrastructure

## Overview

This directory contains the Infrastructure as Code (IaC) configuration for the REBOOT project, using Terraform and the AWS provider.

The infrastructure is organized into reusable modules and independent deployment environments.

## Directory Structure

```text
terraform/
├── modules/
│   ├── networking/
│   └── database/
└── environments/
    ├── qa/
    └── prod/
```

## Environments
### Quality Assurance (QA)

Used for integration testing and quality validation.

- Medium database instances.
- Increased storage capacity.
- Extended backup retention.
- Independent Identity and Organization databases.

### Production (Prod)

Designed for production workloads.

- Larger database instances.
- Increased storage capacity.
- Multi-AZ database deployment.
- Extended backup retention.
- Database deletion protection.
- Final snapshots.

Production resources are configured with greater capacity than QA and Dev.

## Reusable Modules

### Networking

Defines:

- AWS VPC.
- Private subnets.
- Environment-specific network configuration.

### Database

Defines:

- Amazon RDS PostgreSQL instances.
- Database subnet groups.
- Database security groups.
- Restricted access from authorized application security groups.
- Storage encryption.
- Backup and availability settings.

Identity and Organization use independent PostgreSQL databases following the Database per Microservice pattern.

## Terraform Backend

During local development, Terraform uses its default local backend.

Each environment maintains an independent state file.

The remote S3 backend will be configured during the AWS deployment phase, when AWS Academy Learner Lab access and permissions are available.

## Authentication

AWS Academy Learner Lab uses temporary AWS credentials.

AWS credentials must not be committed to the repository.

When AWS deployment is required, credentials will be configured using the temporary credentials provided by the laboratory.

## Local Validation

Install Terraform and initialize each environment independently.

Example for Dev:

```powershell
cd infrastructure/terraform/environments/dev
terraform init
terraform validate
```

Repeat the process for QA and Prod.

## Deployment

AWS resources are not deployed during the initial Terraform project setup.

Resource deployment will take place in a later infrastructure deployment phase after verifying AWS Academy Learner Lab permissions, availability, and remaining credits.

Never execute `terraform apply` without reviewing the execution plan and expected AWS costs.