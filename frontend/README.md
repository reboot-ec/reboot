# REBOOT Frontend

The frontend uses npm workspaces. Run commands from `frontend/`. The `package-lock.json` file records dependencies for the application and shared packages.

| Path | Responsibility |
| --- | --- |
| `web/` | The `@reboot/web` application uses React 18, TypeScript, Vite, and React Router. It provides authentication and manages organizations and memberships. |
| `packages/api-client/` | `@reboot/api-client` handles HTTP requests, Bearer authentication, the `X-Request-Id` header, and session renewal. It exports `authService` and `organizationService`. |
| `packages/shared-types/` | `@reboot/shared-types` defines TypeScript contracts for users, sessions, organizations, memberships, and API responses. |
| `desktop/` and `mobile/` | Directories reserved for future clients. Electron and Capacitor are the planned options. |

The active workspaces are `web` and `packages/*`. When implementing desktop and mobile clients, add them as workspaces and declare the shared packages as dependencies. Each client can configure its HTTP client and adapt session storage and navigation to its platform. Reuse the CSS tokens from `web/src/styles/global.css` and compatible React components to share the design.

## Atomic Design

Under `web/src/components/`, `atoms/` contains basic controls, `molecules/` combines controls into reusable units, `organisms/` assembles forms and sections, and `templates/` defines screen layouts. `web/src/pages/` connects those pieces to routes and data. See the [web application guide](web/README.md) for the full structure and examples.

## Install, run, and build

Use npm with a Node.js version supported by the installed version of Vite. The project was verified with Node.js 24.14.0 and npm 11.9.0.

```powershell
npm install
if (-not (Test-Path web/.env)) { Copy-Item web/.env.example web/.env }
npm run dev
```

Copy the template during initial setup. The command preserves an existing `web/.env`. To generate a production build, run this command separately.

```powershell
npm run build
```

The build checks TypeScript across the application and shared packages and writes the output to `web/dist/`. You can also run `npm run typecheck` and `npm run lint` from the same directory.

## Environment variables

Set these variables in `web/.env` using [web/.env.example](web/.env.example) as a template.

| Variable | Purpose |
| --- | --- |
| `VITE_API_BASE_URL` | HTTPS base URL for Amazon API Gateway on AWS, including the stage if it is part of the URL. This setting takes precedence for all requests. |
| `VITE_IDENTITY_API_URL` | Local Identity service URL. Defaults to `http://localhost:8081`. |
| `VITE_ORGANIZATION_API_URL` | Local Organization service URL. Defaults to `http://localhost:8082`. |

Leave `VITE_API_BASE_URL` blank to use local services. Vite exposes `VITE_*` variables to the browser, so use them only for public configuration. Git ignores `.env` files and their local variants and allows the `.env.example` template to be tracked.
