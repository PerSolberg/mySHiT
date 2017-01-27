package no.shitt.myshit;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
//import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
//import android.widget.AdapterView;
import android.widget.ExpandableListView;
//import android.widget.ListAdapter;
//import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import no.shitt.myshit.adapters.TripListAdapter;
import no.shitt.myshit.helper.AlertDialogueManager;
import no.shitt.myshit.helper.ConnectionDetector;
import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.ChangeState;
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
    ExpandableListView listView;
    TripListAdapter listAdapter;

    // Trip code to show in details list
    String tripCode = null;

    // Save state keys
    private static final String STATE_TRIP_CODE = "tripCode";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.d("TripActivity", "onCreate starting");
        //Log.d("TripActivity", "onCreate Intent action = " + getIntent().getAction());

        if (Constants.DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .penaltyFlashScreen()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    //.penaltyDeath()
                    .build());
        }

        //Log.d("TripsActivity", "onCreate: savedInstanceState = " + savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        // Make sure preferences are initialised
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Set up toolbar and enable Up button
        Toolbar myToolbar = (Toolbar) findViewById(R.id.trip_list_toolbar);
        //myToolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(myToolbar);

        // Common name isn't persisted so may not work (especially if app crashes due to race condition)
        //ActionBar ab = getSupportActionBar();
        //ab.setTitle(User.sharedUser.getCommonName());

        // Set up list view
        listView = (ExpandableListView) findViewById(R.id.trip_list);
        listAdapter = new TripListAdapter(TripsActivity.this);
        listView.setAdapter(listAdapter);

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
                // On selecting a trip: TripDetailsActivity will be launched to trip details
                Intent i = new Intent(getApplicationContext(), TripDetailsActivity.class);

                // send trip code to TripDetails activity to get list of trip elements
                tripCode = ((TextView) view.findViewById(R.id.trip_code)).getText().toString();
                i.putExtra(Constants.IntentExtra.TRIP_CODE, tripCode);

                startActivity(i);
                return true;
            }
        });

        /*
        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // On selecting a trip: TripDetailsActivity will be launched to trip details
                Intent i = new Intent(getApplicationContext(), TripDetailsActivity.class);

                // send trip code to TripDetails activity to get list of trip elements
                tripCode = ((TextView) view.findViewById(R.id.trip_code)).getText().toString();
                i.putExtra(Constants.IntentExtra.TRIP_CODE, tripCode);

                startActivity(i);
            }
        });
        */

        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.trip_list_container);
        swipeLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        //Log.i("TripsActivity", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        loadTrips(true);
                    }
                }
        );

        if (User.sharedUser.getUserName() != null) {
            TripList.getSharedList().loadFromArchive();
            if (TripList.getSharedList().tripCount() == 0) {
                // Check for internet connection and load from server
                cd = new ConnectionDetector(getApplicationContext());
                if (!cd.isConnectingToInternet()) {
                    // Internet Connection is not present
                    alert.showAlertDialogue(TripsActivity.this, "Internet Connection Error",
                            "Please connect to working Internet connection", false);
                    // stop executing code by return
                    return;
                }

                loadTrips(false);
            } else {
                updateListView();
            }
        }

        if (savedInstanceState != null) {
            //Log.d("TripsActivity", "Restoring state");
            // Restore value of members from saved state
            tripCode = savedInstanceState.getString(STATE_TRIP_CODE);
            if (tripCode != null) {
                //Log.d("TripsActivity", "onCreate refreshing modification flag for trip");
                AnnotatedTrip annotatedTrip = TripList.getSharedList().tripByCode(tripCode);
                if (annotatedTrip.trip.areTripElementsUnchanged()) {
                    annotatedTrip.modified = ChangeState.UNCHANGED;
                    TripList.getSharedList().saveToArchive();
                    updateListView();
                }
                tripCode = null;
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(new HandleNotification(), new IntentFilter(Constants.Notification.TRIPS_LOADED));
        LocalBroadcastManager.getInstance(this).registerReceiver(new HandleNotification(), new IntentFilter(Constants.Notification.LOGON_SUCCEEDED));
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        //Log.d("TripsActivity", "onRestoreInstanceState (state = " + savedInstanceState + ")");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Log.d("TripsActivity", "onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trip_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Log.d("TripsActivity", "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.action_settings:
                //Log.d("TripsActivity", "Opening settings screen");
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;

            case R.id.action_refresh:
                //Log.d("TripsActivity", "Refreshing from menu");
                SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.trip_list_container);
                swipeLayout.setRefreshing(true);
                loadTrips(true);
                return true;

            case R.id.action_logout:
                // Log out user and clear list
                logout();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        //Log.d("TripsActivity", "Saving state");
        if (tripCode != null) {
            savedInstanceState.putString(STATE_TRIP_CODE, tripCode);
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        //Log.d("TripActivity", "onResume Starting, action = " + getIntent().getAction() );

        // Check if we're logged out, if so, show logon screen
        if (User.sharedUser.getUserName() == null) {
            //Log.d("TripsActivity", "Not logged in - launching logon screen");
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);

            // Prevent refresh attempt below
            tripCode = null;
        } else {
            // Otherwise refresh data
            updateListView();
        }

        // If we've viewed trip details, reset modification flag if all element changes have been reviewed
        if (tripCode != null) {
            //Log.d("TripsActivity", "Refreshing modification flag for trip");
            AnnotatedTrip annotatedTrip = TripList.getSharedList().tripByCode(tripCode);
            if (annotatedTrip != null && annotatedTrip.trip.areTripElementsUnchanged()) {
                annotatedTrip.modified = ChangeState.UNCHANGED;
                TripList.getSharedList().saveToArchive();
                updateListView();
            }
            tripCode = null;
        }

        {
            String fcmToken;

            fcmToken = FirebaseInstanceId.getInstance().getToken();
            if (fcmToken != null) {
                //Log.d("TripsActivity", "Firebase token = " + fcmToken);
            }
        }
    }

    private void updateListView() {
        //Log.d("TripsActivity", "updateListView");
        runOnUiThread(new Runnable() {
            public void run() {
                listAdapter.notifyDataSetChanged();
                //TripListAdapter adapter = new TripListAdapter(TripsActivity.this);
                //listView.setAdapter(adapter);
                listAdapter.applyPreviousCollapse(listView);
            }
        });
    }

    private class HandleNotification extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d("TripsActivity", "HandleNotification onReceive, intent action = " + intent.getAction());
            if (intent.getAction().equals(Constants.Notification.LOGON_SUCCEEDED)) {
                loadTrips(false);
            } else if (intent.getAction().equals(Constants.Notification.TRIPS_LOADED)) {
                serverCallComplete();
            } else if (intent.getAction().equals(Constants.Notification.COMMUNICATION_FAILED)) {
                serverCallFailed();
            }
        }
    }

    public void serverCallComplete() {
        //Log.d("TripsActivity", "serverCallComplete");
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.trip_list_container);
        swipeLayout.setRefreshing(false);
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
        TripList.getSharedList().saveToArchive();

        updateListView();
    }

    public void serverCallFailed() {
        //Log.d("TripsActivity", "serverCallFailed");
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.trip_list_container);
        swipeLayout.setRefreshing(false);
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    private void loadTrips(boolean refresh) {
        //Log.d("TripsActivity", "loadTrips");
        if ( ! refresh ) {
            pDialog = new ProgressDialog(TripsActivity.this);
            pDialog.setMessage("Loading SHiT Trips ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        TripList.getSharedList().getFromServer();
        //new ServerAPI(this).execute(URL_TRIPS);
    }

    private void logout() {
        //Log.d("TripsActivity", "logout");
        TripList.getSharedList().clear();
        User.sharedUser.logout();
        //this.recreate();
        updateListView();
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
    }
}