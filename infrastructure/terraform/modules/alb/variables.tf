variable "project_name" {
  description = "Project name used in resource names and tags."
  type        = string
  default     = "reboot"
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
}

variable "vpc_id" {
  description = "ID of the VPC hosting the ALB and target groups."
  type        = string
}

variable "vpc_cidr" {
  description = "CIDR block used to limit ALB egress to targets within the VPC."
  type        = string
}

variable "private_subnet_ids" {
  description = "Private subnet IDs for the internal ALB."
  type        = list(string)

  validation {
    condition     = length(var.private_subnet_ids) >= 2
    error_message = "At least two private subnet IDs are required for the internal ALB."
  }
}

variable "vpc_link_security_group_id" {
  description = "Security group ID of the API Gateway VPC Link allowed to reach the ALB."
  type        = string
}
