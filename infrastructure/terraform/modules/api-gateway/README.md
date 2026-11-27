# API Gateway Module

This module exposes the REBOOT Identity and Organization services through an
Amazon API Gateway HTTP API. It connects to an internal Application Load
Balancer (ALB) listener through a VPC Link. The ALB is provisioned separately
by the `modules/alb` module; both resources use private subnets.

## Architecture and Network Flow

```text
Web / Desktop / Mobile clients
              |
              | HTTPS
              v
Amazon API Gateway HTTP API
              |
              | VPC Link
              v
Internal Application Load Balancer (HTTP, port 80)
in private subnets
        |                         |
        | HTTP, port 8081         | HTTP, port 8082
        v                         v
identity-service              organization-service
```

The ALB listener forwards Identity paths to the Identity target group on port
`8081` and Organization paths to the Organization target group on port `8082`.
Both target groups use IP targets and check `/actuator/health` for health. The
ALB module creates the target groups but does not register service instances;
a compute module must register the targets.

The API Gateway integration forwards requests to the ALB listener using an
HTTP proxy integration. The API Gateway is the public HTTPS entry point; the
ALB remains internal.

## Public Routes

All three API Gateway routes use the `ANY` method, which forwards any HTTP
method matched by the route to the ALB integration.

| Route | API Gateway method | ALB destination |
| --- | --- | --- |
| `/api/v1/identity/{proxy+}` | `ANY` | Identity service target group, HTTP port `8081` |
| `/api/v1/organizations` | `ANY` | Organization service target group, HTTP port `8082` |
| `/api/v1/organizations/{proxy+}` | `ANY` | Organization service target group, HTTP port `8082` |

The ALB listener applies path-based forwarding rules for the Identity and
Organization services. Requests that do not match either rule receive a
`404` JSON response.

## CORS

The HTTP API allows the origins supplied through `allow_origins`. Configure
explicit trusted origins for Web, Desktop, and Mobile clients; do not use
`*` for production origins.

| CORS setting | Configuration |
| --- | --- |
| Allowed origins | `allow_origins` input |
| Allowed methods | `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS` |
| Allowed request headers | `Authorization`, `Content-Type`, `X-Request-Id`, `Idempotency-Key` |
| Exposed response headers | `X-Request-Id` |

The route method is `ANY`; the CORS method list controls which cross-origin
methods browsers are permitted to use. CORS is not an authorization
mechanism—service authentication and authorization must still be enforced.

## Inputs

| Variable | Type | Required | Default / constraints | Description |
| --- | --- | --- | --- | --- |
| `project_name` | `string` | No | `"reboot"` | Project name used in resource names and tags. |
| `environment` | `string` | Yes | None | Deployment environment name, such as `qa` or `prod`. |
| `private_subnet_ids` | `list(string)` | Yes | At least two IDs | Private subnets for the VPC Link. |
| `vpc_link_security_group_id` | `string` | Yes | None | Security group attached to the API Gateway VPC Link. |
| `alb_security_group_id` | `string` | Yes | None | Security group attached to the internal ALB. |
| `alb_listener_arn` | `string` | Yes | None | HTTP listener ARN exported by the ALB module. |
| `allow_origins` | `list(string)` | Yes | At least one origin | Origins allowed by the HTTP API CORS configuration. |

## Outputs

| Output | Description |
| --- | --- |
| `api_endpoint` | Invoke URL for the HTTP API. |
| `api_id` | ID of the HTTP API. |
| `vpc_link_id` | ID of the API Gateway VPC Link. |

The ALB module exports the ALB DNS name, listener ARN, security group ID, and
target group ARNs.

## Validation and Deployment

Run the following commands in PowerShell from the repository root. Terraform
must be installed, AWS credentials must be configured, and the environment's
local `terraform.tfvars` must provide all required values. Keep secrets in an
approved secret store or local untracked configuration; do not commit them.

Check formatting for the Terraform tree:

```powershell
terraform fmt -check -recursive infrastructure\terraform
```

### QA

```powershell
Push-Location infrastructure\terraform\environments\qa
terraform init
terraform fmt -check
terraform validate
terraform plan -var-file="terraform.tfvars"
# Review the plan before applying.
terraform apply -var-file="terraform.tfvars"
Pop-Location
```

### Production

```powershell
Push-Location infrastructure\terraform\environments\prod
terraform init
terraform fmt -check
terraform validate
terraform plan -var-file="terraform.tfvars"
# Obtain explicit approval after reviewing the plan.
terraform apply -var-file="terraform.tfvars"
Pop-Location
```

Do not run `terraform apply` in production without explicit approval. Review
the plan and verify the AWS account, region, and variable values before
deployment.
