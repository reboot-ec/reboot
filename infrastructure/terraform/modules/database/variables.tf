variable "identifier" {
  description = "Unique RDS instance identifier."
  type        = string
}

variable "database_name" {
  description = "PostgreSQL database name."
  type        = string
}

variable "master_username" {
  description = "Database administrator username."
  type        = string
}

variable "master_password" {
  description = "Database administrator password."
  type        = string
  sensitive   = true
}

variable "instance_class" {
  description = "RDS instance class."
  type        = string
}

variable "allocated_storage" {
  description = "Initial database storage in GB."
  type        = number
}

variable "max_allocated_storage" {
  description = "Maximum autoscaled database storage in GB."
  type        = number
}

variable "backup_retention_period" {
  description = "Number of days to retain backups."
  type        = number
}

variable "multi_az" {
  description = "Whether to enable Multi-AZ deployment."
  type        = bool
}

variable "deletion_protection" {
  description = "Whether deletion protection is enabled."
  type        = bool
}

variable "skip_final_snapshot" {
  description = "Whether to skip the final snapshot when deleting."
  type        = bool
}

variable "environment" {
  description = "Deployment environment."
  type        = string
}

variable "vpc_id" {
  description = "VPC identifier."
  type        = string
}

variable "subnet_ids" {
  description = "Private subnet identifiers."
  type        = list(string)
}

variable "allowed_security_group_ids" {
  description = "Application security groups allowed to access PostgreSQL."
  type        = list(string)
}
