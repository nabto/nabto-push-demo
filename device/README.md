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