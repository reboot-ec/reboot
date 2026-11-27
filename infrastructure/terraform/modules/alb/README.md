# Módulo de Application Load Balancer (ALB)

Este módulo crea un Application Load Balancer interno para enrutar solicitudes
HTTP hacia Identity y Organization. Los target groups usan direcciones IP y
pueden registrar targets de ECS/Fargate o instancias EC2.

## Diseño de red y seguridad

- El ALB se crea con `internal = true` y `load_balancer_type = "application"`;
  no recibe tráfico directamente desde Internet.
- Se conecta únicamente a los IDs indicados en `private_subnet_ids`. Se
  requieren al menos dos subredes privadas.
- El Security Group del ALB es distinto del Security Group del VPC Link:
  permite ingreso TCP/80 únicamente desde `vpc_link_security_group_id`.
- La salida del Security Group del ALB permite TCP/8081 y TCP/8082 dentro de
  `vpc_cidr`, para alcanzar los targets de Identity y Organization.
- Los Security Groups de los microservicios deben permitir ingreso en sus
  puertos de aplicación desde el Security Group del ALB. El módulo ALB no
  crea ni modifica los Security Groups de los microservicios.

```text
API Gateway VPC Link
  [SG del VPC Link]
       | TCP/80
       v
ALB interno en subredes privadas
  [SG del ALB: ingreso desde SG VPC Link;
   salida TCP/8081 y TCP/8082 dentro de la VPC]
       | TCP/8081                   | TCP/8082
       v                             v
Identity target group          Organization target group
```

## Target groups, health checks y rutas

Ambos target groups usan `target_type = "ip"`, protocolo `HTTP` y el endpoint
de salud `/actuator/health`.

| Target group | Puerto | Protocolo | Health check | Intervalo | Timeout | Healthy threshold | Unhealthy threshold | Matcher |
| --- | ---: | --- | --- | ---: | ---: | ---: | ---: | --- |
| `identity` | 8081 | HTTP | `/actuator/health` | 15 s | 5 s | 2 | 3 | `200` |
| `organization` | 8082 | HTTP | `/actuator/health` | 15 s | 5 s | 2 | 3 | `200` |

El listener HTTP del puerto 80 aplica las reglas siguientes:

| Prioridad | Patrón de ruta | Acción |
| ---: | --- | --- |
| 10 | `/api/v1/identity*` | Forward al target group `identity` |
| 20 | `/api/v1/organizations*` | Forward al target group `organization` |
| Por defecto | Cualquier otra ruta | Respuesta fija JSON `404` con código `ROUTE_NOT_FOUND` |

El ALB comprueba periódicamente la salud de cada target. Tras tres fallos
consecutivos, un target se marca como no saludable y deja de recibir nuevas
solicitudes del listener; los demás targets saludables del mismo grupo siguen
recibiendo tráfico. Después de dos comprobaciones satisfactorias consecutivas,
el target vuelve a considerarse saludable y puede recibir tráfico de nuevo. Si
ningún target del grupo está saludable, el ALB no puede entregar la solicitud
a un target saludable.

## Variables de entrada

| Variable | Tipo | Requerida | Valor predeterminado / restricciones | Descripción |
| --- | --- | --- | --- | --- |
| `project_name` | `string` | No | `"reboot"` | Nombre del proyecto usado en nombres y etiquetas. |
| `environment` | `string` | Sí | Sin valor predeterminado | Entorno de despliegue, por ejemplo `qa` o `prod`. |
| `vpc_id` | `string` | Sí | — | ID de la VPC donde se crean el ALB y los target groups. |
| `vpc_cidr` | `string` | Sí | — | CIDR de la VPC al que se limita la salida del ALB. |
| `private_subnet_ids` | `list(string)` | Sí | Al menos dos IDs | Subredes privadas donde se conecta el ALB. |
| `vpc_link_security_group_id` | `string` | Sí | — | Security Group de origen autorizado para TCP/80 al ALB. |

## Outputs

| Output | Descripción |
| --- | --- |
| `alb_id` | ID del ALB interno. |
| `alb_arn` | ARN del ALB interno. |
| `alb_dns_name` | Nombre DNS interno del ALB. |
| `alb_security_group_id` | ID del Security Group del ALB. |
| `http_listener_arn` | ARN del listener HTTP del ALB. |
| `identity_target_group_arn` | ARN del target group de Identity. |
| `organization_target_group_arn` | ARN del target group de Organization. |

## Validación y plan

Ejecuta estos comandos desde PowerShell, en la raíz del repositorio. La
validación y el plan se ejecutan en el entorno que se desea revisar; `init`
prepara ese directorio de trabajo si aún no se ha inicializado.

Comprueba el formato del módulo:

```powershell
terraform fmt -check -recursive infrastructure\terraform\modules\alb
```

Valida y genera el plan para QA:

```powershell
Push-Location infrastructure\terraform\environments\qa
terraform init
terraform validate
terraform plan -var-file="terraform.tfvars"
Pop-Location
```

Para producción, reemplaza `qa` por `prod` en la ruta del entorno. Revisa
cuidadosamente el plan y verifica cuenta, región y variables antes de aplicar;
no ejecutes `terraform apply` en producción sin aprobación explícita. Los
archivos locales `terraform.tfvars` no deben contenerse en control de versiones
si incluyen secretos.
