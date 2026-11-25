output "directory_id" {
  description = "ID of the Directory Service."
  value       = aws_directory_service_directory.main.id
}

output "dns_ip_addresses" {
  description = "List of DNS IP addresses associated with the directory."
  value       = aws_directory_service_directory.main.dns_ip_addresses
}

output "ldap_url" {
  description = "LDAP connection URL using the first DNS IP address."
  value       = "ldap://${aws_directory_service_directory.main.dns_ip_addresses[0]}:389"
}
