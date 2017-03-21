# uNabto device stub
Device made to show how to use push notifications with Nabto.

## Usage:

```
mkdir build
cd build
cmake ../
make -j
./push_device_stub -d DEVICE_ID -k ENCRYPTION_KEY
```
When the device stub is running, pres enter to trigger a push notification. This will send a push notification to all subscribed clients. Clients should subscribe as shown in the mobile apps on this repository.

## Implementation details

### unabto_main.c
This implements the main function, it sets up a standard uNabto device, then calls demo_init() which initializes the push demo, and then starts an infinite loop ticking the uNabto core, and listens for user inputs using select with timeouts. If the user presses enter, the sendPN function in the unabto_application.c is called.

### unabto_application.c
Handling the push notifications using the stateful_push_service module. This module uses a callback function to provide feedback after the notification is handled.
This file also implements a persistence file to remember client subscriptions after a restart. This file is initialized in demo_init(). When new subscription data is available, the updatePersistanceFile() function is used to update this file.
sendPN() is used to construct a push notification to each subscribed client, and pass it to the stateful_push_service.

The application_event() function is called whenever a client invokes an RPC. This handles clients (un-)subscribing.