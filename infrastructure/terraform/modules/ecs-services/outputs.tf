output "ecs_cluster_name" {
  description = "Name of the ECS cluster."
  value       = aws_ecs_cluster.main.name
}

output "identity_ecr_repository_url" {
  description = "URL of the Identity ECR repository."
  value       = aws_ecr_repository.identity.repository_url
}

output "organization_ecr_repository_url" {
  description = "URL of the Organization ECR repository."
  value       = aws_ecr_repository.organization.repository_url
}

output "ecs_tasks_security_group_id" {
  description = "Security group ID attached to the ECS tasks."
  value       = aws_security_group.tasks.id
}

output "identity_service_name" {
  description = "Name of the Identity ECS service."
  value       = aws_ecs_service.identity.name
}

output "organization_service_name" {
  description = "Name of the Organization ECS service."
  value       = aws_ecs_service.organization.name
}
