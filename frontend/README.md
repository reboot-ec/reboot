# Frontend de REBOOT

El frontend es un monorepo de npm workspaces. Ejecuta los comandos desde `frontend/`. El archivo `package-lock.json` registra las dependencias de la aplicación y los paquetes compartidos.

| Ruta | Responsabilidad |
| --- | --- |
| `web/` | Aplicación `@reboot/web` con React 18, TypeScript, Vite y React Router. Implementa autenticación y gestión de organizaciones y membresías. |
| `packages/api-client/` | `@reboot/api-client` centraliza las solicitudes HTTP, el token Bearer, el `X-Request-Id` y la renovación de sesión. Exporta `authService` y `organizationService`. |
| `packages/shared-types/` | `@reboot/shared-types` define los contratos TypeScript de usuarios, sesiones, organizaciones, membresías y respuestas de API. |
| `desktop/` y `mobile/` | Directorios reservados para clientes futuros. Electron y Capacitor son las opciones previstas. |

Los workspaces activos son `web` y `packages/*`. Al implementar los clientes de escritorio y móvil, incorpóralos como workspaces y declara los paquetes compartidos como dependencias. Cada cliente puede configurar su instancia del cliente HTTP y adaptar el almacenamiento de sesión y la navegación a su plataforma. Para reutilizar el diseño, toma como base los tokens CSS de `web/src/styles/global.css` y los componentes React que sean compatibles con el cliente.

## Atomic Design

En `web/src/components/`, `atoms/` contiene controles básicos, `molecules/` combina controles en unidades reutilizables, `organisms/` reúne formularios y secciones completas y `templates/` organiza la estructura de cada pantalla. `web/src/pages/` conecta esas piezas con las rutas y los datos. Consulta los ejemplos y la estructura completa en la [guía de la aplicación web](web/README.md).

## Instalación, ejecución y compilación

Usa npm con una versión de Node.js compatible con el Vite instalado. El proyecto se verificó con Node.js 24.14.0 y npm 11.9.0.

```powershell
npm install
if (-not (Test-Path web/.env)) { Copy-Item web/.env.example web/.env }
npm run dev
```

Copia la plantilla al configurar el entorno por primera vez y conserva tu `web/.env` si ya existe. Para generar la compilación de producción, ejecuta por separado:

```powershell
npm run build
```

El comando valida TypeScript en la aplicación y los paquetes compartidos y genera `web/dist/`. Desde la misma carpeta también puedes ejecutar `npm run typecheck` y `npm run lint`.

## Variables de entorno

Configura estas variables en `web/.env` a partir de [web/.env.example](web/.env.example).

| Variable | Uso |
| --- | --- |
| `VITE_API_BASE_URL` | URL HTTPS base de Amazon API Gateway en AWS, incluido el stage cuando forme parte de la URL. Tiene prioridad para todas las solicitudes. |
| `VITE_IDENTITY_API_URL` | URL local de Identity. El valor predeterminado es `http://localhost:8081`. |
| `VITE_ORGANIZATION_API_URL` | URL local de Organization. El valor predeterminado es `http://localhost:8082`. |

Deja `VITE_API_BASE_URL` vacía para usar los servicios locales. Las variables `VITE_*` se exponen al navegador y deben contener solo configuración pública. Git ignora los archivos `.env` y sus variantes locales, y permite versionar la plantilla `.env.example`.
