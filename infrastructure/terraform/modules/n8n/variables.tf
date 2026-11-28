variable "vpc_id" {
  description = "VPC containing the public subnet and n8n security group."
  type        = string
}

variable "public_subnet_id" {
  description = "Public subnet ID with a route to an internet gateway."
  type        = string
}

variable "instance_type" {
  description = "EC2 instance type compatible with the x86_64 AMI."
  type        = string
  default     = "t3.micro"
}

variable "allowed_ssh_cidr" {
  description = "IPv4 CIDR allowed to connect via SSH."
  type        = string
  default     = "0.0.0.0/0"
}

variable "key_name" {
  description = "Optional existing EC2 key pair name for SSH access."
  type        = string
  default     = null
}

variable "n8n_db_password" {
  description = "Password for the dedicated n8n PostgreSQL role."
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.n8n_db_password) > 0 && length(regexall("[[:cntrl:]]", var.n8n_db_password)) == 0
    error_message = "The database password must be nonempty and contain no control characters."
  }
}

variable "n8n_encryption_key" {
  description = "Persistent key used by n8n to encrypt stored credentials."
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.n8n_encryption_key) > 0 && length(regexall("[[:cntrl:]]", var.n8n_encryption_key)) == 0
    error_message = "The encryption key must be nonempty and contain no control characters."
  }
}
