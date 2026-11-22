output "environment" {
  description = "Current deployment environment."
  value       = var.environment
}

output "vpc_id" {
  description = "REBOOT VPC ID."
  value       = module.networking.vpc_id
}

output "private_subnet_ids" {
  description = "REBOOT private subnet IDs."
  value       = module.networking.private_subnet_ids
}

output "identity_database_endpoint" {
  description = "Identity PostgreSQL endpoint."
  value       = module.identity_database.endpoint
}

output "organization_database_endpoint" {
  description = "Organization PostgreSQL endpoint."
  value       = module.organization_database.endpoint
}