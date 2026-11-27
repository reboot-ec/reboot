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

output "api_gateway_invoke_url" {
  description = "Invoke URL for the HTTP API."
  value       = module.api_gateway.api_endpoint
}

output "internal_alb_dns_name" {
  description = "DNS name of the internal application load balancer."
  value       = module.alb.alb_dns_name
}

output "identity_target_group_arn" {
  description = "ARN of the Identity service target group for container registration."
  value       = module.alb.identity_target_group_arn
}

output "organization_target_group_arn" {
  description = "ARN of the Organization service target group for container registration."
  value       = module.alb.organization_target_group_arn
}
