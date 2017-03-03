package com.nabto.tfk.pushnotificationdemo;

import android.util.Log;

import com.onesignal.OSNotification;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tfk on 03/03/17.
 */

public class PnRecHandler implements OneSignal.NotificationReceivedHandler {
    @Override
    public void notificationReceived(OSNotification notification) {
        JSONObject data = notification.payload.additionalData;
        int temp;
        Log.d("PnRecHandler", "Handling Received PN");
        if (data != null) {
            try {
                temp = data.getInt("temp");
                Log.i("PnRecHandler", "Temperature is: " + temp);
            } catch (JSONException e) {

            }
        }
    }
}
