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

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets."
  type        = list(string)

  validation {
    condition     = length(var.public_subnet_cidrs) == length(var.availability_zones)
    error_message = "The number of public subnet CIDRs must match the number of availability zones."
  }
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets."
  type        = list(string)
}

variable "availability_zones" {
  description = "AWS availability zones for subnets."
  type        = list(string)

  validation {
    condition     = length(var.availability_zones) == length(var.private_subnet_cidrs)
    error_message = "The number of availability zones must match the number of private subnets."
  }
}
