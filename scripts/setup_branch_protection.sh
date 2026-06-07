#!/usr/bin/env bash
# ------------------------------------------------------------
# setup_branch_protection.sh – apply branch protection to main, develop, release/*
# Requires: gh (GitHub CLI) authenticated with repo write permissions.
# ------------------------------------------------------------
set -euo pipefail
REPO="victorhugobenevides/Genesys21"
# Protect main
gh api -X PUT "/repos/$REPO/branches/main/protection" -f required_status_checks='{"strict":true,"contexts":["ci.yml"]}' -f enforce_admins=true -f required_pull_request_reviews='{"dismiss_stale_reviews":true,"required_approving_review_count":2}' -f restrictions='null'
# Protect develop
gh api -X PUT "/repos/$REPO/branches/develop/protection" -f required_status_checks='{"strict":true,"contexts":["ci.yml"]}' -f enforce_admins=true -f required_pull_request_reviews='{"dismiss_stale_reviews":true,"required_approving_review_count":2}' -f restrictions='null'
# Protect all release/* branches (wildcard)
gh api -X PUT "/repos/$REPO/branches/release%2A/protection" -f required_status_checks='{"strict":true,"contexts":["ci.yml"]}' -f enforce_admins=true -f required_pull_request_reviews='{"dismiss_stale_reviews":true,"required_approving_review_count":2}' -f restrictions='null'

echo "Branch protection rules applied."
