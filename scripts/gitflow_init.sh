#!/usr/bin/env bash

# gitflow_init.sh – bootstrap GitFlow workflow
# Creates develop branch (if missing), sets branch naming conventions,
# and applies branch protection rules via setup_branch_protection.sh

set -euo pipefail

REPO_ROOT=$(git rev-parse --show-toplevel)
cd "$REPO_ROOT"

# 1. Ensure we are on main and up to date
git checkout main
git pull origin main

# 2. Create develop branch if it does not exist
if ! git show-ref --verify --quiet refs/heads/develop; then
  git checkout -b develop
  git push -u origin develop
else
  echo "Branch 'develop' already exists."
fi

# 3. Push naming convention enforcement (no actual enforcement script, just documentation)
cat <<EOF > .gitbranching
# Branch naming conventions
# feature/<ticket-id>-<short-desc>
# release/<version>
# hotfix/<ticket-id>-<short-desc>
EOF

git add .gitbranching
git commit -m "Add branch naming conventions file"

git push

# 4. Run branch protection setup
./scripts/setup_branch_protection.sh

echo "GitFlow initialization complete."
