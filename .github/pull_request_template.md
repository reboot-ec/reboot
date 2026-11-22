## General Description

* **Jira Issue Linked:** REBOOT-
* **Affected Scope:** (e.g., `frontend`, `services/auth-service`, `infra/dev`, `docker-compose`, `all`)
* **What was done?:** (Briefly describe what services, endpoints, UI views, or AWS resources were added or modified).

## Type of Change

* [ ] `feat` (New feature)
* [ ] `fix` (Bug correction)
* [ ] `refactor` (Code restructuring without changes in behavior)
* [ ] `infra` (Terraform, AWS, or Docker changes)
* [ ] `chore` (Dependencies, CI/CD, or build config updates)
* [ ] `other` (Specify type from Conventional Commits)

## Quality Verification Checklist

* [ ] **Build & Run:** The affected microservice(s) and/or frontend compile and run without errors locally (or via `docker-compose`).
* [ ] **Manual Testing:** Endpoints, UI flows, or service-to-service communications were tested before opening this PR.
* [ ] **Infrastructure (if applicable):** `terraform fmt` and `terraform plan` were executed and reviewed with no unintended resource destruction.
* [ ] **Security & Clean Repo:** No secrets, `.env` files, compiled binaries (`target/`, `dist/`), or Terraform state files (`*.tfstate`, `*.tfvars`) are included.
