package com.nabto.tfk.pushnotificationdemo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.nabto.api.*;
import com.onesignal.OneSignal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends AppCompatActivity {
    private NabtoApi nabto;
    private Session session;
    private Spinner spinner;
    private ToggleButton subBut;
    private EditText appIdField;
    private TextView playerIdField;
    private Context context;
    private String userId_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Android initialization
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        // OneSignal Initialization
        OneSignal.startInit(this).inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification).init();
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                userId_ = userId;
            }
        });

        // Nabto Initialization
        nabto = new NabtoApi(new NabtoAndroidAssetManager(this));
        nabto.startup();
        String clientName = getResources().getString(R.string.clientName);
        String clientPass = getResources().getString(R.string.clientPass);
        session = nabto.openSession(clientName, clientPass);
        if (session.getStatus() != NabtoStatus.OK) {
            nabto.createSelfSignedProfile(clientName, clientPass);
            nabto.openSession(clientName, clientPass);
            if (session.getStatus() != NabtoStatus.OK) {
                Log.e("nabtoSession", "Unable to open Nabto session");
            }
        }
        String str;
        InputStream is = getResources().openRawResource(R.raw.queries);
        str = getStringFromInputStream(is);

        Log.d("onCreate", "xml:\n" + str);
        RpcResult res = nabto.rpcSetDefaultInterface(str, session);
        if (res.getStatus() != NabtoStatus.OK) {
            Log.e("onCreate", "rpcSetDefaultInterface failed with: " + res.getJson());
        }

        //Initializing app GUI context
        playerIdField = (TextView) findViewById(R.id.playerIdField);
        spinner = (Spinner) findViewById(R.id.spinner);
        Button scanButton = (Button) findViewById(R.id.scanButton);
        subBut = (ToggleButton) findViewById(R.id.subButton);
        appIdField = (EditText) findViewById(R.id.editText);
        TextView logBox = (TextView) findViewById(R.id.logBox);
        logBox.setMovementMethod(new ScrollingMovementMethod());

        // Getting the OneSignal Player ID
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                playerIdField.setText("Your player ID is: " + userId + "\n");
            }
        });

        // Scanning for local uNabto devices
        new discoverLocalHandler(context,findViewById(android.R.id.content),nabto).execute();

        // Manually trigger a scan for local uNabto devices
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new discoverLocalHandler(context,findViewById(android.R.id.content),nabto).execute();
            }
        });

        // Removing startup text from App ID field when clicked
        appIdField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appIdField.getText().toString().equals(getResources().getString(R.string.noAppID))) {
                    appIdField.setText("");
                }
            }
        });

        // (Un-)Subscribe to push notifications at the device
        subBut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                new subscriptionHandler(findViewById(android.R.id.content), userId_,nabto,session,isChecked).execute();
            }
        });

    }

    // Help function to convert the queries.xml file into a string
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }
}
