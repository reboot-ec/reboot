variable "project_name" {
  description = "Project name for resource naming."
  type        = string
}

variable "environment" {
  description = "Deployment environment."
  type        = string
}

variable "vpc_id" {
  description = "VPC ID where Active Directory will be deployed."
  type        = string
}

variable "subnet_ids" {
  description = "List of private subnet IDs for Active Directory."
  type        = list(string)
}

variable "vpc_cidr" {
  description = "CIDR block of the VPC for security group rules."
  type        = string
}

variable "directory_name" {
  description = "The fully qualified name for the directory, such as corp.example.com."
  type        = string
  default     = "reboot.local"
}

variable "directory_password" {
  description = "The password for the directory administrator."
  type        = string
  sensitive   = true
}
