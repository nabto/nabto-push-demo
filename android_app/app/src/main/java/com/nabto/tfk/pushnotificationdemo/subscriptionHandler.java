package com.nabto.tfk.pushnotificationdemo;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.nabto.api.NabtoApi;
import com.nabto.api.NabtoStatus;
import com.nabto.api.RpcResult;
import com.nabto.api.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by tfk on 06/03/17.
 */

public class subscriptionHandler extends AsyncTask<Void, Void, String > {

    private View mView;
    private String userId_;
    private NabtoApi nabto;
    private Session session;
    private boolean isChecked;

    private Spinner spinner;
    private ToggleButton subBut;
    private TextView logBox;
    private EditText appIdField;

    private Object obj;
    private String appId;

    private boolean shouldRun;

    //public subscriptionHandler(Context context, Activity act, String userId, NabtoApi nab, Session sess, boolean checked){
    public subscriptionHandler(View rootview, String userId, NabtoApi nab, Session sess, boolean checked){
        mView = rootview;
        //activity = new WeakReference<Activity>(act);;
        userId_ = userId;
        nabto = nab;
        shouldRun = true;
        session = sess;
        isChecked = checked;

        spinner = (Spinner) mView.findViewById(R.id.spinner);
        subBut = (ToggleButton) mView.findViewById(R.id.subButton);
        logBox = (TextView) mView.findViewById(R.id.logBox);
        appIdField = (EditText) mView.findViewById(R.id.editText);
    }
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
        appId = appIdField.getText().toString();
    }
    @Override
    protected String doInBackground(Void... param){
        String retStr = "";
        if(!shouldRun){
            retStr = "preExecute marked should not run";
            return retStr;
        }
        if (isChecked) {
            Log.d("subBut_click", "Using app id: " + appId + " and Player ID: " + userId_);
            JSONObject staticData = new JSONObject();
            try {
                staticData.put("App_id", appId);
                staticData.put("Player_id", userId_);
            } catch (JSONException e) {
                Log.e("subBut_click", "Unable to put to JSON object");
                return null;
            }
            String dev = "nabto://" + obj.toString()  + "/push_subscribe.json?staticData=" + staticData.toString();
            retStr = retStr + "Subscribing using:\n " + dev + "\n";
            //logBox.append("Subscribing using:\n " + dev + "\n");
            RpcResult res = nabto.rpcInvoke(dev, session);
            if (res.getStatus() != NabtoStatus.OK) {
                Log.d("subBut_click", "Rpc failed with: " + res.getJson());
            }

        } else {
            String dev = "nabto://" + obj.toString() + "/push_unsubscribe.json";
            retStr = retStr + "Removing client subscribtion from uNabto using:\n" + dev + "\n";
            //logBox.append("Removing client subscribtion from uNabto using:\n" + dev + "\n");
            RpcResult res = nabto.rpcInvoke(dev,session);
            if (res.getStatus() != NabtoStatus.OK) {
                Log.d("subBut_click", "Rpc failed with: " + res.getJson());
            }
        }
        return retStr;
    }
    @Override
    protected void onPostExecute(String param) {
        logBox.append(param);
        mView.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

    }
}

