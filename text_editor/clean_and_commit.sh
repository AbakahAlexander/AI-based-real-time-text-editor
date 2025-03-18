#!/bin/bash

# Step 1: Install git-filter-repo if needed
echo "Checking for git-filter-repo..."
if ! command -v git-filter-repo &> /dev/null; then
    echo "Installing git-filter-repo..."
    pip3 install git-filter-repo
fi

# Step 2: Go to repository root
cd $(git rev-parse --show-toplevel)

# Step 3: Make sure we have a backup
echo "Creating backup branch..."
git checkout -b backup_branch_$(date +%Y%m%d%H%M%S)
git checkout main

# Step 4: Clean the history to remove credentials.json
echo "Removing credentials.json from Git history..."
git filter-repo --path-glob "*credentials.json" --invert-paths

# Step 5: Add all current files
echo "Adding current files..."
git add .

# Step 6: Create commit
echo "Creating commit..."
git commit -m "Clean repository and add text editor files"

echo ""
echo "History has been cleaned and files committed."
echo "You can now push with: git push -f origin main"
echo ""
echo "IMPORTANT: If you previously cloned this repository elsewhere,"
echo "you will need to re-clone it after this operation."
