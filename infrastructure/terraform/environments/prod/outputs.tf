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
output "active_directory_id" {
  description = "ID of the Active Directory service."
  value       = module.active_directory.directory_id
}

output "active_directory_dns_ip_addresses" {
  description = "DNS IP addresses of the Active Directory service."
  value       = module.active_directory.dns_ip_addresses
}

output "active_directory_ldap_url" {
  description = "LDAP connection URL for Active Directory."
  value       = module.active_directory.ldap_url
}
