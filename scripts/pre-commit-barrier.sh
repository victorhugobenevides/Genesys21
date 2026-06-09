#!/bin/bash

# Simple pre-commit hook to prevent direct commits to main or develop
current_branch=$(git rev-parse --abbrev-ref HEAD)

if [[ "$current_branch" == "main" || "$current_branch" == "develop" ]]; then
  echo "Error: Direct commits to $current_branch are prohibited by project specs."
  echo "Please use a feature/ or bugfix/ branch and submit a Pull Request."
  exit 1
fi

exit 0
