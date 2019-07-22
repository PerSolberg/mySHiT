package no.shitt.myshit;

import no.shitt.myshit.adapters.TripPagerAdapter;
import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.ChatMessage;
import no.shitt.myshit.model.TripList;
import no.shitt.myshit.model.User;
import no.shitt.myshit.ui.ChatThreadFragment;
import no.shitt.myshit.ui.TripDetailsFragment;

import android.app.NotificationManager;
import android.app.ProgressDialog;
//import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class TripDetailsActivity extends AppCompatActivity
        implements TripDetailsFragment.OnFragmentInteractionListener
                 , ChatThreadFragment.OnFragmentInteractionListener
{
    private ProgressDialog pDialog;

    private AnnotatedTrip annotatedTrip;

    String trip_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                    .build());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        // Get trip code
        Intent i = getIntent();
        String intentAction = i.getAction();
        if (intentAction == null) {
            trip_code = i.getStringExtra(Constants.IntentExtra.TRIP_CODE);
            annotatedTrip = TripList.getSharedList().tripByCode(trip_code);
        } else {
            Bundle extras = i.getExtras();
            String extTripId = extras.getString(Constants.PushNotificationKeys.TRIP_ID);
            int tripId = Integer.parseInt(extTripId);
            annotatedTrip = TripList.getSharedList().tripById(tripId);
            trip_code = annotatedTrip.trip.code;
        }

        // Get the ViewPager and set its PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.trip_content);
        viewPager.setAdapter(new TripPagerAdapter(getSupportFragmentManager(),
                TripDetailsActivity.this, annotatedTrip.trip.id, trip_code));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                View view = getCurrentFocus(); //getView().getRootView();
                if (view != null) {
                    Log.d("TripDetailsActivity", "Closing keyboard");
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                } else {
                    Log.d("TripDetailsActivity", "Unable to close keyboard");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Set up toolbar and enable Up button
        Toolbar myToolbar = (Toolbar) findViewById(R.id.trip_details_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(annotatedTrip.trip.name);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.trip_details_tabbar);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;

            case R.id.action_refresh:
                SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.trip_details_list_container);
                swipeLayout.setRefreshing(true);
                loadTripDetails(true);
                return true;


            case R.id.action_logout:
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

    @Override
    public void onPause() {
        TripList.getSharedList().saveToArchive();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        //updateListView();
    }


    protected void loadTripDetails(boolean refresh) {
        if ( ! refresh ) {
            pDialog = new ProgressDialog(/*TripDetailsActivity.*/this);
            pDialog.setMessage("Loading trip details ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        annotatedTrip.trip.loadDetails();
    }

    public void onFragmentInteraction(Uri uri) {
        Log.d("TripDetailsActivity", "Trip Details fragment interaction detected");
    }

    public void onChatFragmentInteraction(Uri uri) {
        Log.d("TripDetailsActivity", "Chat fragment interaction detected");
    }

    protected void cancelAlert() {
        String strTripId = getIntent().getStringExtra(Constants.IntentExtra.TRIP_ID);
        String notificationTag = getIntent().getStringExtra(Constants.IntentExtra.NOTIFICATION_TAG);
        if (strTripId != null && notificationTag != null) {
            int tripId = Integer.parseInt(strTripId);
            NotificationManager notificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationTag, tripId);
        }
    }
}
