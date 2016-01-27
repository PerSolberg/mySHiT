package no.shitt.myshit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import no.shitt.myshit.adapters.TripListAdapter;
import no.shitt.myshit.helper.AlertDialogueManager;
import no.shitt.myshit.helper.ConnectionDetector;
import no.shitt.myshit.helper.JSONParser;

import no.shitt.myshit.beans.TripItem;
import no.shitt.myshit.helper.ServerAPI;
import no.shitt.myshit.helper.ServerAPIListener;
import no.shitt.myshit.model.TripList;

public class TripsActivity extends ListActivity /*implements ServerAPIListener */ {
    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogueManager alert = new AlertDialogueManager();

    // Progress Dialog
    private ProgressDialog pDialog;

    // Trip Data
    //JSONObject tripData = null;

    // albums JSON url
    //private static final String URL_ALBUMS = "http://api.androidhive.info/songs/albums.php";
    private static final String URL_TRIPS = "http://www.shitt.no/mySHiT/trip?userName=persolberg@hotmail.com&password=Vertex70&sectioned=0&details=non-historic&dummy=55";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        cd = new ConnectionDetector(getApplicationContext());

        // Check for internet connection
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialogue(TripsActivity.this, "Internet Connection Error",
                    "Please, please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        if (TripList.getSharedList().tripCount() == 0) {
            loadTrips();
        }

        // get listview
        ListView lv = getListView();

        // Listview item click listener
        lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // On selecting a trip: TripDetailsActivity will be launched to trip details
                Intent i = new Intent(getApplicationContext(), TripDetailsActivity.class);

                // send album id to tracklist activity to get list of songs under that album
                String trip_code = ((TextView) view.findViewById(R.id.trip_code)).getText().toString();
                i.putExtra("trip_code", trip_code);

                startActivity(i);
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(new HandleNotification(), new IntentFilter("tripsLoaded"));
    }

    private void updateListView() {
        runOnUiThread(new Runnable() {
            public void run() {
                ListAdapter adapter = new TripListAdapter(TripsActivity.this);
                setListAdapter(adapter);
            }
        });
    }

    private class HandleNotification extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("tripsLoaded")) {
                serverCallComplete();
            } else if (intent.getAction().equals("communicationError")) {
                serverCallFailed();
            }
        }
    }

    public void serverCallComplete() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
        Log.d("TripActivity", "Server call succeeded");

        updateListView();
    }

    public void serverCallFailed() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
        Log.d("TripActivity", "Server REST call failed.");
    }

    private void loadTrips() {
        pDialog = new ProgressDialog(TripsActivity.this);
        pDialog.setMessage("Loading SHiT Trips ...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        TripList.getSharedList().getFromServer();
        //new ServerAPI(this).execute(URL_TRIPS);
    }

}