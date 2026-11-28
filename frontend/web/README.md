# REBOOT Web

`@reboot/web` ofrece inicio de sesión local y por Active Directory, registro de usuarios y rutas protegidas para consultar y gestionar organizaciones y membresías. Usa los contratos de [@reboot/shared-types](../packages/shared-types) y los servicios de [@reboot/api-client](../packages/api-client).

`src/context/api.ts` configura el cliente HTTP con las variables de Vite. `AuthContext` restaura la sesión de la pestaña con `/me`, y `useOrganizations` conecta las operaciones de organizaciones con la sesión activa. Los futuros clientes de [escritorio](../desktop) y [móvil](../mobile) pueden consumir esos mismos paquetes y reutilizar componentes React y tokens CSS compatibles con su plataforma. Consulta la [arquitectura del frontend](../README.md).

## Atomic Design

| Ruta en `src/` | Responsabilidad y ejemplos |
| --- | --- |
| `components/atoms/` | Controles básicos como `Button`, `Input`, `Badge` y `Spinner`. |
| `components/molecules/` | Combinaciones reutilizables como `FormField`, `SelectField`, `SearchBar`, `Pagination` y `ErrorBanner`. |
| `components/organisms/` | Secciones completas como `Navbar`, `AuthCard`, `OrganizationCard`, formularios de organizaciones y `MemberTable`. |
| `components/templates/` | Estructura de las pantallas y acceso a las rutas mediante `AuthLayout`, `DashboardLayout` y `ProtectedRoute`. |
| `pages/` | Pantallas que componen la interfaz y conectan los flujos con la API. |
| `styles/` | Estilos globales y tokens CSS de color, espaciado y bordes. |

| Ruta | Página |
| --- | --- |
| `/login` | `LoginPage` |
| `/register` | `RegisterPage` |
| `/organizations` | `OrganizationsPage` |
| `/organizations/:id` | `OrganizationDetailPage` |

## Entornos

Copia [`.env.example`](.env.example) a `.env` dentro de `frontend/web/`.

| Variable | Configuración |
| --- | --- |
| `VITE_API_BASE_URL` | Para AWS, usa la URL HTTPS de Amazon API Gateway, con el stage cuando corresponda. El cliente añade las rutas `/api/v1/...`. |
| `VITE_IDENTITY_API_URL` | Para pruebas locales, usa `http://localhost:8081` o la URL del servicio Identity que estés ejecutando. |
| `VITE_ORGANIZATION_API_URL` | Para pruebas locales, usa `http://localhost:8082` o la URL del servicio Organization que estés ejecutando. |

Con `VITE_API_BASE_URL` vacía, cada solicitud usa la URL local de su servicio. Para probar los flujos locales, inicia [Identity](../../backend/identity-service/README.md) y [Organization](../../backend/organization-service/README.md). Aplica los cambios de configuración reiniciando Vite en desarrollo o generando una nueva compilación para producción. Usa las variables `VITE_*` solo para valores públicos.

## Comandos

Ejecuta desde `frontend/`:

```powershell
npm install
if (-not (Test-Path web/.env)) { Copy-Item web/.env.example web/.env }
npm run dev
```

La copia de la plantilla se hace una vez. Abre la URL que imprime Vite, normalmente `http://localhost:5173`. Para compilar con la comprobación de TypeScript, ejecuta:

```powershell
npm run build
```

La salida se genera en `frontend/web/dist/`. Cuando sirvas esa carpeta, configura la redirección de rutas del cliente a `index.html` para permitir abrir directamente rutas como `/login`.
