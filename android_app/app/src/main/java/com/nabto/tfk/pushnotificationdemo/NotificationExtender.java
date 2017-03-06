package com.nabto.tfk.pushnotificationdemo;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationDisplayedResult;
import com.onesignal.OSNotificationReceivedResult;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tfk on 28/02/17.
 * Class extending incoming notifications with relevant text before it is shown to the user.
 */

public class NotificationExtender extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(final OSNotificationReceivedResult receivedResult) {
        OverrideSettings overrideSettings = new OverrideSettings();
        overrideSettings.extender = new NotificationCompat.Extender() {
            @Override
            public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
                // using the builder, the notification can be changed at will
                // This is where languages should be handled
                JSONObject data = receivedResult.payload.additionalData;
                int i = 0;
                try{
                    i = data.getInt("temp");
                    Log.d("notificationExt","Got temperature of: " + i);
                } catch (JSONException e){
                    Log.e("notificationExt","Error parsing JSON: " + e.toString());
                    Log.e("notificationExt","data is: " + data.toString());
                }
                builder.setContentText("The temperature is: " + i + ". Your house may be on fire.");
                builder.setContentTitle("THE ROOF IS ON FIRE");
                return builder;
            }
        };

        OSNotificationDisplayedResult displayedResult = displayNotification(overrideSettings);
        Log.d("OneSignalExample", "Notification displayed with id: " + displayedResult.androidNotificationId);

        return false;
    }
}
