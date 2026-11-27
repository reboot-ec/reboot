output "alb_id" {
  description = "ID of the internal Application Load Balancer."
  value       = aws_lb.internal.id
}

output "alb_arn" {
  description = "ARN of the internal Application Load Balancer."
  value       = aws_lb.internal.arn
}

output "alb_dns_name" {
  description = "DNS name of the internal Application Load Balancer."
  value       = aws_lb.internal.dns_name
}

output "alb_security_group_id" {
  description = "Security group ID attached to the internal ALB."
  value       = aws_security_group.internal_alb.id
}

output "http_listener_arn" {
  description = "ARN of the ALB HTTP listener."
  value       = aws_lb_listener.http.arn
}

output "identity_target_group_arn" {
  description = "ARN of the Identity service target group."
  value       = aws_lb_target_group.identity.arn
}

output "organization_target_group_arn" {
  description = "ARN of the Organization service target group."
  value       = aws_lb_target_group.organization.arn
}
