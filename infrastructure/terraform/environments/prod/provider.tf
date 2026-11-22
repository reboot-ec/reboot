provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "REBOOT"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}
