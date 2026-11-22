# REBOOT

## 1. Project Overview

REBOOT is a digital platform designed to promote the repair, reuse, donation, and recycling of electronic devices.

This repository centralizes the development of the platform's applications, microservices, automation workflows, and infrastructure configuration.

The project follows a microservices architecture, allowing each service and application to be developed, tested, and deployed independently.

## 2. Repository Structure

```text
reboot/
├── services/
│   ├── identity-service/
│   └── organization-service/
│
├── clients/
│   ├── web/
│   ├── desktop/
│   └── mobile/
│
├── infrastructure/
│   ├── docker/
│   │   └── nginx/
│   └── terraform/
│
├── automation/
│   └── n8n/
│
├── shared/
├── scripts/
├── .github/
│   └── workflows/
│
├── .gitignore
└── README.md
```

## 3. Main Components

### Microservices

- **Identity Service:** User authentication, sessions, roles, and permissions.
- **Organization Service:** Organization management and membership administration.

### Client Applications

- **Web:** Web application.
- **Desktop:** Desktop application using Electron.
- **Mobile:** Mobile application using Capacitor.

### Infrastructure

- **Docker:** Local development environment and container configuration.
- **Terraform:** Infrastructure as Code for AWS resources.

### Automation

- **n8n:** Workflow automation and external service integrations.

### Supporting Directories

- **shared:** Components and resources shared between applications when necessary.
- **docs:** Technical documentation and architectural decisions.
- **scripts:** Development and maintenance scripts.
- **.github/workflows:** GitHub Actions automation workflows.

## 4. Technology Stack

| Component | Technology |
|---|---|
| Backend | Java 21 + Spring Boot |
| Identity Database | PostgreSQL |
| Organization Database | PostgreSQL |
| Desktop | Electron |
| Mobile | Capacitor |
| Containers | Docker |
| Infrastructure | Terraform |
| Automation | n8n |
| Version Control | Git + GitHub |

## 5. Architectural Principles

REBOOT follows these architectural principles:

- Microservices architecture.
- Database per Microservice.
- Independent development and deployment.
- Separation of concerns.
- Infrastructure as Code.
- Containerized local development.

Each microservice owns and manages its own data. Services communicate through defined APIs and messaging mechanisms rather than accessing each other's databases directly.

## 6. Development Setup

### Prerequisites

Install the following tools according to the component being developed:

- Git
- Visual Studio Code
- Java 21 (for backend development)
- Docker Desktop
- Node.js and npm (for client development)
- Terraform CLI (for infrastructure development)

### Clone the Repository

```bash
git clone https://github.com/reboot-ec/reboot
cd reboot
```

Replace `USERNAME` with the GitHub account or organization that owns the repository.

## 7. Development Guidelines

- Keep each microservice independent.
- Maintain separate databases for each microservice.
- Do not commit credentials, passwords, tokens, or environment files.
- Document important architectural decisions.
- Use Terraform for AWS infrastructure provisioning.
- Follow consistent naming and folder organization.
- Keep client applications independent while consuming shared backend APIs.

## 8. Project Status

This repository establishes the initial monorepo structure for REBOOT.

The applications, microservices, automation workflows, and infrastructure will be implemented progressively according to the project sprint plan.