package com.nabto.tfk.pushnotificationdemo;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.nabto.api.NabtoApi;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by tfk on 06/03/17.
 * Class handling the discovery of local uNabto devices
 */

public class discoverLocalHandler  extends AsyncTask<Void, Void, ArrayAdapter<String> > {


    private View mView;
    private NabtoApi nabto;
    private Spinner spinner;
    private Collection<String> devs;
    private Context context;


    //public subscriptionHandler(Context context, Activity act, String userId, NabtoApi nab, Session sess, boolean checked){
    public discoverLocalHandler(Context contextIn, View rootview, NabtoApi nab){
        mView = rootview;
        context = contextIn;
        nabto = nab;
        spinner = (Spinner) mView.findViewById(R.id.spinner);
    }
    @Override
    protected void onPreExecute() {
        mView.findViewById(R.id.progressBar2).setVisibility(View.VISIBLE);
    }
    @Override
    protected ArrayAdapter<String> doInBackground(Void... param){
        devs = nabto.getLocalDevices();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, new ArrayList<>(devs));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return dataAdapter;
    }
    @Override
    protected void onPostExecute(ArrayAdapter<String> dataAdapter) {
        spinner.setAdapter(dataAdapter);
        if(devs.isEmpty()){
            ((TextView)mView.findViewById(R.id.logBox)).append("No devices found, please ensure your device is turned on and connected to the local network\n");
        }
        mView.findViewById(R.id.progressBar2).setVisibility(View.INVISIBLE);

    }
}
