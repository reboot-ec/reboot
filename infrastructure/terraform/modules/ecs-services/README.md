# ECS Services Module

This module deploys the Identity and Organization microservices as separate
Amazon ECS services on AWS Fargate. It also creates their ECR repositories,
CloudWatch log groups, task security group, task definitions, and ECS cluster.

## Architecture and network isolation

Each Fargate task uses `awsvpc` networking and runs in the supplied private
subnets without a public IP. The task security group accepts ports 8081 and
8082 only from the supplied ALB security group. Task egress is limited to
PostgreSQL (5432), LDAP/LDAPS (389/636) within the VPC, and HTTPS (443) for
AWS services such as ECR, Secrets Manager, and CloudWatch. Private subnet
routing must provide NAT or the required VPC endpoints for those AWS services.

The ALB target groups register the tasks by IP. Their health checks use
`/actuator/health`; ECS allows a 90-second health-check grace period. Container
logs go to separate CloudWatch log groups with seven-day retention.

The task execution role is supplied by the caller (for example, AWS Academy
`LabRole`); this module does not create IAM roles. It must permit ECR image
pulls, CloudWatch Logs writes, and `secretsmanager:GetSecretValue` for the
referenced secrets. Secret values are injected into containers at task start,
not stored in image layers. The database secret JSON documents must contain a
`password` key. JWT and AD bind password secrets are read as plain secret
strings.

## Docker image versioning

Build each service once and tag that image with three tags:

1. A release identifier that is never reused, such as `v1.0.0` or a Git SHA.
2. The moving environment alias (`qa` or `prod`).
3. The moving `latest` alias.

The ECR repositories are configured as `MUTABLE` so environment aliases and
`latest` can move. ECR does not enforce immutability for release tags in this
configuration; treat `vX.Y.Z` and Git SHA tags as immutable by convention and
never push a different image under an existing release tag.

`identity_image_tag` and `organization_image_tag` are independent Terraform
inputs. QA defaults both to `qa`; production defaults both to `prod`. To pin a
release or roll back only one service, set only that service's variable to the
desired release tag and apply Terraform. Terraform registers a task definition
revision with that image tag. Re-pushing an environment alias without changing
the task definition can instead be picked up with
`aws ecs update-service --force-new-deployment`.

The ECR lifecycle policies retain up to the 15 newest images matching the
`v`, `qa`, `prod`, or `latest` tag prefixes and expire untagged images after
three days.

## PowerShell deployment

Run these commands from the repository root. Configure the environment's
`terraform.tfvars` first, including its task execution role and secrets.
Terraform `apply` provisions the environment's full infrastructure, including
the ECR repositories and ECS services. For production, review the plan and
obtain the required approval before applying.

### 1. Create ECR repositories with Terraform

For QA:

```powershell
terraform -chdir="infrastructure\terraform\environments\qa" init
terraform -chdir="infrastructure\terraform\environments\qa" plan -out=qa.tfplan
terraform -chdir="infrastructure\terraform\environments\qa" apply qa.tfplan
```

For production:

```powershell
terraform -chdir="infrastructure\terraform\environments\prod" init
terraform -chdir="infrastructure\terraform\environments\prod" plan -out=prod.tfplan
terraform -chdir="infrastructure\terraform\environments\prod" apply prod.tfplan
```

### 2. Log in to Amazon ECR

Choose the environment to publish to. Set `$environment` to `qa` or `prod`;
the example below is for QA.

```powershell
$environment = "qa"
$terraformDirectory = "infrastructure\terraform\environments\$environment"
$awsRegion = "us-east-1"
$accountId = aws sts get-caller-identity --query Account --output text
$registry = "$($accountId).dkr.ecr.$($awsRegion).amazonaws.com"

aws ecr get-login-password --region $awsRegion |
    docker login --username AWS --password-stdin $registry

$identityRepository = terraform -chdir="$terraformDirectory" output -raw identity_ecr_repository_url
$organizationRepository = terraform -chdir="$terraformDirectory" output -raw organization_ecr_repository_url
```

### 3. Build, tag, and push both microservices

Use a unique version for each release. To use a commit identifier instead,
replace the version assignment with:
`$releaseTag = (git rev-parse --short=12 HEAD).Trim()`.

```powershell
$releaseTag = "v1.0.0"
$environmentTag = $environment

docker build -f backend\identity-service\Dockerfile `
    -t "${identityRepository}:${releaseTag}" `
    -t "${identityRepository}:${environmentTag}" `
    -t "${identityRepository}:latest" .

docker build -f backend\organization-service\Dockerfile `
    -t "${organizationRepository}:${releaseTag}" `
    -t "${organizationRepository}:${environmentTag}" `
    -t "${organizationRepository}:latest" .

docker push "${identityRepository}:${releaseTag}"
docker push "${identityRepository}:${environmentTag}"
docker push "${identityRepository}:latest"

docker push "${organizationRepository}:${releaseTag}"
docker push "${organizationRepository}:${environmentTag}"
docker push "${organizationRepository}:latest"
```

For production, set `$environment = "prod"` before retrieving the repository
URLs and building. The environment alias will then be `prod`.

Both Dockerfiles expect the monorepo root as the build context. Build them
from the repository root using the commands above; do not use a service
subdirectory as the context.

### 4. Deploy and verify ECS, ALB, and API Gateway

With the environment alias configured in the task definition (`qa` or `prod`),
force ECS to launch fresh tasks after pushing the aliases:

```powershell
$projectName = "reboot"
$clusterName = "${projectName}-${environment}-cluster"
$identityServiceName = "${projectName}-${environment}-identity-service"
$organizationServiceName = "${projectName}-${environment}-organization-service"

aws ecs update-service --cluster $clusterName --service $identityServiceName `
    --force-new-deployment --region $awsRegion

aws ecs update-service --cluster $clusterName --service $organizationServiceName `
    --force-new-deployment --region $awsRegion
```

Check that both ALB target groups report healthy targets. These Terraform
outputs are available from the selected environment:

```powershell
$identityTargetGroupArn = terraform -chdir="$terraformDirectory" output -raw identity_target_group_arn
$organizationTargetGroupArn = terraform -chdir="$terraformDirectory" output -raw organization_target_group_arn

aws elbv2 describe-target-health --target-group-arn $identityTargetGroupArn `
    --query "TargetHealthDescriptions[].{Target:Target.Id,State:TargetHealth.State,Reason:TargetHealth.Reason}" `
    --output table --region $awsRegion

aws elbv2 describe-target-health --target-group-arn $organizationTargetGroupArn `
    --query "TargetHealthDescriptions[].{Target:Target.Id,State:TargetHealth.State,Reason:TargetHealth.Reason}" `
    --output table --region $awsRegion
```

For an authenticated API Gateway smoke check, set `REBOOT_ACCESS_TOKEN` to a
valid access token in the PowerShell session, then call the Organization list
route:

```powershell
$apiEndpoint = terraform -chdir="$terraformDirectory" output -raw api_gateway_invoke_url
$headers = @{ Authorization = "Bearer $env:REBOOT_ACCESS_TOKEN" }
Invoke-RestMethod -Method Get -Uri "$apiEndpoint/api/v1/organizations" -Headers $headers
```

To roll back one service to an earlier release, update only its
`identity_image_tag` or `organization_image_tag` value in the environment's
`terraform.tfvars`, run `terraform plan`, and apply the reviewed plan. ECS will
then deploy a new task definition revision using that service's pinned image.

## Module variables

| Variable | Type | Required/default | Purpose |
|---|---|---|---|
| `project_name` | `string` | Optional; `reboot` | Prefix for resource names and tags. |
| `environment` | `string` | Required | Environment name, such as `qa` or `prod`. |
| `aws_region` | `string` | Optional; `us-east-1` | Region used by the CloudWatch Logs driver. |
| `vpc_id` | `string` | Required | VPC hosting the tasks. |
| `vpc_cidr` | `string` | Required | Limits database and directory egress to the VPC. |
| `private_subnet_ids` | `list(string)` | Required | Private task subnets; at least one is required. |
| `alb_security_group_id` | `string` | Required | Only source allowed to reach task ports. |
| `identity_target_group_arn` | `string` | Required | Identity ALB target group. |
| `organization_target_group_arn` | `string` | Required | Organization ALB target group. |
| `task_execution_role_arn` | `string` | Required | Existing execution role; must pull images, write logs, and read secrets. |
| `task_role_arn` | `string` | Required | Existing role assumed by the application tasks. |
| `identity_image_tag` | `string` | Required | Identity image tag; validated as a Docker tag. |
| `organization_image_tag` | `string` | Required | Organization image tag; validated as a Docker tag. |
| `identity_datasource_url` | `string` | Required | Identity JDBC URL. |
| `identity_datasource_username` | `string` | Required | Identity database username. |
| `organization_datasource_url` | `string` | Required | Organization JDBC URL. |
| `organization_datasource_username` | `string` | Required | Organization database username. |
| `identity_db_secret_arn` | `string` | Required | Secrets Manager ARN; JSON includes `password`. |
| `organization_db_secret_arn` | `string` | Required | Secrets Manager ARN; JSON includes `password`. |
| `jwt_secret_arn` | `string` | Required | Secrets Manager ARN containing the JWT secret string. |
| `ad_bind_password_secret_arn` | `string` | Required | Secrets Manager ARN containing the AD bind password. |
| `ad_enabled` | `bool` | Required | Enables or disables Active Directory authentication. |
| `ad_ldap_url` | `string` | Required | Active Directory LDAP/LDAPS URL. |
| `ad_base_dn` | `string` | Required | Active Directory search base DN. |
| `ad_bind_dn` | `string` | Required | Active Directory bind account DN. |
| `cors_allowed_origins` | `list(string)` | Required | Non-empty list serialized as a comma-separated environment variable. |

## Module outputs

| Output | Description |
|---|---|
| `ecs_cluster_name` | ECS cluster name. |
| `identity_ecr_repository_url` | Identity ECR repository URL. |
| `organization_ecr_repository_url` | Organization ECR repository URL. |
| `ecs_tasks_security_group_id` | Security group ID attached to the Fargate tasks. |
| `identity_service_name` | Identity ECS service name. |
| `organization_service_name` | Organization ECS service name. |
