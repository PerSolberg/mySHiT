package no.shitt.myshit;

import no.shitt.myshit.adapters.TripElementListAdapter;
import no.shitt.myshit.helper.AlertDialogueManager;
import no.shitt.myshit.helper.ConnectionDetector;
import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.AnnotatedTripElement;
import no.shitt.myshit.model.ChangeState;
import no.shitt.myshit.model.TripElement;
import no.shitt.myshit.model.TripList;
import no.shitt.myshit.model.User;

//import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class TripDetailsActivity extends AppCompatActivity {
    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogueManager alert = new AlertDialogueManager();

    // List view
    ExpandableListView listView;

    // Progress Dialog
    private ProgressDialog pDialog;

    private AnnotatedTrip annotatedTrip;

    // Trip ids
    String trip_code;
    String trip_name;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        // Set up toolbar and enable Up button
        Toolbar myToolbar = (Toolbar) findViewById(R.id.trip_details_toolbar);
        //myToolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        // Get trip code
        Intent i = getIntent();
        trip_code = i.getStringExtra(Constants.IntentExtra.TRIP_CODE);

        annotatedTrip = TripList.getSharedList().tripByCode(trip_code);
        ab.setTitle(annotatedTrip.trip.name);

        // get listview
        //ListView lv = getListView();
        listView = (ExpandableListView) findViewById(R.id.trip_details_list);

        /**
         * Listview on item click listener
         * */
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id)
            {
                String trip_code = ((TextView) view.findViewById(R.id.element_trip_code)).getText().toString();
                String element_id = ((TextView) view.findViewById(R.id.element_id)).getText().toString();
                AnnotatedTripElement annotatedElement = TripList.getSharedList().tripByCode(trip_code).trip.elementById(Integer.valueOf(element_id));
                TripElement element = annotatedElement.tripElement;

                // Reset modification flag when user views data
                if (annotatedElement.modified != ChangeState.UNCHANGED) {
                    annotatedElement.modified = ChangeState.UNCHANGED;
                    TripList.getSharedList().saveToArchive();
                    updateListView();
                }

                Intent i;
                if ("TRA".equals(element.type) && "AIR".equals(element.subType)) {
                    i = new Intent(getApplicationContext(), FlightActivity.class);
                } else if ("TRA".equals(element.type) && "PBUS".equals(element.subType)) {
                    i = new Intent(getApplicationContext(), PrivateTransportActivity.class);
                } else if ("TRA".equals(element.type) && "LIMO".equals(element.subType)) {
                    i = new Intent(getApplicationContext(), PrivateTransportActivity.class);
                } else if ("TRA".equals(element.type) && "BUS".equals(element.subType)) {
                    i = new Intent(getApplicationContext(), ScheduledTransportActivity.class);
                } else if ("ACM".equals(element.type) && "HTL".equals(element.subType)) {
                    i = new Intent(getApplicationContext(), HotelActivity.class);
                } else {
                    //Log.e("TripDetailsActivity", "ChildItemClick: Unsupported element type");
                    return false;
                }

                // Pass trip id and element id to details view
                i.putExtra(Constants.IntentExtra.TRIP_CODE, trip_code);
                i.putExtra(Constants.IntentExtra.ELEMENT_ID, element_id);

                startActivity(i);
                return true;
            }
        });

        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.trip_details_list_container);
        swipeLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        //Log.i("TripDetailsActivity", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        loadTripDetails(true);
                    }
                }
        );

        if (annotatedTrip == null) {
            //Log.e("TripDetailsActivity", "Invalid trip!");
        } else if (annotatedTrip.trip.elementCount() == 0) {
            // Check if Internet present
            cd = new ConnectionDetector(getApplicationContext());
            if (!cd.isConnectingToInternet()) {
                // Internet Connection is not present
                alert.showAlertDialogue(TripDetailsActivity.this, "Internet Connection Error",
                        "Please connect to working Internet connection", false);
                // stop executing code by return
            } else {
                loadTripDetails(false);
            }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                //Log.d("TripDetailsActivity", "Opening settings screen");
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;

            case R.id.action_refresh:
                //Log.d("TripDetailsActivity", "Refreshing from menu");
                SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.trip_details_list_container);
                swipeLayout.setRefreshing(true);
                loadTripDetails(true);
                return true;


            case R.id.action_logout:
                // Log out user and clear list
                TripList.getSharedList().clear();
                User.sharedUser.logout();
                finish();
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
                TripElementListAdapter adapter = new TripElementListAdapter(TripDetailsActivity.this, annotatedTrip);
                //setListAdapter(adapter);
                listView.setAdapter(adapter);
                adapter.applyDefaultCollapse(listView);
            }
        });
    }

    public void serverCallComplete() {
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.trip_details_list_container);
        swipeLayout.setRefreshing(false);
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
        //Log.d("TripDetailsActivity", "Server call succeeded");
        TripList.getSharedList().saveToArchive();

        updateListView();
    }

    public void serverCallFailed() {
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.trip_details_list_container);
        swipeLayout.setRefreshing(false);
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
        //Log.d("TripDetailsActivity", "Server REST call failed.");
    }


    private void loadTripDetails(boolean refresh) {
        if ( ! refresh ) {
            pDialog = new ProgressDialog(TripDetailsActivity.this);
            pDialog.setMessage("Loading trip details ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        annotatedTrip.trip.loadDetails();
    }

}
