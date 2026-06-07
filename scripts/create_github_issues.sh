#!/usr/bin/env bash
# ------------------------------------------------------------
# Script: create_github_issues.sh
# Purpose: Parse *.md spec files under .specify/ and create a GitHub issue
#          for each user story (lines that start with "- **As a**").
# Requirements:
#   - GitHub CLI (`gh`) installed and authenticated (GH_TOKEN env var or `gh auth login`).
#   - `jq` for JSON handling (optional, not used here).
# ------------------------------------------------------------

set -euo pipefail

# Ensure the label exists (ignore error if already present)
if ! gh label list | grep -q "spec-imported"; then
  gh label create spec-imported --color 0E8A16 --description "Imported from spec"
fi

# Directory containing specs (relative to repo root)
SPEC_DIR=".specify/specs"

# Function to create an issue from title and body, then add label
create_issue() {
  local title="$1"
  local body="$2"
  echo "Creating issue: $title"
  # Create issue and capture its number (gh prints URL, we extract the number)
  local url=$(gh issue create --title "$title" --body "$body" --label "spec-imported")
  # Extract the issue number from the URL (last path component)
  local number=$(basename "$url")
  echo "Created issue #$number"
}

# Iterate over all markdown spec files
find "$SPEC_DIR" -name "*.md" | while read -r file; do
  echo "Processing $file"
  # Extract lines that start with "- **As a**"
  awk '/^- \*\*As a\*\*/ {print}' "$file" | while IFS= read -r line; do
    # Remove leading dash and spaces
    story=$(echo "$line" | sed -E 's/^[- ]+//')
    # Build issue body with reference to the spec file and the story text
    body=$(printf "User story extracted from %s\n\n---\n\n%s" "$file" "$story")
    # Title: first sentence before the first comma (or the whole story if no comma)
    title=$(echo "$story" | cut -d',' -f1)
    create_issue "$title" "$body"
  done
done

echo "All user stories processed."
