variable "project_name" {
  description = "REBOOT project name."
  type        = string
  default     = "reboot"
}

variable "environment" {
  description = "Deployment environment."
  type        = string
}

variable "vpc_id" {
  description = "VPC identifier exported by the networking module."
  type        = string
}

variable "private_subnet_ids" {
  description = "Private subnet identifiers exported by the networking module."
  type        = list(string)

  validation {
    condition     = length(var.private_subnet_ids) >= 2
    error_message = "At least two private subnet IDs are required for the internal ALB and VPC Link."
  }
}

variable "allow_origins" {
  description = "Origins allowed by the HTTP API CORS configuration."
  type        = list(string)

  validation {
    condition     = length(var.allow_origins) > 0
    error_message = "At least one CORS origin must be configured."
  }
}
