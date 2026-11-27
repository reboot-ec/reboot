# Organization Service

Servicio REST para administrar organizaciones y sus membresías. Usa Java 17, Spring Boot, PostgreSQL, JWT y Flyway. El puerto predeterminado es `8082`.

## Variables de entorno

| Variable | Obligatoria | Valor predeterminado | Descripción |
|---|---:|---|---|
| `SERVER_PORT` | No | `8082` | Puerto HTTP del servicio |
| `SPRING_DATASOURCE_URL` | No | `jdbc:postgresql://localhost:5432/organization_db` | URL JDBC de PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | No | `postgres` | Usuario de la base de datos |
| `SPRING_DATASOURCE_PASSWORD` | No | vacío | Contraseña de la base de datos |
| `JWT_SECRET` | Sí | — | Secreto HMAC para validar JWT; debe tener al menos 32 bytes |
| `CORS_ALLOWED_ORIGINS` | No | `http://localhost:3000,http://localhost:5173` | Orígenes CORS permitidos, separados por comas |

La aplicación valida el esquema existente con Hibernate y aplica las migraciones de Flyway al iniciar. Asegúrate de que PostgreSQL esté disponible y que la base de datos exista. No guardes secretos en el repositorio ni en imágenes Docker.

## Maven

Ejecuta estos comandos desde la raíz del repositorio:

```powershell
.\mvnw.cmd -pl backend\organization-service clean test
.\mvnw.cmd -pl backend\organization-service clean package
.\mvnw.cmd -pl backend\organization-service spring-boot:run
```

Configura las variables de entorno antes de iniciar la aplicación. Por ejemplo, en PowerShell:

```powershell
$env:JWT_SECRET = "<secreto-de-al-menos-32-bytes>"
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/organization_db"
.\mvnw.cmd -pl backend\organization-service spring-boot:run
```

## Docker

Construye la imagen desde el directorio del servicio (este directorio es el contexto para aplicar su `.dockerignore`):

```powershell
docker build -t reboot-organization-service:local .
```

Iníciala configurando un JWT secret y la conexión a PostgreSQL:

```powershell
docker run --rm -p 8082:8082 `
  -e JWT_SECRET="<secreto-de-al-menos-32-bytes>" `
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:5432/organization_db" `
  -e SPRING_DATASOURCE_USERNAME="postgres" `
  -e SPRING_DATASOURCE_PASSWORD="<contraseña>" `
  reboot-organization-service:local
```

La etapa de ejecución usa Temurin 17 JRE y corre como el usuario no-root `appuser`.

## API y Swagger

- API base: `http://localhost:8082/api/v1/organizations`
- Swagger UI: `http://localhost:8082/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8082/v3/api-docs`

Las rutas protegidas requieren `Authorization: Bearer <JWT>`. El servicio devuelve las respuestas exitosas en el sobre `{ "data": ..., "meta": { "requestId": ... } }`.
