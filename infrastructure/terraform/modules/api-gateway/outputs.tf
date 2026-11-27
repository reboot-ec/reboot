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

