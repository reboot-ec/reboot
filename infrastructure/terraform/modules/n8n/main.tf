data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-kernel-*-x86_64"]
  }

  filter {
    name   = "root-device-type"
    values = ["ebs"]
  }
}

resource "aws_security_group" "n8n" {
  name_prefix = "n8n-"
  description = "n8n web UI, webhooks and SSH"
  vpc_id      = var.vpc_id

  ingress {
    description = "n8n UI and webhooks"
    from_port   = 5678
    to_port     = 5678
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.allowed_ssh_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "n8n"
  }
}

locals {
  # Compose double-quoted dotenv values interpret escapes and dollar signs.
  db_password    = replace(replace(replace(var.n8n_db_password, "\\", "\\\\"), "\"", "\\\""), "$", "$$")
  encryption_key = replace(replace(replace(var.n8n_encryption_key, "\\", "\\\\"), "\"", "\\\""), "$", "$$")
}

resource "aws_instance" "n8n" {
  ami                         = data.aws_ami.al2023.id
  instance_type               = var.instance_type
  subnet_id                   = var.public_subnet_id
  associate_public_ip_address = true
  vpc_security_group_ids      = [aws_security_group.n8n.id]
  key_name                    = var.key_name

  root_block_device {
    volume_type = "gp3"
    volume_size = 20
    encrypted   = true
  }

  metadata_options {
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
  }

  user_data_base64 = base64encode(<<-BOOTSTRAP
    #!/bin/bash
    set -euo pipefail
    umask 077

    dnf install -y docker
    install -d -m 0755 /usr/local/lib/docker/cli-plugins
    curl -fsSL --retry 5 \
      https://github.com/docker/compose/releases/download/v2.32.4/docker-compose-linux-x86_64 \
      -o /usr/local/lib/docker/cli-plugins/docker-compose
    chmod 0755 /usr/local/lib/docker/cli-plugins/docker-compose
    systemctl enable --now docker

    token=$(curl -fsS --retry 5 -X PUT \
      -H 'X-aws-ec2-metadata-token-ttl-seconds: 60' \
      http://169.254.169.254/latest/api/token)
    public_ip=$(curl -fsS --retry 5 -H "X-aws-ec2-metadata-token: $token" \
      http://169.254.169.254/latest/meta-data/public-ipv4)

    install -d -m 0700 /opt/n8n
    cd /opt/n8n

    cat > .env <<'N8N_ENV'
    N8N_DB_PASSWORD="${local.db_password}"
    N8N_ENCRYPTION_KEY="${local.encryption_key}"
    N8N_ENV
    printf 'WEBHOOK_URL=http://%s:5678/\n' "$public_ip" >> .env
    chmod 0600 .env

    cat > docker-compose.yml <<'N8N_COMPOSE'
    services:
      postgres:
        image: postgres:16-alpine
        restart: unless-stopped
        environment:
          POSTGRES_DB: n8n_db
          POSTGRES_USER: n8n
          POSTGRES_PASSWORD: "$${N8N_DB_PASSWORD:?required}"
        volumes:
          - n8n_postgres_data:/var/lib/postgresql/data
        healthcheck:
          test: ["CMD", "pg_isready", "-U", "n8n", "-d", "n8n_db"]
          interval: 5s
          timeout: 5s
          retries: 12

      n8n:
        image: docker.n8n.io/n8nio/n8n:1.76.1
        restart: unless-stopped
        depends_on:
          postgres:
            condition: service_healthy
        ports:
          - "5678:5678"
        environment:
          DB_TYPE: postgresdb
          DB_POSTGRESDB_HOST: postgres
          DB_POSTGRESDB_DATABASE: n8n_db
          DB_POSTGRESDB_USER: n8n
          DB_POSTGRESDB_PASSWORD: "$${N8N_DB_PASSWORD:?required}"
          N8N_ENCRYPTION_KEY: "$${N8N_ENCRYPTION_KEY:?required}"
          N8N_SECURE_COOKIE: "false"
          WEBHOOK_URL: "$${WEBHOOK_URL:?required}"
        volumes:
          - n8n_data:/home/node/.n8n

    volumes:
      n8n_postgres_data:
        name: n8n_postgres_data
      n8n_data:
        name: n8n_data
    N8N_COMPOSE

    docker compose up -d
  BOOTSTRAP
  )

  tags = {
    Name = "n8n"
  }
}
