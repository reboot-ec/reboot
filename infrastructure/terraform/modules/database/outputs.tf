output "endpoint" {
  description = "RDS endpoint."
  value       = aws_db_instance.main.address
}

output "port" {
  description = "Database connection port."
  value       = aws_db_instance.main.port
}

output "database_name" {
  description = "Database name."
  value       = aws_db_instance.main.db_name
}

output "security_group_id" {
  description = "Database security group ID."
  value       = aws_security_group.database.id
}

output "secret_arn" {
  description = "AWS Secrets Manager secret ARN containing database credentials."
  value       = aws_secretsmanager_secret.database.arn
  depends_on  = [aws_secretsmanager_secret_version.database]
}

output "secret_name" {
  description = "AWS Secrets Manager secret name containing database credentials."
  value       = aws_secretsmanager_secret.database.name
}

