package com.nabto.nabtoPushFirebaseDemo;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.iid.FirebaseInstanceId;
import com.nabto.api.NabtoApi;
import com.nabto.api.NabtoStatus;
import com.nabto.api.RpcResult;
import com.nabto.api.Session;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by tfk on 06/03/17.
 * Class handling (un-)subscribing to push notifications on the uNabto device
 */

class subscriptionHandler extends AsyncTask<Void, Void, String > {

    private View mView;
    private String token_;
    private NabtoApi nabto;
    private Session session;
    private boolean isChecked;

    private Spinner spinner;
    private ToggleButton subBut;
    private TextView logBox;

    private Object obj;

    private boolean shouldRun;

    // Constructor initializing references to the main activity
    subscriptionHandler(View rootview, NabtoApi nab, Session sess, boolean checked){
        mView = rootview;
        token_ = FirebaseInstanceId.getInstance().getToken();
        nabto = nab;
        shouldRun = true;
        session = sess;
        isChecked = checked;

        spinner = (Spinner) mView.findViewById(R.id.spinner);
        subBut = (ToggleButton) mView.findViewById(R.id.subButton);
        logBox = (TextView) mView.findViewById(R.id.logBox);
    }
    // Getting information from the UI must be done in the main thread before forking in doInBackground()
    @Override
    protected void onPreExecute() {
        mView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        obj = spinner.getSelectedItem();
        if(obj == null){
            Log.d("subBut_click","Cannot get selected device");
            subBut.setChecked(false);
            shouldRun = false;
            return;
        }
    }
    // invoking RPC at the selected uNabto device depending on the toggle button status
    @Override
    protected String doInBackground(Void... param){
        String retStr = "";
        if(!shouldRun){
            retStr = "No device selected, please scan for local devices.\n";
            return retStr;
        }
        if (isChecked) {
            Log.d("subBut_click", "Using registration token: " + token_);
            JSONObject staticData = new JSONObject();
            try {
                staticData.put("token", token_);
            } catch (JSONException e) {
                Log.e("subBut_click", "Unable to put to JSON object");
                return null;
            }
            String dev = "nabto://" + obj.toString()  + "/push_subscribe.json?staticData=" + staticData.toString() + "&pnsid=1";
            retStr = retStr + "Subscribing using:\n " + dev + "\n";
            RpcResult res = nabto.rpcInvoke(dev, session);
            if (res.getStatus() != NabtoStatus.OK) {
                Log.d("subBut_click", "Rpc failed with: " + res.getJson());
                retStr = retStr + "Rpc failed with: " + res.getJson() + "\n";
            }

        } else {
            String dev = "nabto://" + obj.toString() + "/push_unsubscribe.json";
            retStr = retStr + "Removing client subscribtion from uNabto using:\n" + dev + "\n";
            RpcResult res = nabto.rpcInvoke(dev,session);
            if (res.getStatus() != NabtoStatus.OK) {
                Log.d("subBut_click", "Rpc failed with: " + res.getJson());
                retStr = retStr + "Rpc failed with: " + res.getJson() + "\n";
            }
        }
        return retStr;
    }
    // Outputs must be sent to the UI from the main thread
    @Override
    protected void onPostExecute(String param) {
        logBox.append(param);
        mView.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

    }
}

