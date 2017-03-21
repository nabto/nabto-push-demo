package com.nabto.nabtoPushFirebaseDemo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.iid.FirebaseInstanceId;
import com.nabto.api.NabtoAndroidAssetManager;
import com.nabto.api.NabtoApi;
import com.nabto.api.NabtoStatus;
import com.nabto.api.RpcResult;
import com.nabto.api.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private NabtoApi nabto;
    private Session session;
    private TextView tokenField;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
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

        Log.d("onCreate", "xml: " + str);
        RpcResult res = nabto.rpcSetDefaultInterface(str, session);
        if (res.getStatus() != NabtoStatus.OK) {
            Log.e("onCreate", "rpcSetDefaultInterface failed with: " + res.getJson());
        }
        //Initializing app GUI context
        tokenField = (TextView) findViewById(R.id.tokenField);
        Button scanButton = (Button) findViewById(R.id.scanButton);
        ToggleButton subBut = (ToggleButton) findViewById(R.id.subButton);

        TextView logBox = (TextView) findViewById(R.id.logBox);
        logBox.setMovementMethod(new ScrollingMovementMethod());
        tokenField.setText(FirebaseInstanceId.getInstance().getToken());
        Log.d("onCreate","token is: " + FirebaseInstanceId.getInstance().getToken());
        // Scanning for local uNabto devices
        new discoverLocalHandler(this,findViewById(android.R.id.content),nabto).execute();

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nabto.startup();
                new discoverLocalHandler(context,findViewById(android.R.id.content),nabto).execute();
            }
        });

        // (Un-)Subscribe to push notifications at the device
        subBut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                new subscriptionHandler(findViewById(android.R.id.content), nabto,session,isChecked).execute();
            }
        });


    }
    @Override
    protected void onStop(){
        super.onStop();
        nabto.shutdown();
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
