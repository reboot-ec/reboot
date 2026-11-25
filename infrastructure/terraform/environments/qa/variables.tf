variable "project_name" {
  description = "REBOOT project name."
  type        = string
  default     = "reboot"
}

variable "environment" {
  description = "Deployment environment."
  type        = string
}

variable "aws_region" {
  description = "AWS deployment region."
  type        = string
  default     = "us-east-1"
}

variable "vpc_cidr" {
  description = "REBOOT VPC CIDR."
  type        = string
}

variable "private_subnet_cidrs" {
  description = "Private subnet CIDR blocks."
  type        = list(string)
}

variable "availability_zones" {
  description = "AWS availability zones."
  type        = list(string)
}

variable "db_instance_class" {
  description = "RDS instance class."
  type        = string
}

variable "db_allocated_storage" {
  description = "Initial RDS storage in GB."
  type        = number
}

variable "db_max_allocated_storage" {
  description = "Maximum RDS storage in GB."
  type        = number
}

variable "backup_retention_period" {
  description = "Backup retention in days."
  type        = number
}

variable "multi_az" {
  description = "Enable Multi-AZ deployment."
  type        = bool
}

variable "deletion_protection" {
  description = "Enable RDS deletion protection."
  type        = bool
}

variable "skip_final_snapshot" {
  description = "Skip final snapshot when deleting RDS."
  type        = bool
}

variable "identity_db_password" {
  description = "Identity database administrator password."
  type        = string
  sensitive   = true
}

variable "organization_db_password" {
  description = "Organization database administrator password."
  type        = string
  sensitive   = true
}

variable "directory_name" {
  description = "Fully qualified domain name for Active Directory."
  type        = string
  default     = "reboot.local"
}

variable "directory_password" {
  description = "Administrator password for Active Directory."
  type        = string
  sensitive   = true
}

variable "public_subnet_cidrs" {
  description = "Public subnet CIDR blocks."
  type        = list(string)
}