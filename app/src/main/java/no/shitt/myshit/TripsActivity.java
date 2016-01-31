package no.shitt.myshit;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import no.shitt.myshit.adapters.TripListAdapter;
import no.shitt.myshit.helper.AlertDialogueManager;
import no.shitt.myshit.helper.ConnectionDetector;
import no.shitt.myshit.model.TripList;
import no.shitt.myshit.model.User;

public class TripsActivity extends AppCompatActivity /* ListActivity */ {
    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogueManager alert = new AlertDialogueManager();

    // Progress Dialog
    private ProgressDialog pDialog;

    // List view
    ListView listView;

    // JSON url
    //private static final String URL_TRIPS = "http://www.shitt.no/mySHiT/trip?userName=persolberg@hotmail.com&password=Vertex70&sectioned=0&details=non-historic&dummy=55";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        // Set up toolbar and enable Up button
        Toolbar myToolbar = (Toolbar) findViewById(R.id.trip_list_toolbar);
        setSupportActionBar(myToolbar);

        // Set up list view
        listView = (ListView) findViewById(R.id.trip_list);
        //ListView lv = getListView();
        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // On selecting a trip: TripDetailsActivity will be launched to trip details
                Intent i = new Intent(getApplicationContext(), TripDetailsActivity.class);

                // send trip code to TripDetails activity to get list of trip elements
                String trip_code = ((TextView) view.findViewById(R.id.trip_code)).getText().toString();
                i.putExtra(Constants.IntentExtra.TRIP_CODE, trip_code);

                startActivity(i);
            }
        });

        if (User.sharedUser.getUserName() == null) {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        } else {
            TripList.getSharedList().loadFromArchive();
            if (TripList.getSharedList().tripCount() == 0) {
                // Check for internet connection and load from server
                cd = new ConnectionDetector(getApplicationContext());
                if (!cd.isConnectingToInternet()) {
                    // Internet Connection is not present
                    alert.showAlertDialogue(TripsActivity.this, "Internet Connection Error",
                            "Please, please connect to working Internet connection", false);
                    // stop executing code by return
                    return;
                }

                loadTrips();
            } else {
                updateListView();
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(new HandleNotification(), new IntentFilter(Constants.Notification.TRIPS_LOADED));
        LocalBroadcastManager.getInstance(this).registerReceiver(new HandleNotification(), new IntentFilter(Constants.Notification.LOGON_SUCCEEDED));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trip_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.d("TripsActivity", "Opening settings screen (NOT)");
                return true;

            case R.id.action_logout:
                // Log out user and clear list
                logout();
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

    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("TripsActivity", "Returning from trip details or logon: " + data.getAction() );
    }
    */

    @Override
    public void onResume() {
        Log.d("TripsActivity", "Resuming");
        super.onResume();

        // Check if we're logged out, if so, show logon screen
        if (User.sharedUser.getUserName() == null) {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        }
    }

    /*
    @Override
    public void onWindowFocusChanged (boolean hasFocus)
    {
        Log.d("TripsActivity", "Got focus");
        super.onWindowFocusChanged(hasFocus);
    }
    */

    private void updateListView() {
        runOnUiThread(new Runnable() {
            public void run() {
                ListAdapter adapter = new TripListAdapter(TripsActivity.this);
                //setListAdapter(adapter);
                listView.setAdapter(adapter);
            }
        });
    }

    private class HandleNotification extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.Notification.LOGON_SUCCEEDED)) {
                loadTrips();
            } else if (intent.getAction().equals(Constants.Notification.TRIPS_LOADED)) {
                serverCallComplete();
            } else if (intent.getAction().equals(Constants.Notification.COMMUNICATION_FAILED)) {
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
        TripList.getSharedList().saveToArchive();

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

    private void logout() {
        TripList.getSharedList().clear();
        User.sharedUser.logout();
        //this.recreate();
        updateListView();
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
    }
}