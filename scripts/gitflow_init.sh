#!/usr/bin/env bash

# gitflow_init.sh – cria a branch develop (se não existir) e aplica proteção de branches

set -e

# Ensure we are at repo root
cd "$(git rev-parse --show-toplevel)"

# Create develop branch from main if it doesn't exist
if ! git rev-parse --verify develop >/dev/null 2>&1; then
  echo "Creating develop branch from main..."
  git checkout -b develop main
  git push -u origin develop
else
  echo "Develop branch already exists."
fi

# Run branch protection setup (placeholder script)
if [ -f scripts/setup_branch_protection.sh ]; then
  chmod +x scripts/setup_branch_protection.sh
  ./scripts/setup_branch_protection.sh
else
  echo "No setup_branch_protection.sh found – you may add branch protection rules manually in GitHub settings."
fi

# Switch back to main for safety
git checkout main
