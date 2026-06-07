# Spec: GitFlow / Trunk‑Based Development Process

## Status
- **Date**: 2026-05-30
- **Author**: AI Assistant
- **Status**: Draft / Review

## Context
The Genesys21 mobile app is developed by a small cross‑functional team (frontend, backend, QA, DevOps).  Releases are delivered to the Play Store and internal testers via CI/CD pipelines.  To guarantee **security**, **stability**, and **fast feedback**, we need a well‑defined branching and delivery workflow.

## User Stories
- **As a** developer, **I want to** create a short‑lived feature branch from `develop` following a naming convention, **so that** the CI pipeline validates my changes early.
- **As a** reviewer, **I want to** see a standardized Pull Request (PR) template with required checklists, **so that** I can ensure security and quality before merging.
- **As a** CI system, **I want to** automatically run linting, unit tests, integration tests, and security scans on every PR, **so that** defects are caught before they reach `main`.
- **As a** release manager, **I want to** promote a set of merged changes from `develop` to a release tag via a protected `release/*` branch, **so that** we can perform a controlled, audited deployment.
- **As a** DevOps engineer, **I want to** enforce branch protection rules (no direct pushes, required reviews, status checks), **so that** the mainline stays stable.
- **As a** QA analyst, **I want to** trigger a canary deployment of the new release to a subset of users, **so that** we can monitor for regressions before full rollout.

## Functional Requirements
- **Branching Model**
  - `main` – production‑ready code, protected, only updated via release merges.
  - `develop` – integration branch where feature branches are merged after PR approval.
  - `feature/<ticket‑id>-<short‑desc>` – short‑lived branches created from `develop`.
  - `release/<version>` – temporary branch created from `develop` for release preparation (version bump, changelog, signing).
  - `hotfix/<ticket‑id>-<short‑desc>` – created from `main` for urgent fixes, merged back into both `main` and `develop`.
- **Pull Request Workflow**
  - PR must target `develop` (or `release/*` for release PRs).
  - PR template includes:
    - Checklist for code review, security review, and performance impact.
    - Links to related tickets and specs.
    - Required **CodeQL** and **Dependabot** alerts resolution.
  - Mandatory **2‑approver** rule; at least one approver must be a senior engineer.
  - All status checks (lint, unit, integration, security) must pass before merge.
- **CI/CD Pipeline (GitHub Actions)**
  - On push to any branch: run **lint**, **unit tests**, **static analysis**.
  - On PR open / synchronize: run **integration tests**, **CodeQL**, **OWASP Dependency‑Check**.
  - On merge to `develop`: trigger **build‑artifact** job that produces a night‑ly snapshot for internal QA.
  - On merge to `release/*`: run **signing**, **publish‑to‑internal‑repo**, **create Git tag**.
  - On merge to `main`: trigger **production‑release** workflow (Play Store upload, canary rollout).
- **Security & Stability Measures**
  - Enforce **branch protection**: no force‑pushes, required PR reviews, required status checks.
  - Enable **secret scanning** and **dependency alerts** via GitHub Advanced Security.
  - Run **SAST** (CodeQL) and **DAST** (OWASP ZAP) on PRs.
  - Automated **rollback** job that reverts the last release tag if smoke tests fail.

## Non‑Functional Requirements
- **Performance**: CI pipeline must complete within 15 min for PRs.
- **Reliability**: No direct pushes to `main` or `release/*`; all merges go through PRs with required checks.
- **Auditability**: Every merge must be signed with GPG; release tags must be annotated with release notes.
- **Scalability**: Branch naming convention must allow unlimited concurrent feature branches.
- **Compliance**: Must satisfy OWASP Top 10 and internal security policies.

## Acceptance Criteria
### Scenario 1: Feature Development
1. **Given** a developer starts work on ticket `GEN-123`.
2. **When** they run `git checkout -b feature/GEN-123-add-payment-flow develop`.
3. **Then** the CI pipeline starts lint and unit tests on the new branch.
4. **When** they push commits and open a PR targeting `develop`.
5. **Then** the PR shows the required template and all status checks (lint, unit, integration, CodeQL) must pass.
6. **And** at least two senior engineers approve the PR.
7. **When** the PR is merged, the commit appears in `develop` and a night‑ly build is triggered.

### Scenario 2: Release Preparation
1. **Given** `develop` contains all features for version `1.4.0`.
2. **When** a release engineer creates `git checkout -b release/1.4.0 develop`.
3. **Then** the CI runs a full integration test suite, signs the build, and generates a changelog.
4. **When** the release branch passes all checks, a PR is opened against `main`.
5. **Then** the PR must be approved by a Release Manager and pass all status checks.
6. **When** merged, an annotated tag `v1.4.0` is created and the production‑release workflow deploys to Play Store.

### Scenario 3: Hotfix
1. **Given** a critical bug is discovered in production.
2. **When** a hotfix branch `hotfix/GEN-200‑fix-crash` is created from `main`.
3. **Then** the same PR rules apply, but the branch is merged back into both `main` and `develop`.
4. **When** the hotfix tag is released, the rollback job is ready in case of failure.

## Tech Stack
- **Version Control**: GitHub (Git)
- **Branch Protection**: GitHub Branch Protection Rules
- **CI/CD**: GitHub Actions (lint, unit, integration, CodeQL, Dependabot, OWASP ZAP)
- **Security Scanning**: GitHub Advanced Security, CodeQL, Dependency‑Check
- **Release Management**: GitHub Releases, annotated tags, Fastlane for Play Store
- **Automation**: Bash scripts, Gradle tasks, Fastlane lanes

## References
- [Git Flow Workflow – Vincent Driessen](https://nvie.com/posts/a-successful-git-branching-model/)
- [GitHub Branch Protection Rules](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/about-protected-branches)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- Internal security policy **GEN‑SEC‑01** (stored in the `docs/security` folder).
