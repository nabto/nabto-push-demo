package com.nabto.tfk.pushnotificationdemo;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.Collection;

public class MainActivity extends AppCompatActivity {
    private NabtoApi nabto;
    private Spinner spinner;
    private Button scanButton;
    private ToggleButton subBut;
    private Button triggerBut;
    private EditText appIdField;
    private TextView logBox;
    private TextView playerIdField;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        OneSignal.startInit(this).init();
        nabto = new NabtoApi(new NabtoAndroidAssetManager(this));
        nabto.startup();
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
                Collection<String> devs = nabto.getLocalDevices();
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, new ArrayList<>(devs));
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	            spinner.setAdapter(dataAdapter);

            }
        });
        appIdField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if(appIdField.getText().toString().equals(getResources().getString(R.string.noAppID))) {
                    appIdField.setText("");
                }
            }
        });
        subBut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                if (isChecked) {
                    OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                        @Override
                        public void idsAvailable(String userId, String registrationId) {
                            Log.d("subBut_click", "Using app id: " + appIdField.getText().toString() + " and Player ID: " + userId);
                            JSONObject staticData = new JSONObject();
                            try {
                                staticData.put("App_id", appIdField.getText().toString());
                                staticData.put("Player_id", userId);
                            } catch (JSONException e){
                                Log.e("subBut_click", "Unable to put to JSON object");
                            }
                            //logBox.append("Using app id: " + appIdField.getText().toString() + " and Player ID: " + userId + "\n");
                            logBox.append("Subscribing with static Data: " + staticData.toString() + "\n");
                            // TODO: CALL UNABTO TO ADD CLIENT
                        }
                    });
                } else {
                    // TODO: CALL UNABTO TO REMOVE CLIENT
                    logBox.append("Removing client subscribtion from uNabto\n");
                }
            }
        });
        triggerBut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // TODO: SEND TRIGGER SIGNAL TO UNABTO
                logBox.append("Triggering push notification\n");
            }
        });
        /*subBut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View V){
                OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                    @Override
                    public void idsAvailable(String userId, String registrationId) {
                        Log.d("subBut_click", "Using app id: " + appIdField.getText().toString() + " and Player ID: " + userId);
                        JSONObject staticData = new JSONObject();
                        try {
                            staticData.put("App_id", appIdField.getText().toString());
                            staticData.put("Player_id", userId);
                        } catch (JSONException e){
                            Log.e("subBut_click", "Unable to put to JSON object");
                        }
                        //logBox.append("Using app id: " + appIdField.getText().toString() + " and Player ID: " + userId + "\n");
                        logBox.append("Using static Data: " + staticData.toString() + "\n");
                    }
                });
            }
        });*/

    }
}
