# Push Notification Demo
Demo showing how to use Push Notifications in Nabto.  The
firebase-android folder implements a simple Android app with Nabto
client software supporting push notification. Similarly, firebase-ios
folder implements a client with push notification support for ios.
The device folder shows how to implement push notifications in a
uNabto device.

Push notifications requires your Firebase project server key to be
configured for your device domain. This key is found in your firebase
project in settings->cloud messaging. This key can currently only be
added to the basestation domain configuration by Nabto employees,
please contact Nabto for further help.

For further information see the local README files.

# Usage
Clone this repository with the recursive argument:
```
git clone --recursive https://github.com/nabto/nabto-push-demo.git
```
This will clone the unabto submodule into the repository.
