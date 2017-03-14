package com.nabto.nabtoPushFirebaseDemo;

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

class discoverLocalHandler  extends AsyncTask<Void, Void, ArrayAdapter<String> > {


    private View mView;
    private NabtoApi nabto;
    private Spinner spinner;
    private Collection<String> devs;
    private Context context;

    // Constructor initializing references to the main activity
    discoverLocalHandler(Context contextIn, View rootview, NabtoApi nab){
        mView = rootview;
        context = contextIn;
        nabto = nab;
        spinner = (Spinner) mView.findViewById(R.id.spinner);
    }
    // Showing progressBar to signal the discover is in running
    @Override
    protected void onPreExecute() {
        mView.findViewById(R.id.progressBar2).setVisibility(View.VISIBLE);
    }
    // Scanning for local uNabto devices.
    @Override
    protected ArrayAdapter<String> doInBackground(Void... param){
        devs = nabto.getLocalDevices();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, new ArrayList<>(devs));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return dataAdapter;
    }
    // doInBackground is not running in main thread, so results are sent to the UI in onPostExecute
    @Override
    protected void onPostExecute(ArrayAdapter<String> dataAdapter) {
        spinner.setAdapter(dataAdapter);
        if(devs.isEmpty()){
            ((TextView)mView.findViewById(R.id.logBox)).append("No devices found, please ensure your device is turned on and connected to the local network\n");
        }
        mView.findViewById(R.id.progressBar2).setVisibility(View.INVISIBLE);

    }
}
