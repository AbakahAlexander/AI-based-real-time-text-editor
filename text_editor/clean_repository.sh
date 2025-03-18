#!/bin/bash

# This script creates a clean repository without credentials in history

# Go to project root
cd $(git rev-parse --show-toplevel)

# Create a temporary directory for the clean repository
mkdir -p /tmp/clean_repo
cd /tmp/clean_repo

# Initialize a new git repository
git init

# Copy all current files from the original repo, excluding credentials and .git
rsync -av --exclude='.git' --exclude='credentials.json' \
  $(git rev-parse --show-toplevel)/ .

# Initialize the new repository
git add .
git commit -m "Initial commit with clean history"

# Add remote
git remote add origin https://github.com/AbakahAlexander/AI-based-real-time-text-editor.git

echo ""
echo "Clean repository created at /tmp/clean_repo"
echo ""
echo "To replace your GitHub repository with this clean one:"
echo ""
echo "1. First revoke the exposed credentials in Google Cloud Console"
echo "2. Create new credentials and save them locally (but don't commit them)"
echo "3. Run the following commands:"
echo ""
echo "   cd /tmp/clean_repo"
echo "   git push -f origin master:main"
echo ""
echo "4. Then update your local repo:"
echo ""
echo "   cd $(git rev-parse --show-toplevel)"
echo "   git fetch"
echo "   git reset --hard origin/main"
echo ""
echo "Or alternatively, use the unblock URL GitHub provided in the error message"
