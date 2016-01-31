package no.shitt.myshit;

import no.shitt.myshit.adapters.TripElementListAdapter;
import no.shitt.myshit.helper.AlertDialogueManager;
import no.shitt.myshit.helper.ConnectionDetector;
import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.TripElement;
import no.shitt.myshit.model.TripList;
import no.shitt.myshit.model.User;

//import android.app.ActionBar;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TripDetailsActivity extends AppCompatActivity /* ListActivity */ {
    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogueManager alert = new AlertDialogueManager();

    // List view
    ListView listView;

    // Progress Dialog
    private ProgressDialog pDialog;

    private AnnotatedTrip annotatedTrip;

    // Trip ids
    String trip_code;
    String trip_name;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        // Set up toolbar and enable Up button
        Toolbar myToolbar = (Toolbar) findViewById(R.id.trip_details_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        // Check if Internet present
        cd = new ConnectionDetector(getApplicationContext());
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialogue(TripDetailsActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        // Get trip code
        Intent i = getIntent();
        trip_code = i.getStringExtra(Constants.IntentExtra.TRIP_CODE);

        annotatedTrip = TripList.getSharedList().tripByCode(trip_code);
        ab.setTitle(annotatedTrip.trip.name);

        // get listview
        //ListView lv = getListView();
        listView = (ListView) findViewById(R.id.trip_details_list);

        /**
         * Listview on item click listener
         * */
        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // On selecting trip element, show appropriate details screen
                String trip_id = ((TextView) view.findViewById(R.id.trip_id)).getText().toString();
                String element_id = ((TextView) view.findViewById(R.id.element_id)).getText().toString();
                TripElement element = TripList.getSharedList().tripByCode(trip_code).trip.elementById(Integer.valueOf(element_id)).tripElement;

                Intent i;
                if ("TRA".equals(element.type) && "AIR".equals(element.subType)) {
                    i = new Intent(getApplicationContext(), FlightActivity.class);
                } else if ("TRA".equals(element.type) && "PBUS".equals(element.subType)) {
                    i = new Intent(getApplicationContext(), PrivateTransportActivity.class);
                } else if ("TRA".equals(element.type) && "LIMO".equals(element.subType)) {
                    i = new Intent(getApplicationContext(), PrivateTransportActivity.class);
                } else {
                    Log.e("TripDetailsActivity", "Unsupported element type");
                    return;
                }

                Log.d("TripDetailsActivity", "Trip Id: " + trip_id + ", Trip Code: " + trip_code + ", Element Id: " + element_id);

                // Pass trip id and element id to details view
                i.putExtra(Constants.IntentExtra.TRIP_CODE, trip_code);
                i.putExtra(Constants.IntentExtra.ELEMENT_ID, element_id);

                startActivity(i);
            }
        });

        if (annotatedTrip == null) {
            Log.e("TripDetailsActivity", "Invalid trip!");
        } else if (annotatedTrip.trip.elementCount() == 0) {
            loadTripDetails();
        } else {
            updateListView();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(new HandleNotification(), new IntentFilter(Constants.Notification.TRIP_DETAILS_LOADED));
    }

    private class HandleNotification extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.Notification.TRIP_DETAILS_LOADED)) {
                serverCallComplete();
            } else if (intent.getAction().equals(Constants.Notification.COMMUNICATION_FAILED)) {
                serverCallFailed();
            }
        }
    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.d("TripsActivity", "Opening settings screen (NOT)");
                return true;

            case R.id.action_logout:
                // Log out user and clear list
                TripList.getSharedList().clear();
                User.sharedUser.logout();
                finish();
                return true;

            case R.id.action_alerts:
                Log.d("TripsActivity", "Opening alerts list (NOT)");
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trip_list_menu, menu);
        return true;
    }

    private void updateListView() {
        runOnUiThread(new Runnable() {
            public void run() {
                ListAdapter adapter = new TripElementListAdapter(TripDetailsActivity.this, annotatedTrip);
                //setListAdapter(adapter);
                listView.setAdapter(adapter);
            }
        });
    }

    public void serverCallComplete() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
        Log.d("TripDetailsActivity", "Server call succeeded");
        TripList.getSharedList().saveToArchive();

        updateListView();
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

        annotatedTrip.trip.loadDetails();
    }

}
