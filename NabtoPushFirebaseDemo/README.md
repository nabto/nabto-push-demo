# Android Push Notification Demo App
To use this app you need to have the following software installed:
 * Android SDK tools
 * Android Studio
Once these are installed, open this folder as a project in Android Studio. 
You will also need a Firebase project (creatable from https://console.firebase.google.com)

In Android Studio go to Tools->firebase
click Notifications -> "Receive Notifications in your app"
click "Connect your app to Firebase"
follow the instructions to connect your app to your Firebase project

compile and run the app.

## Implementation overview
It is recommended to use location keys to set the text of the push notification. This is done by setting the "title_loc_key" and "body_loc_key" in the push notification on the uNabto device. In this example, these keys are defined in the ```res/values/strings.xml``` file as "body_1" and "title_1". Where the body example is taking a string as input argument which the uNabto device must include in the "body_loc_args" field. This method means the notification strings can be defined in multiple languages configured by the chosen device language settings. See https://developer.android.com/training/basics/supporting-devices/languages.html for details on how to implement different languages. 
The App consists of 6 Java classes described below.

### MainActivity
The Main Activity initializes the NabtoApi and opens a Session with a self signed certificate. It will then set the default RPC interface based on the queries.xml file in ```res/raw/queries.xml```. This activity also set up the onClickListeners for the buttons using the ```discoverLocalHandler``` and ```subscriptionHandler``` classes.

### discoverLocalHandler
This class is used to discover uNabto devices on the local network in a background thread so the app does not freeze while discovery is ongoing. The necessary context is provided to the constructor. A progress bar is shown in the ```onPreExecute``` method, as this has to be done in the main thread. The ```getLocalDevices``` call is done in the background thread, and the results passed to the ```onPostExecute``` where the discovered devices is put into the spinner menu, and the progressbar is removed.

### subscriptionHandler
An asyncronous task similar to the ```discoverLocalHandler``` to handle RPC invokes in the background. Again, the needed context is passed in the constructor, and User Interface actions is handled in the ```onPreExecute``` method where the selected uNabto device is retreived from the spinner menu. If the toggle button is checked, it will construct the client specific content for the push notification as a JSON document. This field must contain a "to" field with the Firebase registration token uniquely identifying the phone to Firebase. Additionally, it can contain any field described in https://firebase.google.com/docs/cloud-messaging/http-server-ref. In this example, we add the field:
```
"notification":{
  "sound" : "default"
}
```
to set the sound played when the notification is received to the default set in the Android system settings. An RPC Invoke is then called according to the interface specified in the  ```res/raw/queries.xml``` file, with the client specific data and a pnsId. The pnsId is set to 1 as this is the Firebase PNS.

If the toggle button is unchecked, the unsubscribe RPC is invoked which takes no parameters.

Finally, a status string is returned to the ```onPostExecute``` method handling UI actions.

### MyFirebaseMessagingService
This is the service handling received push notifications. If background data is included in the push notification, it will be passed to the ```onMessageReveived``` method when it is received. Otherwise, this function will be called with the entire push notification if the app is in the foreground when the notification is received. Notifications not including background data and received while the app is in the background does not invoke any methods in the app software. In this case, if a notification is received with the app in the foreground, it is forwarded to the ```AlertActivity``` class which will show it as an alert dialog.

### AlertActivity
Activity class used to show an alert dialog when a notification is received while the app is in the foreground. The push notification is passed to this activity through the Intent Extras.
This class is instantiated from the ```MyFirebaseMessagingService``` class.

### MyFirebaseInstanceIDService
The Firebase registration token can be changed if the user reinstalls the app, restores the app on a new device, or clears app data, or if the app instance ID is deleted programatically in the app. To handle this, it is recommended to implement this service with the ```onTokenRefresh()``` method.