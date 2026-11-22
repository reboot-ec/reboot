module "networking" {
  source = "../../modules/networking"

  project_name         = var.project_name
  environment          = var.environment
  vpc_cidr             = var.vpc_cidr
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  availability_zones   = var.availability_zones
}

# IDENTITY SERVICE SECURITY GROUP

resource "aws_security_group" "identity_service" {
  name        = "${var.project_name}-${var.environment}-identity-service-sg"
  description = "Security group for Identity Service."
  vpc_id      = module.networking.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# ORGANIZATION SERVICE SECURITY GROUP

resource "aws_security_group" "organization_service" {
  name        = "${var.project_name}-${var.environment}-organization-service-sg"
  description = "Security group for Organization Service."
  vpc_id      = module.networking.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# IDENTITY DATABASE

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