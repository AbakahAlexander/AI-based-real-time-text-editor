#!/bin/bash

# This script helps set up Google Cloud credentials for the text editor

# Check if credentials.json already exists
if [ -f "credentials.json" ]; then
    echo "credentials.json already exists."
    read -p "Do you want to replace it? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Keeping existing credentials file."
        exit 0
    fi
fi

echo "=== Google Cloud Credentials Setup ==="
echo "1. Go to https://console.cloud.google.com/"
echo "2. Create a project or select an existing one"
echo "3. Enable the Speech-to-Text API"
echo "4. Create a service account and download the JSON key"
echo

read -p "Enter the path to your downloaded JSON credentials file: " CREDS_PATH

if [ ! -f "$CREDS_PATH" ]; then
    echo "File not found: $CREDS_PATH"
    exit 1
fi

# Copy the credentials file
cp "$CREDS_PATH" credentials.json
echo "Credentials file has been copied to: $(pwd)/credentials.json"

# Set environment variable for current session
export GOOGLE_APPLICATION_CREDENTIALS="$(pwd)/credentials.json"
echo "Environment variable set: GOOGLE_APPLICATION_CREDENTIALS=$(pwd)/credentials.json"

# Add to .bashrc for persistence
echo "Would you like to add this environment variable to your .bashrc file for persistence?"
read -p "(This will apply to all terminal sessions) (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "export GOOGLE_APPLICATION_CREDENTIALS=\"$(pwd)/credentials.json\"" >> ~/.bashrc
    echo "Added to .bashrc. The variable will be available in new terminal sessions."
    echo "Run 'source ~/.bashrc' to apply in the current session."
fi

echo
echo "Setup complete! You can now run the application with:"
echo "mvn clean compile exec:java -Dexec.mainClass=\"com.example.texteditor.SimpleTextEditor\""
