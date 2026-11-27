output "api_endpoint" {
  description = "Invoke URL for the HTTP API."
  value       = aws_apigatewayv2_api.http.api_endpoint
}

output "api_id" {
  description = "ID of the HTTP API."
  value       = aws_apigatewayv2_api.http.id
}

output "vpc_link_id" {
  description = "ID of the API Gateway VPC Link."
  value       = aws_apigatewayv2_vpc_link.main.id
}

output "internal_alb_dns_name" {
  description = "DNS name of the internal application load balancer."
  value       = aws_lb.internal.dns_name
}

output "identity_target_group_arn" {
  description = "ARN of the Identity service target group."
  value       = aws_lb_target_group.identity_tg.arn
}

output "organization_target_group_arn" {
  description = "ARN of the Organization service target group."
  value       = aws_lb_target_group.organization_tg.arn
}
