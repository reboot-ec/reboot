# REBOOT Web

`@reboot/web` provides local and Active Directory sign-in, user registration, and protected routes for viewing and managing organizations and memberships. It uses the contracts from [@reboot/shared-types](../packages/shared-types) and services from [@reboot/api-client](../packages/api-client).

`src/context/api.ts` configures the HTTP client from Vite's environment variables. `AuthContext` restores the tab session using `/me`, and `useOrganizations` connects organization operations to the active session. Future [desktop](../desktop) and [mobile](../mobile) clients can use the same packages and reuse compatible React components and CSS tokens. See the [frontend architecture](../README.md).

## Atomic Design

| Path within `src/` | Responsibility and examples |
| --- | --- |
| `components/atoms/` | Basic controls such as `Button`, `Input`, `Badge`, and `Spinner`. |
| `components/molecules/` | Reusable combinations such as `FormField`, `SelectField`, `SearchBar`, `Pagination`, and `ErrorBanner`. |
| `components/organisms/` | Complete sections such as `Navbar`, `AuthCard`, `OrganizationCard`, organization forms, and `MemberTable`. |
| `components/templates/` | Screen layouts and route access through `AuthLayout`, `DashboardLayout`, and `ProtectedRoute`. |
| `pages/` | Screens that assemble the interface and connect workflows to the API. |
| `styles/` | Global styles and CSS tokens for color, spacing, and borders. |

| Route | Page |
| --- | --- |
| `/login` | `LoginPage` |
| `/register` | `RegisterPage` |
| `/organizations` | `OrganizationsPage` |
| `/organizations/new` | `CreateOrganizationPage` |
| `/organizations/mine` | `MyOrganizationsPage` |
| `/organizations/:id` | `OrganizationDetailPage` |

Registration saves the selected profile intent for that user in the browser. Citizens start in the directory. Representatives start in My Organizations and are redirected to the registration form if they have no active memberships. My Organizations reads the authenticated `GET /api/v1/organizations/mine` endpoint.

## Environments

Copy [`.env.example`](.env.example) to `.env` in `frontend/web/`.

| Variable | Configuration |
| --- | --- |
| `VITE_API_BASE_URL` | On AWS, use the Amazon API Gateway HTTPS URL, including the stage where applicable. The client appends the `/api/v1/...` paths. |
| `VITE_IDENTITY_API_URL` | For local testing, use `http://localhost:8081` or the URL of your running Identity service. |
| `VITE_ORGANIZATION_API_URL` | For local testing, use `http://localhost:8082` or the URL of your running Organization service. |

When `VITE_API_BASE_URL` is blank, each request uses its service's local URL. To test locally, start [Identity](../../backend/identity-service/README.md) and [Organization](../../backend/organization-service/README.md). Restart Vite in development or rebuild for production to apply configuration changes. Use `VITE_*` variables only for public values.

## Commands

Run these commands from `frontend/`.

```powershell
npm install
if (-not (Test-Path web/.env)) { Copy-Item web/.env.example web/.env }
npm run dev
```

The template is copied during initial setup. Open the URL printed by Vite, typically `http://localhost:5173`. Run the following command to check TypeScript and build the application.

```powershell
npm run build
```

The build output is written to `frontend/web/dist/`. When hosting that directory, configure a fallback to `index.html` so users can open client routes such as `/login` directly.
