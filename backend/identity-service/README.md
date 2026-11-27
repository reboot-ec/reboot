# Identity Service

Servicio de identidad de REBOOT para registro, autenticación, perfiles, roles y sesiones. Expone una API REST JSON con JWT, persiste en IdentityDB (PostgreSQL) y puede autenticar contra Windows Active Directory por LDAPS. La arquitectura separa controladores, servicios, DTOs, mappers MapStruct, repositorios y modelos JPA.

## Runtime y health checks

- Puerto local predeterminado: `8081` (`SERVER_PORT`).
- Para coincidir con el puerto `8080` permitido por el security group actual de Identity en Terraform, configura `SERVER_PORT=8080` en la definición de tarea de ECS.
- ALB/ECS pueden consultar `GET /actuator/health`, `/actuator/health/liveness` y `/actuator/health/readiness`. La respuesta no expone detalles ni componentes de health.
- Swagger UI local: <http://localhost:8081/swagger-ui/index.html>. OpenAPI JSON: <http://localhost:8081/v3/api-docs>.

## Variables de entorno de la aplicación

Configura las variables en el task definition de ECS. Usa AWS Secrets Manager o un mecanismo seguro de secretos para contraseñas y claves; no las incluyas en imágenes, build arguments, archivos versionados ni logs.

| Variable | Requerida en AWS | Uso y valor predeterminado |
|---|---:|---|
| `SERVER_PORT` | No | Puerto HTTP. Predeterminado `8081`; usa `8080` para el security group actual de Terraform. |
| `SPRING_DATASOURCE_URL` | Sí | JDBC URL del endpoint RDS de IdentityDB. En desarrollo: `jdbc:postgresql://localhost:5432/identity_db`. |
| `SPRING_DATASOURCE_USERNAME` | Sí | Usuario de IdentityDB. La configuración local usa `postgres`; el Terraform actual declara `identity_admin`. |
| `SPRING_DATASOURCE_PASSWORD` | Sí | Contraseña de IdentityDB; sin contraseña predeterminada. Se corresponde con el secreto `identity_db_password` de Terraform. |
| `JWT_SECRET` | Sí | Clave de firma JWT; no tiene valor predeterminado. Provee una clave fuerte y privada desde Secrets Manager. |
| `JWT_ACCESS_EXPIRATION_MS` | No | Vigencia del access token en milisegundos. Predeterminado `900000`. |
| `JWT_REFRESH_EXPIRATION_MS` | No | Vigencia del refresh token en milisegundos. Predeterminado `604800000`. |
| `CORS_ALLOWED_ORIGINS` | Sí | Orígenes aprobados del frontend separados por comas. El valor predeterminado solo cubre localhost. |
| `AD_ENABLED` | No | Habilita autenticación Active Directory. Predeterminado `false`. |
| `AD_LDAP_URL` | Si AD está habilitado | Endpoint LDAPS, por ejemplo `ldaps://ad.example.internal:636`. Predeterminado local `ldaps://localhost:636`. |
| `AD_BASE_DN` | Si AD está habilitado | DN base de búsqueda; predeterminado `dc=reboot,dc=com`. |
| `AD_BIND_DN` | Si AD está habilitado | DN de la cuenta de servicio LDAP; predeterminado local `cn=admin,dc=reboot,dc=com`. |
| `AD_BIND_PASSWORD` | Si AD está habilitado | Contraseña de la cuenta de bind; sin secreto predeterminado. |
| `AD_CA_CERT_PATH` | Si AD usa CA privada | Ruta dentro del contenedor a un certificado CA montado en runtime. El entrypoint lo importa a Java `cacerts`; no copies ni hornees el certificado en la imagen. |
| `SMTP_HOST` | Para recuperación por email | Host SMTP. Predeterminado `localhost`. |
| `SMTP_PORT` | Para recuperación por email | Puerto SMTP. Predeterminado `587`. |
| `SMTP_USERNAME` | Según proveedor SMTP | Usuario SMTP; predeterminado vacío. |
| `SMTP_PASSWORD` | Según proveedor SMTP | Contraseña SMTP; predeterminada vacía. |
| `SMTP_AUTH` | No | Activa autenticación SMTP. Predeterminado `false`. |
| `SMTP_STARTTLS` | No | Activa STARTTLS. Predeterminado `true`. |
| `SMTP_FROM` | Para recuperación por email | Remitente de los emails; predeterminado `no-reply@reboot.local`. |
| `PASSWORD_RESET_URL` | Para recuperación por email | URL del frontend que recibe el token de recuperación; predeterminada `http://localhost:3000/reset-password`. |
| `PASSWORD_RESET_EXPIRATION_MS` | No | Vigencia del token de recuperación en milisegundos. Predeterminado `1800000` (30 minutos). |

## Variables de Terraform

Los entornos `infrastructure/terraform/environments/qa` y `prod` declaran los siguientes inputs. Terraform los recibe como `TF_VAR_<nombre>`; `environment`, CIDRs, zonas, capacidad/retención de RDS y contraseñas no tienen default en esos entornos.

| Variable de entorno Terraform | Tipo / default | Descripción |
|---|---|---|
| `TF_VAR_project_name` | `string`, default `reboot` | Prefijo de recursos. |
| `TF_VAR_environment` | `string`, requerida | Entorno, por ejemplo `qa` o `prod`. |
| `TF_VAR_aws_region` | `string`, default `us-east-1` | Región AWS. |
| `TF_VAR_vpc_cidr` | `string`, requerida | CIDR de la VPC. |
| `TF_VAR_public_subnet_cidrs` | `list(string)`, requerida | CIDRs de subredes públicas. |
| `TF_VAR_private_subnet_cidrs` | `list(string)`, requerida | CIDRs de subredes privadas. |
| `TF_VAR_availability_zones` | `list(string)`, requerida | Availability Zones para las subredes. |
| `TF_VAR_db_instance_class` | `string`, requerida | Clase de instancia RDS. |
| `TF_VAR_db_allocated_storage` | `number`, requerida | Almacenamiento inicial RDS (GB). |
| `TF_VAR_db_max_allocated_storage` | `number`, requerida | Límite de autoscaling de almacenamiento (GB). |
| `TF_VAR_backup_retention_period` | `number`, requerida | Retención de backups en días. |
| `TF_VAR_multi_az` | `bool`, requerida | Activa/desactiva Multi-AZ. |
| `TF_VAR_deletion_protection` | `bool`, requerida | Protección contra eliminación de RDS. |
| `TF_VAR_skip_final_snapshot` | `bool`, requerida | Omite o conserva el snapshot final al borrar RDS. |
| `TF_VAR_identity_db_password` | `string` sensible, requerida | Contraseña admin de IdentityDB. |
| `TF_VAR_organization_db_password` | `string` sensible, requerida | Contraseña admin de OrganizationDB. |
| `TF_VAR_directory_name` | `string`, default `reboot.local` | FQDN del AWS Managed Microsoft AD definido por el módulo actual. |
| `TF_VAR_directory_password` | `string` sensible, requerida | Contraseña admin del directorio. |

No guardes variables `TF_VAR_*` sensibles en archivos `.tfvars` versionados. El Terraform actual crea networking, grupos de seguridad, RDS y Active Directory; todavía no declara repositorios ECR, clúster/servicios ECS, task definitions ni su conexión a Secrets Manager. Esos recursos deben configurarse antes de un despliegue gestionado íntegramente por Terraform.

## Build y pruebas

Ejecuta estos comandos desde la raíz del repositorio:

```powershell
.\mvnw.cmd -pl backend\identity-service -am clean test
.\mvnw.cmd -pl backend\identity-service -am clean package -DskipTests
```

## Docker

El contexto de build es la raíz del monorepo para reutilizar su Maven Wrapper. El Dockerfile copia únicamente el wrapper, el POM y fuentes de producción; `.dockerignore` excluye `.env`, certificados y otros secretos del contexto. La etapa de ejecución usa JRE Java 17 y el usuario no-root `appuser`.

```powershell
docker build -f backend/identity-service/Dockerfile -t reboot/identity-service:local .
```

Ejemplo local (sustituye los placeholders mediante variables de entorno seguras):

```powershell
docker run --rm -p 8081:8081 `
  -e SERVER_PORT=8081 `
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:5432/identity_db" `
  -e SPRING_DATASOURCE_USERNAME="postgres" `
  -e SPRING_DATASOURCE_PASSWORD="$env:SPRING_DATASOURCE_PASSWORD" `
  -e JWT_SECRET="$env:JWT_SECRET" `
  reboot/identity-service:local
```

Para AD con CA privada, monta el certificado de solo lectura y define `AD_CA_CERT_PATH` con su ruta dentro del contenedor. Por ejemplo, añade `-v "C:\secure\ad-ca.crt:/run/certs/ad-ca.crt:ro"` y `-e AD_CA_CERT_PATH=/run/certs/ad-ca.crt` al comando de ejecución. El entrypoint falla explícitamente si la ruta configurada no existe o no es legible.

## Publicar en Amazon ECR

Requiere Docker y AWS CLI configurados con credenciales IAM de mínimo privilegio para ECR. Los siguientes valores son inputs del flujo de publicación, no variables de la aplicación:

| Variable de entorno | Uso |
|---|---|
| `AWS_REGION` | Región de ECR; debe coincidir con `TF_VAR_aws_region`. |
| `AWS_ACCOUNT_ID` | ID de la cuenta, normalmente obtenido con `aws sts get-caller-identity`. |
| `ECR_REPOSITORY` | Nombre del repositorio ECR, por ejemplo `reboot-identity-service`. |
| `IMAGE_TAG` | Tag inmutable de release, por ejemplo el SHA del commit. |
| `AWS_PROFILE` | Opcional; perfil local de AWS CLI. En ECS usa un task execution role en vez de este perfil. |

Ejecuta desde la raíz del repositorio en PowerShell:

```powershell
$env:AWS_REGION = "us-east-1"
$env:AWS_ACCOUNT_ID = aws sts get-caller-identity --query Account --output text
$env:ECR_REPOSITORY = "reboot-identity-service"
$env:IMAGE_TAG = "latest"
$registry = "$env:AWS_ACCOUNT_ID.dkr.ecr.$env:AWS_REGION.amazonaws.com"

aws ecr create-repository --repository-name $env:ECR_REPOSITORY --region $env:AWS_REGION
aws ecr get-login-password --region $env:AWS_REGION |
  docker login --username AWS --password-stdin $registry
docker build -f backend/identity-service/Dockerfile -t reboot/identity-service:local .
docker tag reboot/identity-service:local "$registry/$env:ECR_REPOSITORY:$env:IMAGE_TAG"
docker push "$registry/$env:ECR_REPOSITORY:$env:IMAGE_TAG"
```
