variable "project_name" {
  description = "REBOOT project name."
  type        = string
  default     = "reboot"
}

variable "environment" {
  description = "Deployment environment."
  type        = string
}

variable "private_subnet_ids" {
  description = "Private subnet identifiers exported by the networking module."
  type        = list(string)

  validation {
    condition     = length(var.private_subnet_ids) >= 2
    error_message = "At least two private subnet IDs are required for the VPC Link."
  }
}

variable "vpc_link_security_group_id" {
  description = "Security group ID attached to the API Gateway VPC Link."
  type        = string
}

variable "alb_security_group_id" {
  description = "Security group ID attached to the internal ALB."
  type        = string
}

variable "alb_listener_arn" {
  description = "ARN of the internal ALB HTTP listener used by the API integration."
  type        = string
}

variable "allow_origins" {
  description = "Origins allowed by the HTTP API CORS configuration."
  type        = list(string)

  validation {
    condition     = length(var.allow_origins) > 0
    error_message = "At least one CORS origin must be configured."
  }
}
