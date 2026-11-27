terraform {
  required_version = ">= 1.10.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

resource "aws_apigatewayv2_vpc_link" "main" {
  name               = "${var.project_name}-${var.environment}-vpc-link"
  subnet_ids         = var.private_subnet_ids
  security_group_ids = [var.vpc_link_security_group_id]

  tags = {
    Name        = "${var.project_name}-${var.environment}-vpc-link"
    Environment = var.environment
  }
}

resource "aws_apigatewayv2_api" "http" {
  name          = "${var.project_name}-${var.environment}-http-api"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = var.allow_origins
    allow_methods = ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"]
    allow_headers = [
      "Authorization",
      "Content-Type",
      "X-Request-Id",
      "Idempotency-Key"
    ]
    expose_headers = ["X-Request-Id"]
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-http-api"
    Environment = var.environment
  }
}

resource "aws_apigatewayv2_integration" "alb" {
  api_id                 = aws_apigatewayv2_api.http.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "ANY"
  integration_uri        = var.http_listener_arn
  connection_type        = "VPC_LINK"
  connection_id          = aws_apigatewayv2_vpc_link.main.id
  payload_format_version = "1.0"
}

resource "aws_apigatewayv2_route" "identity_proxy" {
  api_id    = aws_apigatewayv2_api.http.id
  route_key = "ANY /api/v1/identity/{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.alb.id}"
}

resource "aws_apigatewayv2_route" "organizations" {
  api_id    = aws_apigatewayv2_api.http.id
  route_key = "ANY /api/v1/organizations"
  target    = "integrations/${aws_apigatewayv2_integration.alb.id}"
}

resource "aws_apigatewayv2_route" "organizations_proxy" {
  api_id    = aws_apigatewayv2_api.http.id
  route_key = "ANY /api/v1/organizations/{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.alb.id}"
}

resource "aws_cloudwatch_log_group" "api_access" {
  name              = "/aws/apigateway/${var.project_name}-${var.environment}-http-api"
  retention_in_days = 7

  tags = {
    Name        = "${var.project_name}-${var.environment}-api-access-logs"
    Environment = var.environment
  }
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.http.id
  name        = "$default"
  auto_deploy = true

  depends_on = [
    aws_apigatewayv2_route.identity_proxy,
    aws_apigatewayv2_route.organizations,
    aws_apigatewayv2_route.organizations_proxy
  ]

  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.api_access.arn
    format = jsonencode({
      requestId      = "$context.requestId"
      sourceIp       = "$context.identity.sourceIp"
      requestTime    = "$context.requestTime"
      httpMethod     = "$context.httpMethod"
      routeKey       = "$context.routeKey"
      status         = "$context.status"
      protocol       = "$context.protocol"
      responseLength = "$context.responseLength"
    })
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-default-stage"
    Environment = var.environment
  }
}
