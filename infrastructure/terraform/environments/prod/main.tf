module "networking" {
  source = "../../modules/networking"

  project_name         = var.project_name
  environment          = var.environment
  vpc_cidr             = var.vpc_cidr
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  availability_zones   = var.availability_zones
}

moved {
  from = module.api_gateway.aws_security_group.vpc_link
  to   = aws_security_group.vpc_link
}

moved {
  from = module.api_gateway.aws_security_group.internal_alb
  to   = module.alb.aws_security_group.internal_alb
}

moved {
  from = module.api_gateway.aws_vpc_security_group_ingress_rule.alb_from_vpc_link
  to   = module.alb.aws_vpc_security_group_ingress_rule.from_vpc_link
}

moved {
  from = module.api_gateway.aws_lb.internal
  to   = module.alb.aws_lb.internal
}

moved {
  from = module.api_gateway.aws_lb_target_group.identity_tg
  to   = module.alb.aws_lb_target_group.identity
}

moved {
  from = module.api_gateway.aws_lb_target_group.organization_tg
  to   = module.alb.aws_lb_target_group.organization
}

moved {
  from = module.api_gateway.aws_lb_listener.http
  to   = module.alb.aws_lb_listener.http
}

moved {
  from = module.api_gateway.aws_lb_listener_rule.identity
  to   = module.alb.aws_lb_listener_rule.identity
}

moved {
  from = module.api_gateway.aws_lb_listener_rule.organizations
  to   = module.alb.aws_lb_listener_rule.organizations
}

resource "aws_security_group" "vpc_link" {
  name        = "${var.project_name}-${var.environment}-vpc-link-sg"
  description = "Security group for the API Gateway VPC Link."
  vpc_id      = module.networking.vpc_id
  ingress     = []
  egress      = []

  tags = {
    Name        = "${var.project_name}-${var.environment}-vpc-link-sg"
    Environment = var.environment
  }
}

module "alb" {
  source = "../../modules/alb"

  project_name               = var.project_name
  environment                = var.environment
  vpc_id                     = module.networking.vpc_id
  vpc_cidr                   = module.networking.vpc_cidr
  private_subnet_ids         = module.networking.private_subnet_ids
  vpc_link_security_group_id = aws_security_group.vpc_link.id
}

moved {
  from = aws_vpc_security_group_egress_rule.vpc_link_to_alb
  to   = module.api_gateway.aws_vpc_security_group_egress_rule.vpc_link_to_alb
}

module "api_gateway" {
  source = "../../modules/api-gateway"

  project_name               = var.project_name
  environment                = var.environment
  private_subnet_ids         = module.networking.private_subnet_ids
  vpc_link_security_group_id = aws_security_group.vpc_link.id
  alb_security_group_id      = module.alb.alb_security_group_id
  alb_listener_arn           = module.alb.http_listener_arn
  allow_origins              = var.cors_allowed_origins
}

# LOAD BALANCER SECURITY GROUP

resource "aws_security_group" "load_balancer" {
  name        = "${var.project_name}-${var.environment}-load-balancer-sg"
  description = "Security group for the REBOOT Load Balancer."
  vpc_id      = module.networking.vpc_id

  ingress {
    description = "Allow HTTP traffic from the Internet."
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Allow HTTPS traffic from the Internet."
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "Allow outbound traffic."
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-load-balancer-sg"
    Environment = var.environment
  }
}


# IDENTITY SERVICE SECURITY GROUP

resource "aws_security_group" "identity_service" {
  name        = "${var.project_name}-${var.environment}-identity-service-sg"
  description = "Security group for Identity Service."
  vpc_id      = module.networking.vpc_id

  ingress {
    description     = "Allow traffic from the Load Balancer."
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.load_balancer.id]
  }

  egress {
    description = "Allow outbound traffic."
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-identity-service-sg"
    Environment = var.environment
  }
}


# ORGANIZATION SERVICE SECURITY GROUP


# ORGANIZATION SERVICE SECURITY GROUP

resource "aws_security_group" "organization_service" {
  name        = "${var.project_name}-${var.environment}-organization-service-sg"
  description = "Security group for Organization Service."
  vpc_id      = module.networking.vpc_id

  ingress {
    description     = "Allow traffic from the Load Balancer."
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.load_balancer.id]
  }

  egress {
    description = "Allow outbound traffic."
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-organization-service-sg"
    Environment = var.environment
  }
}

# IDENTITY DATABASE
# ACTIVE DIRECTORY

module "active_directory" {
  source = "../../modules/active-directory"

  project_name       = var.project_name
  environment        = var.environment
  vpc_id             = module.networking.vpc_id
  subnet_ids         = module.networking.private_subnet_ids
  vpc_cidr           = var.vpc_cidr
  directory_name     = var.directory_name
  directory_password = var.directory_password
}


module "identity_database" {
  source = "../../modules/database"

  identifier      = "${var.project_name}-identity-${var.environment}"
  database_name   = "identity_db"
  master_username = "identity_admin"
  master_password = var.identity_db_password

  instance_class          = var.db_instance_class
  allocated_storage       = var.db_allocated_storage
  max_allocated_storage   = var.db_max_allocated_storage
  backup_retention_period = var.backup_retention_period
  multi_az                = var.multi_az
  deletion_protection     = var.deletion_protection
  skip_final_snapshot     = var.skip_final_snapshot

  environment = var.environment

  vpc_id     = module.networking.vpc_id
  subnet_ids = module.networking.private_subnet_ids

  allowed_security_group_ids = [
    aws_security_group.identity_service.id
  ]
}

# ORGANIZATION DATABASE

module "organization_database" {
  source = "../../modules/database"

  identifier      = "${var.project_name}-organization-${var.environment}"
  database_name   = "organization_db"
  master_username = "organization_admin"
  master_password = var.organization_db_password

  instance_class          = var.db_instance_class
  allocated_storage       = var.db_allocated_storage
  max_allocated_storage   = var.db_max_allocated_storage
  backup_retention_period = var.backup_retention_period
  multi_az                = var.multi_az
  deletion_protection     = var.deletion_protection
  skip_final_snapshot     = var.skip_final_snapshot

  environment = var.environment

  vpc_id     = module.networking.vpc_id
  subnet_ids = module.networking.private_subnet_ids

  allowed_security_group_ids = [
    aws_security_group.organization_service.id
  ]
}