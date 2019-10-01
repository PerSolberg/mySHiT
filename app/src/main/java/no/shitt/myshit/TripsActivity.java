package no.shitt.myshit;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import no.shitt.myshit.adapters.TripListAdapter;
import no.shitt.myshit.helper.AlertDialogueManager;
import no.shitt.myshit.helper.ConnectionDetector;
import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.ChangeState;
import no.shitt.myshit.model.TripList;
import no.shitt.myshit.model.User;

public class TripsActivity extends AppCompatActivity {
    private static final String LOG_TAG = TripsActivity.class.getSimpleName();
    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    final AlertDialogueManager alert = new AlertDialogueManager();

    // Progress Dialog
    private ProgressDialog pDialog;

    // List view
    ExpandableListView listView;
    TripListAdapter listAdapter;

    // Trip code to show in details list
    String tripCode = null;

    // Save state keys
    private static final String STATE_TRIP_CODE = "tripCode";
    private final TripsUpdateHandler tripsUpdateHandler = new TripsUpdateHandler();
    private final LogonSuccessHandler logonSuccessHandler = new LogonSuccessHandler();
    private final CommErrorHandler commErrorHandler = new CommErrorHandler();

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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        // Make sure preferences are initialised
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Set up toolbar and enable Up button
        Toolbar myToolbar = (Toolbar) findViewById(R.id.trip_list_toolbar);
        //myToolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(myToolbar);

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
                cd = new ConnectionDetector(getApplicationContext());
                if (!cd.isConnectedToInternet()) {
                    alert.showAlertDialogue(this, getResources().getString(R.string.dlgtitle_network_connection_error),
                            getString(R.string.msg_connect_to_network), false);
                    return;
                }

                loadTrips(false);
            } else {
                updateListView();
            }
        }

        if (savedInstanceState != null) {
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

        LocalBroadcastManager.getInstance(this).registerReceiver(tripsUpdateHandler, new IntentFilter(Constants.Notification.TRIPS_LOADED));
        LocalBroadcastManager.getInstance(this).registerReceiver(logonSuccessHandler, new IntentFilter(Constants.Notification.LOGON_SUCCEEDED));
        LocalBroadcastManager.getInstance(this).registerReceiver(commErrorHandler, new IntentFilter(Constants.Notification.COMMUNICATION_FAILED));
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        //Log.d("TripsActivity", "onRestoreInstanceState (state = " + savedInstanceState + ")");
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
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(tripsUpdateHandler);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(logonSuccessHandler);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(commErrorHandler);

        super.onDestroy();
    }

    private void updateListView() {
        runOnUiThread(new Runnable() {
            public void run() {
                configureShortcuts();
                listAdapter.notifyDataSetChanged();
                //TripListAdapter adapter = new TripListAdapter(TripsActivity.this);
                //listView.setAdapter(adapter);
                listAdapter.applyPreviousCollapse(listView);
            }
        });
    }

    private class TripsUpdateHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            serverCallComplete();
        }
    }

    private class LogonSuccessHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadTrips(false);
        }
    }

    private class CommErrorHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            serverCallFailed(context, intent);
        }
    }


    public void serverCallComplete() {
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.trip_list_container);
        swipeLayout.setRefreshing(false);
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
        TripList.getSharedList().saveToArchive();

        updateListView();
    }

    public void serverCallFailed(Context context, Intent intent) {
        Log.e(LOG_TAG, "Server call failed");
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.trip_list_container);
        swipeLayout.setRefreshing(false);
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }

        String message = intent.getStringExtra("message");
        if (message == null) {
            message = getString(R.string.msg_unknown_network_error);
        }
        alert.showAlertDialogue(this, getString(R.string.dlgtitle_network_connection_error),
                message, false);

    }

    private void loadTrips(boolean refresh) {
        if ( ! refresh ) {
            pDialog = new ProgressDialog(this);
            pDialog.setMessage(getResources().getString(R.string.msg_loading_trips));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        TripList.getSharedList().getFromServer();
    }

    private void logout() {
        TripList.getSharedList().clear();
        User.sharedUser.logout();
        updateListView();
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
    }


    @TargetApi(25)
    private void configureShortcuts() {
        if ( android.os.Build.VERSION.SDK_INT >= 25 ) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            List<ShortcutInfo> shortcuts = new ArrayList<>();

            AnnotatedTrip nextTrip = null, currentTrip = null, lastTrip = null;

            for (AnnotatedTrip aTrip : TripList.getSharedList() ) {
                switch ( aTrip.trip.getTense() ) {
                    case FUTURE:
                        nextTrip = aTrip;
                        break;

                    case PRESENT:
                        currentTrip = aTrip;
                        break;

                    case PAST:
                        lastTrip = lastTrip != null ? lastTrip : aTrip;
                        break;
                }
            }

            if ( lastTrip != null ) {
                shortcuts.add(createShortcut(lastTrip));
            }
            if ( currentTrip != null ) {
                shortcuts.add(createShortcut(currentTrip));
            }
            if ( nextTrip != null ) {
                shortcuts.add(createShortcut(nextTrip));
            }

            shortcutManager.setDynamicShortcuts(shortcuts);
        }
    }

    @TargetApi(25)
    private ShortcutInfo createShortcut(AnnotatedTrip trip) {
        Intent sendMsg = new Intent(this, TripDetailsPopupActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .setAction(Constants.PushNotificationActions.CHATMSG_CLICK)
            .putExtra(Constants.PushNotificationKeys.TRIP_ID, String.valueOf(trip.trip.id))
            .putExtra(Constants.PushNotificationKeys.CHANGE_TYPE, Constants.PushNotificationData.TYPE_CHAT_MESSAGE);

        ArrayList<Object> locArgs = new ArrayList<>();
        locArgs.add(trip.trip.name);
        Object[] locArgsArray = locArgs.toArray();
        String shortLabel = getResources().getString(R.string.shortcut_chatmsg_short_label, locArgsArray);
        String longLabel = getResources().getString(R.string.shortcut_chatmsg_long_label, locArgsArray);
        return new ShortcutInfo.Builder(getApplicationContext(), trip.trip.code)
                .setShortLabel(shortLabel)
                .setLongLabel(longLabel)
                .setIcon(Icon.createWithResource(getApplicationContext(), R.mipmap.icon_chat))
                .setIntent(sendMsg)
                .build();
    }
}