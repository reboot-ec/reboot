variable "project_name" {
  description = "Project name."
  type        = string
  default     = "reboot"
}

variable "environment" {
  description = "Deployment environment."
  type        = string
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC."
  type        = string
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets."
  type        = list(string)
}

variable "availability_zones" {
  description = "AWS availability zones for private subnets."
  type        = list(string)

  validation {
    condition     = length(var.availability_zones) == length(var.private_subnet_cidrs)
    error_message = "The number of availability zones must match the number of private subnets."
  }
}