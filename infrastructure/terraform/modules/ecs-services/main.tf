terraform {
  required_version = ">= 1.10.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

resource "aws_ecr_repository" "identity" {
  name                 = "${var.project_name}-${var.environment}-identity-service"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-identity-service"
    Environment = var.environment
  }
}

resource "aws_ecr_lifecycle_policy" "identity" {
  repository = aws_ecr_repository.identity.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Retain the 15 most recent versioned and environment-tagged images."
        selection = {
          tagStatus     = "tagged"
          tagPrefixList = ["v", "qa", "prod", "latest"]
          countType     = "imageCountMoreThan"
          countNumber   = 15
        }
        action = {
          type = "expire"
        }
      },
      {
        rulePriority = 2
        description  = "Expire untagged images older than 3 days."
        selection = {
          tagStatus   = "untagged"
          countType   = "sinceImagePushed"
          countUnit   = "days"
          countNumber = 3
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}

resource "aws_ecr_repository" "organization" {
  name                 = "${var.project_name}-${var.environment}-organization-service"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-organization-service"
    Environment = var.environment
  }
}

resource "aws_ecr_lifecycle_policy" "organization" {
  repository = aws_ecr_repository.organization.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Retain the 15 most recent versioned and environment-tagged images."
        selection = {
          tagStatus     = "tagged"
          tagPrefixList = ["v", "qa", "prod", "latest"]
          countType     = "imageCountMoreThan"
          countNumber   = 15
        }
        action = {
          type = "expire"
        }
      },
      {
        rulePriority = 2
        description  = "Expire untagged images older than 3 days."
        selection = {
          tagStatus   = "untagged"
          countType   = "sinceImagePushed"
          countUnit   = "days"
          countNumber = 3
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}

resource "aws_ecs_cluster" "main" {
  name = "${var.project_name}-${var.environment}-cluster"

  tags = {
    Name        = "${var.project_name}-${var.environment}-cluster"
    Environment = var.environment
  }
}

resource "aws_cloudwatch_log_group" "identity" {
  name              = "/ecs/${var.project_name}-${var.environment}-identity-service"
  retention_in_days = 7

  tags = {
    Environment = var.environment
  }
}

resource "aws_cloudwatch_log_group" "organization" {
  name              = "/ecs/${var.project_name}-${var.environment}-organization-service"
  retention_in_days = 7

  tags = {
    Environment = var.environment
  }
}

resource "aws_security_group" "tasks" {
  name        = "${var.project_name}-${var.environment}-ecs-tasks-sg"
  description = "Security group for REBOOT ECS Fargate tasks."
  vpc_id      = var.vpc_id
  ingress     = []
  egress      = []

  tags = {
    Name        = "${var.project_name}-${var.environment}-ecs-tasks-sg"
    Environment = var.environment
  }
}

resource "aws_vpc_security_group_ingress_rule" "identity_from_alb" {
  security_group_id            = aws_security_group.tasks.id
  referenced_security_group_id = var.alb_security_group_id
  from_port                    = 8081
  to_port                      = 8081
  ip_protocol                  = "tcp"
  description                  = "Allow Identity traffic from the ALB only."
}

resource "aws_vpc_security_group_ingress_rule" "organization_from_alb" {
  security_group_id            = aws_security_group.tasks.id
  referenced_security_group_id = var.alb_security_group_id
  from_port                    = 8082
  to_port                      = 8082
  ip_protocol                  = "tcp"
  description                  = "Allow Organization traffic from the ALB only."
}

resource "aws_vpc_security_group_egress_rule" "to_postgresql" {
  security_group_id = aws_security_group.tasks.id
  cidr_ipv4         = var.vpc_cidr
  from_port         = 5432
  to_port           = 5432
  ip_protocol       = "tcp"
  description       = "Allow PostgreSQL connections within the VPC."
}

resource "aws_vpc_security_group_egress_rule" "to_ldap" {
  security_group_id = aws_security_group.tasks.id
  cidr_ipv4         = var.vpc_cidr
  from_port         = 389
  to_port           = 389
  ip_protocol       = "tcp"
  description       = "Allow LDAP connections within the VPC."
}

resource "aws_vpc_security_group_egress_rule" "to_ldaps" {
  security_group_id = aws_security_group.tasks.id
  cidr_ipv4         = var.vpc_cidr
  from_port         = 636
  to_port           = 636
  ip_protocol       = "tcp"
  description       = "Allow LDAPS connections within the VPC."
}

resource "aws_vpc_security_group_egress_rule" "to_aws_services" {
  security_group_id = aws_security_group.tasks.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 443
  to_port           = 443
  ip_protocol       = "tcp"
  description       = "Allow HTTPS to ECR, Secrets Manager, CloudWatch, and related AWS endpoints."
}

resource "aws_ecs_task_definition" "identity" {
  family                   = "${var.project_name}-${var.environment}-identity-service"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = var.task_execution_role_arn
  task_role_arn            = var.task_role_arn

  container_definitions = jsonencode([
    {
      name      = "identity-service"
      image     = "${aws_ecr_repository.identity.repository_url}:${var.identity_image_tag}"
      essential = true
      portMappings = [
        {
          containerPort = 8081
          hostPort      = 8081
          protocol      = "tcp"
        }
      ]
      environment = [
        { name = "SERVER_PORT", value = "8081" },
        { name = "SPRING_DATASOURCE_URL", value = var.identity_datasource_url },
        { name = "SPRING_DATASOURCE_USERNAME", value = var.identity_datasource_username },
        { name = "AD_ENABLED", value = tostring(var.ad_enabled) },
        { name = "AD_LDAP_URL", value = var.ad_ldap_url },
        { name = "AD_BASE_DN", value = var.ad_base_dn },
        { name = "AD_BIND_DN", value = var.ad_bind_dn },
        { name = "CORS_ALLOWED_ORIGINS", value = join(",", var.cors_allowed_origins) }
      ]
      secrets = [
        {
          name      = "SPRING_DATASOURCE_PASSWORD"
          valueFrom = "${var.identity_db_secret_arn}:password::"
        },
        {
          name      = "JWT_SECRET"
          valueFrom = var.jwt_secret_arn
        },
        {
          name      = "AD_BIND_PASSWORD"
          valueFrom = var.ad_bind_password_secret_arn
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.identity.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])

  tags = {
    Name        = "${var.project_name}-${var.environment}-identity-service"
    Environment = var.environment
  }
}

resource "aws_ecs_task_definition" "organization" {
  family                   = "${var.project_name}-${var.environment}-organization-service"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = var.task_execution_role_arn
  task_role_arn            = var.task_role_arn

  container_definitions = jsonencode([
    {
      name      = "organization-service"
      image     = "${aws_ecr_repository.organization.repository_url}:${var.organization_image_tag}"
      essential = true
      portMappings = [
        {
          containerPort = 8082
          hostPort      = 8082
          protocol      = "tcp"
        }
      ]
      environment = [
        { name = "SERVER_PORT", value = "8082" },
        { name = "SPRING_DATASOURCE_URL", value = var.organization_datasource_url },
        { name = "SPRING_DATASOURCE_USERNAME", value = var.organization_datasource_username },
        { name = "CORS_ALLOWED_ORIGINS", value = join(",", var.cors_allowed_origins) }
      ]
      secrets = [
        {
          name      = "SPRING_DATASOURCE_PASSWORD"
          valueFrom = "${var.organization_db_secret_arn}:password::"
        },
        {
          name      = "JWT_SECRET"
          valueFrom = var.jwt_secret_arn
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.organization.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])

  tags = {
    Name        = "${var.project_name}-${var.environment}-organization-service"
    Environment = var.environment
  }
}

resource "aws_ecs_service" "identity" {
  name                              = "${var.project_name}-${var.environment}-identity-service"
  cluster                           = aws_ecs_cluster.main.id
  task_definition                   = aws_ecs_task_definition.identity.arn
  launch_type                       = "FARGATE"
  desired_count                     = 1
  health_check_grace_period_seconds = 90

  network_configuration {
    subnets          = var.private_subnet_ids
    security_groups  = [aws_security_group.tasks.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = var.identity_target_group_arn
    container_name   = "identity-service"
    container_port   = 8081
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-identity-service"
    Environment = var.environment
  }
}

resource "aws_ecs_service" "organization" {
  name                              = "${var.project_name}-${var.environment}-organization-service"
  cluster                           = aws_ecs_cluster.main.id
  task_definition                   = aws_ecs_task_definition.organization.arn
  launch_type                       = "FARGATE"
  desired_count                     = 1
  health_check_grace_period_seconds = 90

  network_configuration {
    subnets          = var.private_subnet_ids
    security_groups  = [aws_security_group.tasks.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = var.organization_target_group_arn
    container_name   = "organization-service"
    container_port   = 8082
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-organization-service"
    Environment = var.environment
  }
}
