package no.shitt.myshit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import no.shitt.myshit.adapters.TripElementListAdapter;
import no.shitt.myshit.beans.TripElementItem;
import no.shitt.myshit.helper.AlertDialogueManager;
import no.shitt.myshit.helper.ConnectionDetector;
import no.shitt.myshit.helper.ServerAPI;
import no.shitt.myshit.helper.ServerAPIListener;
import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.TripList;

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
import android.widget.Toast;

public class TripDetailsActivity extends ListActivity /* implements ServerAPIListener */ {
    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogueManager alert = new AlertDialogueManager();

    // Progress Dialog
    private ProgressDialog pDialog;

    private AnnotatedTrip annotatedTrip;
    //ArrayList<HashMap<String, String>> elementsList;
    //List<TripElementItem> elementsList;

    // tracks JSONArray
    //JSONArray albums = null;
    //JSONArray elements = null;

    // Album id
    String trip_code;
    String trip_name;

    // tracks JSON url
    // id - should be posted as GET params to get track list (ex: id = 5)
    //private static final String URL_ALBUMS = "http://api.androidhive.info/songs/album_tracks.php";
    private static final String URL_TRIP_DETAILS = "http://www.shitt.no/mySHiT/trip/code/MICA2016?userName=persolberg@hotmail.com&password=Vertex70&sectioned=0&details=non-historic";

    // ALL JSON node names
    private static final String JSON_QUERY_RESULTS          = "results";
    private static final String JSON_TRIP_ID                = "id";
    private static final String JSON_TRIP_NAME              = "name";
    private static final String JSON_TRIP_ELEMENTS          = "elements";

    private static final String JSON_ELEM_TYPE              = "type";
    private static final String JSON_ELEM_SUBTYPE           = "subType";
    private static final String JSON_ELEM_ID                = "id";
    //private static final String JSON_ELEM_REF               = "references";
    //private static final String JSON_ELEM_LEG_NO            = "legNo";
    //private static final String JSON_ELEM_SEG_ID            = "segmentId";
    //private static final String JSON_ELEM_SEG_CODE          = "segmentCode";
    private static final String JSON_ELEM_DEP_TIME          = "departureTime";
    //private static final String JSON_ELEM_DEP_TZ            = "departureTimezone";
    private static final String JSON_ELEM_DEP_LOCATION      = "departureLocation";
    private static final String JSON_ELEM_DEP_STOP          = "departureStop";
    //private static final String JSON_ELEM_DEP_ADDR          = "departureAddress";
    //private static final String JSON_ELEM_DEP_TERMINAL_CODE = "departureTerminalCode";
    //private static final String JSON_ELEM_DEP_TERMINAL_NAME = "departureTerminalName";
    //private static final String JSON_ELEM_DEP_COORDINATES   = "departureCoordinates";
    private static final String JSON_ELEM_ARR_TIME          = "arrivalTime";
    //private static final String JSON_ELEM_ARR_TZ            = "arrivalTimezone";
    private static final String JSON_ELEM_ARR_LOCATION      = "arrivalLocation";
    private static final String JSON_ELEM_ARR_STOP          = "arrivalStop";
    //private static final String JSON_ELEM_ARR_ADDR          = "arrivalAddress";
    //private static final String JSON_ELEM_ARR_TERMINAL_CODE = "arrivalTerminalCode";
    //private static final String JSON_ELEM_ARR_TERMINAL_NAME = "arrivalTerminalName";
    //private static final String JSON_ELEM_ARR_COORDINATES   = "arrivalCoordinates";
    //private static final String JSON_ELEM_ROUTE_NO          = "routeNo";
    //private static final String JSON_ELEM_COMPANY           = "company";
    //private static final String JSON_ELEM_COMPANY_CODE      = "companyCode";
    //private static final String JSON_ELEM_COMPANY_PHONE     = "companyPhone";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        cd = new ConnectionDetector(getApplicationContext());

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialogue(TripDetailsActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        // Get album id
        Intent i = getIntent();
        trip_code = i.getStringExtra("trip_code");

        annotatedTrip = TripList.getSharedList().tripByCode(trip_code);

        if (annotatedTrip == null) {
            Log.e("TripDetailsActivity", "Invalid trip!");
        } else if (annotatedTrip.trip.elementCount() == 0) {
            loadTripDetails();
        } else {
            updateListView();
        }
        // Hashmap for ListView
        //elementsList = new ArrayList<HashMap<String, String>>();
        //elementsList = new ArrayList<>();

        // Loading tracks in Background Thread
        //new LoadTripElements().execute();

        // get listview
        ListView lv = getListView();

        /**
         * Listview on item click listener
         * SingleTrackActivity will be lauched by passing album id, song id
         * */
        lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // On selecting single track get song information
                Intent i = new Intent(getApplicationContext(), FlightActivity.class);

                // Pass trip id and element id to details view
                String trip_id = ((TextView) view.findViewById(R.id.trip_id)).getText().toString();
                String element_id = ((TextView) view.findViewById(R.id.element_id)).getText().toString();

                Toast.makeText(getApplicationContext(), "Trip Id: " + trip_id  + ", Element Id: " + element_id, Toast.LENGTH_SHORT).show();

                i.putExtra("trip_id", trip_id);
                i.putExtra("element_id", element_id);

                startActivity(i);
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(new HandleNotification(), new IntentFilter("tripDetailsLoaded"));
    }

    private class HandleNotification extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("tripDetailsLoaded")) {
                serverCallComplete();
            } else if (intent.getAction().equals("communicationError")) {
                serverCallFailed();
            }
        }
    }


    private void updateListView() {
        runOnUiThread(new Runnable() {
            public void run() {
                ListAdapter adapter = new TripElementListAdapter(TripDetailsActivity.this, annotatedTrip);
                setListAdapter(adapter);
            }
        });
    }

    public void serverCallComplete() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
        Log.d("TripDetailsActivity", "Server call succeeded");

        updateListView();
        /*
        runOnUiThread(new Runnable() {
            public void run() {
                ListAdapter adapter = new TripElementListAdapter(TripDetailsActivity.this, annotatedTrip);
                setListAdapter(adapter);
            }
        });
        */
    }

    public void serverCallFailed() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
        Log.d("TripDetailsActivity", "Server REST call failed.");
    }


    private void loadTripDetails() {
        pDialog = new ProgressDialog(TripDetailsActivity.this);
        pDialog.setMessage("Loading trip details ...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
        //new ServerAPI(this).execute(URL_TRIP_DETAILS);
        //new ServerAPI(this).execute(URL_PART1 + trip_code + URL_PART2);
        annotatedTrip.trip.loadDetails();
    }

}
