# Setting Up Google Cloud Credentials

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Speech-to-Text API for your project:
   - Navigate to "APIs & Services" > "Library"
   - Search for "Speech-to-Text API"
   - Click "Enable"

4. Create a service account:
   - Go to "IAM & Admin" > "Service Accounts"
   - Click "Create Service Account"
   - Enter a name and click "Create"
   - Grant the "Speech-to-Text User" role to the service account
   - Click "Continue" and then "Done"

5. Create a key for the service account:
   - Find your service account in the list and click on it
   - Go to the "Keys" tab
   - Click "Add Key" > "Create new key"
   - Choose JSON format and click "Create"
   - The key file will be downloaded to your computer

6. Move the downloaded key file to:
   `/home/alexander/git_repos/text_editor/credentials.json`

Now the speech recognition feature should work with your Google Cloud credentials.
