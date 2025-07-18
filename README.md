# Somnia
A Reddit app for Android

## How to log in
1. Register a Reddit app
    1. In a browser, log into Reddit, go to https://old.reddit.com/prefs/apps/, and create a new app.
    2. Fill in the fields
        * **name**: Can be anything, e.g. `Somnia`
        * **installed app**
        * **redirect**: `http://127.0.0.1`
    3. The app be created now. Under "installed app" there will be a random string of characters, that is your Reddit client ID.
2. Add API details to Somnia
    1. In Settings -> API:
        * **Reddit Client ID**: Paste your Reddit client ID from step 1.iii
        * **Reddit redirect URI**: Make sure this matches the url from step 1.ii
3. Log into Somnia
    * In Settings -> Account -> Add new account, log into Reddit.
