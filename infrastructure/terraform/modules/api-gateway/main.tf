terraform {
  required_version = ">= 1.10.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

resource "aws_security_group" "vpc_link" {
  name        = "${var.project_name}-${var.environment}-vpc-link-sg"
  description = "Security group for the API Gateway VPC Link."
  vpc_id      = var.vpc_id

  tags = {
    Name        = "${var.project_name}-${var.environment}-vpc-link-sg"
    Environment = var.environment
  }
}

resource "aws_security_group" "internal_alb" {
  name        = "${var.project_name}-${var.environment}-internal-alb-sg"
  description = "Security group for the internal application load balancer."
  vpc_id      = var.vpc_id

  tags = {
    Name        = "${var.project_name}-${var.environment}-internal-alb-sg"
    Environment = var.environment
  }
}

resource "aws_vpc_security_group_egress_rule" "vpc_link_to_alb" {
  security_group_id            = aws_security_group.vpc_link.id
  referenced_security_group_id = aws_security_group.internal_alb.id
  from_port                    = 80
  to_port                      = 80
  ip_protocol                  = "tcp"
  description                  = "Allow HTTP traffic to the internal ALB."
}

resource "aws_vpc_security_group_ingress_rule" "alb_from_vpc_link" {
  security_group_id            = aws_security_group.internal_alb.id
  referenced_security_group_id = aws_security_group.vpc_link.id
  from_port                    = 80
  to_port                      = 80
  ip_protocol                  = "tcp"
  description                  = "Allow HTTP traffic from the API Gateway VPC Link."
}

resource "aws_vpc_security_group_egress_rule" "alb_to_targets" {
  security_group_id = aws_security_group.internal_alb.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
  description       = "Allow outbound traffic to application targets."
}

resource "aws_lb" "internal" {
  name               = substr("${var.project_name}-${var.environment}-internal-alb", 0, 32)
  internal           = true
  load_balancer_type = "application"
  subnets            = var.private_subnet_ids
  security_groups    = [aws_security_group.internal_alb.id]

  tags = {
    Name        = "${var.project_name}-${var.environment}-internal-alb"
    Environment = var.environment
  }
}

resource "aws_lb_target_group" "identity_tg" {
  name        = substr("${var.project_name}-${var.environment}-identity-tg", 0, 32)
  port        = 8081
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "instance"

  health_check {
    enabled  = true
    path     = "/actuator/health"
    protocol = "HTTP"
    matcher  = "200"
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-identity-tg"
    Environment = var.environment
  }
}

resource "aws_lb_target_group" "organization_tg" {
  name        = substr("${var.project_name}-${var.environment}-organization-tg", 0, 32)
  port        = 8082
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "instance"

  health_check {
    enabled  = true
    path     = "/actuator/health"
    protocol = "HTTP"
    matcher  = "200"
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-organization-tg"
    Environment = var.environment
  }
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.internal.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "fixed-response"

    fixed_response {
      content_type = "text/plain"
      message_body = "Not Found"
      status_code  = "404"
    }
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-internal-alb-http"
    Environment = var.environment
  }
}

resource "aws_lb_listener_rule" "identity" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 10

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.identity_tg.arn
  }

  condition {
    path_pattern {
      values = ["/api/v1/identity*"]
    }
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-identity-route"
    Environment = var.environment
  }
}

resource "aws_lb_listener_rule" "organizations" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 20

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.organization_tg.arn
  }

  condition {
    path_pattern {
      values = ["/api/v1/organizations*"]
    }
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-organizations-route"
    Environment = var.environment
  }
}

resource "aws_apigatewayv2_vpc_link" "main" {
  name               = "${var.project_name}-${var.environment}-vpc-link"
  subnet_ids         = var.private_subnet_ids
  security_group_ids = [aws_security_group.vpc_link.id]

  depends_on = [
    aws_vpc_security_group_egress_rule.vpc_link_to_alb,
    aws_vpc_security_group_ingress_rule.alb_from_vpc_link
  ]

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
  integration_uri        = aws_lb_listener.http.arn
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
