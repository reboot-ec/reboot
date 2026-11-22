output "vpc_id" {
  description = "ID of the REBOOT VPC."
  value       = aws_vpc.main.id
}

output "private_subnet_ids" {
  description = "IDs of private subnets."
  value       = aws_subnet.private[*].id
}