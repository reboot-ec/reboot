variable "project_name" {
  description = "Project name used in resource names and tags."
  type        = string
  default     = "reboot"
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
}

variable "aws_region" {
  description = "AWS region used by the ECS awslogs driver."
  type        = string
  default     = "us-east-1"
}

variable "vpc_id" {
  description = "ID of the VPC hosting the ECS tasks."
  type        = string
}

variable "vpc_cidr" {
  description = "VPC CIDR used to restrict task egress to data and directory services."
  type        = string
}

variable "private_subnet_ids" {
  description = "Private subnet IDs for ECS tasks."
  type        = list(string)

  validation {
    condition     = length(var.private_subnet_ids) > 0
    error_message = "At least one private subnet ID is required for ECS tasks."
  }
}

variable "alb_security_group_id" {
  description = "Security group ID of the ALB allowed to reach the ECS tasks."
  type        = string
}

variable "identity_target_group_arn" {
  description = "ARN of the Identity service target group."
  type        = string
}

variable "organization_target_group_arn" {
  description = "ARN of the Organization service target group."
  type        = string
}

variable "task_execution_role_arn" {
  description = "Existing task execution role ARN, such as the AWS Academy LabRole."
  type        = string
}

variable "task_role_arn" {
  description = "Existing ECS task role ARN, such as the AWS Academy LabRole."
  type        = string
}

variable "identity_image_tag" {
  description = "Explicit image tag to deploy for Identity (for example v1.2.3 or qa); no floating default is used."
  type        = string

  validation {
    condition     = can(regex("^[A-Za-z0-9_][A-Za-z0-9_.-]{0,127}$", var.identity_image_tag))
    error_message = "identity_image_tag must be a valid Docker image tag of at most 128 characters."
  }
}

variable "organization_image_tag" {
  description = "Explicit image tag to deploy for Organization (for example v1.2.3 or qa); no floating default is used."
  type        = string

  validation {
    condition     = can(regex("^[A-Za-z0-9_][A-Za-z0-9_.-]{0,127}$", var.organization_image_tag))
    error_message = "organization_image_tag must be a valid Docker image tag of at most 128 characters."
  }
}

variable "identity_datasource_url" {
  description = "JDBC URL for the Identity database."
  type        = string
}

variable "identity_datasource_username" {
  description = "Username for the Identity database."
  type        = string
}

variable "organization_datasource_url" {
  description = "JDBC URL for the Organization database."
  type        = string
}

variable "organization_datasource_username" {
  description = "Username for the Organization database."
  type        = string
}

variable "identity_db_secret_arn" {
  description = "Secrets Manager ARN for Identity database credentials; its JSON must contain a password key."
  type        = string
}

variable "organization_db_secret_arn" {
  description = "Secrets Manager ARN for Organization database credentials; its JSON must contain a password key."
  type        = string
}

variable "jwt_secret_arn" {
  description = "Secrets Manager ARN containing the JWT secret value."
  type        = string
}

variable "ad_bind_password_secret_arn" {
  description = "Secrets Manager ARN containing the Active Directory bind password."
  type        = string
}

variable "ad_enabled" {
  description = "Whether Active Directory authentication is enabled for Identity."
  type        = bool
}

variable "ad_ldap_url" {
  description = "LDAP or LDAPS URL for Active Directory."
  type        = string
}

variable "ad_base_dn" {
  description = "Active Directory base distinguished name."
  type        = string
}

variable "ad_bind_dn" {
  description = "Active Directory bind distinguished name."
  type        = string
}

variable "cors_allowed_origins" {
  description = "Allowed CORS origins, serialized as a comma-separated environment variable."
  type        = list(string)

  validation {
    condition     = length(var.cors_allowed_origins) > 0
    error_message = "At least one CORS origin must be configured."
  }
}
