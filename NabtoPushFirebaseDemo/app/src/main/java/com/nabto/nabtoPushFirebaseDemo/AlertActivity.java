package com.nabto.nabtoPushFirebaseDemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

public class AlertActivity extends Activity {
    private final static String TAG = "AlertActivity";
    private Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        Bundle extras = getIntent().getExtras();
        RemoteMessage msg = (RemoteMessage) extras.get("msg");
        int bodyId;
        String bodyArg;
        try {
            bodyId = getResources().getIdentifier(msg.getNotification().getBodyLocalizationKey(), "string", getPackageName());
            bodyArg = msg.getNotification().getBodyLocalizationArgs()[0];
        } catch (Exception e){
            context.finish();
            return;
        }
        int titleId = getResources().getIdentifier(msg.getNotification().getTitleLocalizationKey(),"string",getPackageName());

        AlertDialog alertDialog = new AlertDialog.Builder(AlertActivity.this).create();
        alertDialog.setTitle(getResources().getString(titleId));
        alertDialog.setMessage(getResources().getString(bodyId,bodyArg));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        context.finish();
                    }
                });
        alertDialog.show();
    }
}
