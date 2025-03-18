#!/bin/bash

# Make the clean and commit script executable
chmod +x clean_and_commit.sh

# Run the script
./clean_and_commit.sh

# After script completes, force push if everything is OK
echo ""
echo "Review the output above."
echo "If everything looks good, run the following command:"
echo "git push -f origin main"
