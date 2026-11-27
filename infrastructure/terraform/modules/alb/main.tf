terraform {
  required_version = ">= 1.10.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

resource "aws_security_group" "internal_alb" {
  name        = substr("${var.project_name}-${var.environment}-internal-alb-sg", 0, 255)
  description = "Security group for the internal application load balancer."
  vpc_id      = var.vpc_id
  ingress     = []
  egress      = []

  tags = {
    Name        = "${var.project_name}-${var.environment}-internal-alb-sg"
    Environment = var.environment
  }
}

resource "aws_vpc_security_group_ingress_rule" "from_vpc_link" {
  security_group_id            = aws_security_group.internal_alb.id
  referenced_security_group_id = var.vpc_link_security_group_id
  from_port                    = 80
  to_port                      = 80
  ip_protocol                  = "tcp"
  description                  = "Allow HTTP traffic from the API Gateway VPC Link."
}

resource "aws_vpc_security_group_egress_rule" "to_identity" {
  security_group_id = aws_security_group.internal_alb.id
  cidr_ipv4         = var.vpc_cidr
  from_port         = 8081
  to_port           = 8081
  ip_protocol       = "tcp"
  description       = "Allow HTTP traffic to the Identity service."
}

resource "aws_vpc_security_group_egress_rule" "to_organization" {
  security_group_id = aws_security_group.internal_alb.id
  cidr_ipv4         = var.vpc_cidr
  from_port         = 8082
  to_port           = 8082
  ip_protocol       = "tcp"
  description       = "Allow HTTP traffic to the Organization service."
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

resource "aws_lb_target_group" "identity" {
  name        = substr("${var.project_name}-${var.environment}-identity-tg", 0, 32)
  port        = 8081
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  health_check {
    enabled             = true
    path                = "/actuator/health"
    protocol            = "HTTP"
    interval            = 15
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
    matcher             = "200"
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-identity-tg"
    Environment = var.environment
  }
}

resource "aws_lb_target_group" "organization" {
  name        = substr("${var.project_name}-${var.environment}-organization-tg", 0, 32)
  port        = 8082
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  health_check {
    enabled             = true
    path                = "/actuator/health"
    protocol            = "HTTP"
    interval            = 15
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
    matcher             = "200"
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
      content_type = "application/json"
      message_body = "{\"error\":{\"code\":\"ROUTE_NOT_FOUND\",\"message\":\"No matching service route\"}}"
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
    target_group_arn = aws_lb_target_group.identity.arn
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
    target_group_arn = aws_lb_target_group.organization.arn
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
