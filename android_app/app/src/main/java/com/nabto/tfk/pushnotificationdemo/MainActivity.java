package com.nabto.tfk.pushnotificationdemo;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.nabto.api.*;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;


public class MainActivity extends AppCompatActivity {
    private NabtoApi nabto;
    private Session session;
    private Spinner spinner;
    private Button scanButton;
    private ToggleButton subBut;
    private Button triggerBut;
    private EditText appIdField;
    private TextView logBox;
    private TextView playerIdField;
    private Context context;
    private String clientName;
    private String clientPass;
    private Collection<String> devs;
    private String userId_;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        clientName = getResources().getString(R.string.clientName);
        clientPass = getResources().getString(R.string.clientPass);
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

        logBox = (TextView) findViewById(R.id.logBox);
        playerIdField = (TextView) findViewById(R.id.playerIdField);
        spinner = (Spinner) findViewById(R.id.spinner);
        scanButton = (Button) findViewById(R.id.scanButton);
        subBut = (ToggleButton) findViewById(R.id.subButton);
        triggerBut = (Button) findViewById(R.id.triggerBut);
        appIdField = (EditText) findViewById(R.id.editText);
        logBox.setMovementMethod(new ScrollingMovementMethod());
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                playerIdField.setText("Your player ID is: " + userId + "\n");
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devs = nabto.getLocalDevices();
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, new ArrayList<>(devs));
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(dataAdapter);

            }
        });
        appIdField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appIdField.getText().toString().equals(getResources().getString(R.string.noAppID))) {
                    appIdField.setText("");
                }
            }
        });
        subBut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    Log.d("subBut_click", "Using app id: " + appIdField.getText().toString() + " and Player ID: " + userId_);
                    JSONObject staticData = new JSONObject();
                    try {
                        staticData.put("App_id", appIdField.getText().toString());
                        staticData.put("Player_id", userId_);
                    } catch (JSONException e) {
                        Log.e("subBut_click", "Unable to put to JSON object");
                    }
                    //logBox.append("Using app id: " + appIdField.getText().toString() + " and Player ID: " + userId + "\n");
                    // TODO: CALL UNABTO TO ADD CLIENT
                    try {
                        String item = spinner.getSelectedItem().toString();
                    } catch (NullPointerException e){
                        Log.d("subBut_click","Cannot get selected device");
                        return;
                    }
                    String dev = "nabto://" + spinner.getSelectedItem().toString()  + "/push_subscribe.json?staticData=" + staticData.toString();
                    logBox.append("Subscribing using:\n " + dev + "\n");
                    RpcResult res = nabto.rpcInvoke(dev, session);
                    if (res.getStatus() != NabtoStatus.OK) {
                        Log.d("subBut_click", "Rpc failed with: " + res.getJson());
                    }

                } else {
                    // TODO: CALL UNABTO TO REMOVE CLIENT
                    String dev = "nabto://" + spinner.getSelectedItem().toString() + "/push_unsubscribe.json";
                    logBox.append("Removing client subscribtion from uNabto using:\n" + dev + "\n");
                    RpcResult res = nabto.rpcInvoke(dev,session);
                    if (res.getStatus() != NabtoStatus.OK) {
                        Log.d("subBut_click", "Rpc failed with: " + res.getJson());
                    }
                }
            }
        });
        triggerBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: SEND TRIGGER SIGNAL TO UNABTO
                String dev = "nabto://" + spinner.getSelectedItem().toString() + "/push_trigger.json";
                logBox.append("Triggering push notification with url:\n" + dev + "\n");
                RpcResult res = nabto.rpcInvoke(dev,session);
                if (res.getStatus() != NabtoStatus.OK) {
                    Log.d("subBut_click", "Rpc failed with: " + res.getJson());
                }
            }
        });
    }
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
