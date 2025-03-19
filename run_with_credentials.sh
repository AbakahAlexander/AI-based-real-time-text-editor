#!/bin/bash

# This script runs the text editor with the credentials file configured

# Check if credentials.json exists
if [ ! -f "credentials.json" ]; then
    echo "Error: credentials.json not found!"
    echo "Please run ./setup_credentials.sh first to set up your Google Cloud credentials."
    exit 1
fi

# Set the environment variable to the credentials file in this directory
export GOOGLE_APPLICATION_CREDENTIALS="$(pwd)/credentials.json"
echo "Using credentials file: $GOOGLE_APPLICATION_CREDENTIALS"

# Run the application
echo "Starting text editor application..."
mvn clean compile exec:java -Dexec.mainClass="com.example.texteditor.SimpleTextEditor"
