package com.nabto.tfk.pushnotificationdemo;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationDisplayedResult;
import com.onesignal.OSNotificationReceivedResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

/**
 * Created by tfk on 28/02/17.
 */

public class NotificationExtender extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(final OSNotificationReceivedResult receivedResult) {
      OverrideSettings overrideSettings = new OverrideSettings();
      overrideSettings.extender = new NotificationCompat.Extender() {
         @Override
         public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
            // Sets the background notification color to Green on Android 5.0+ devices.
             builder.setContentInfo("THE ROOF THE ROOF THE ROOF IS ON FIRE");
             JSONObject data = receivedResult.payload.additionalData;
             int i = 0;
             try{
                i = data.getInt("temp");
             } catch (JSONException e){
                Log.d("notificationExt","Error parsing JSON");
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
/*
public class NotificationExtender extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        // Read properties from result.
        JSONObject data = receivedResult.payload.additionalData;
        int i;
        try {
            i = data.getInt("temp");
        } catch (JSONException e){
            Log.d("notificationExt","Error parsing JSON");
            return true;
        }
        Log.d("notificationExt","temp is: " + i);
        String str = "The temperature of your house is: " + i + ". Your house may be on fire";
        receivedResult.payload.body = str;
        receivedResult.payload.title = "THE ROOF THE ROOF THE ROOF IS ON FIRE";
        // Return true to stop the notification from displaying.
        return false;
    }
}*/
