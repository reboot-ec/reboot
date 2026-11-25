resource "aws_security_group" "active_directory" {
  name        = "${var.project_name}-${var.environment}-ad-sg"
  description = "Security group for Simple AD directory service."
  vpc_id      = var.vpc_id

  ingress {
    description = "Allow LDAP TCP traffic from VPC."
    from_port   = 389
    to_port     = 389
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  ingress {
    description = "Allow LDAP UDP traffic from VPC."
    from_port   = 389
    to_port     = 389
    protocol    = "udp"
    cidr_blocks = [var.vpc_cidr]
  }

  ingress {
    description = "Allow LDAPS TCP traffic from VPC."
    from_port   = 636
    to_port     = 636
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  egress {
    description = "Allow all outbound traffic."
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-ad-sg"
    Environment = var.environment
  }
}

resource "aws_directory_service_directory" "main" {
  name     = var.directory_name
  password = var.directory_password
  edition  = "Standard"
  type     = "SimpleAD"
  size     = "Small"

  vpc_settings {
    vpc_id     = var.vpc_id
    subnet_ids = slice(var.subnet_ids, 0, 2)
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-directory"
    Environment = var.environment
  }
}
