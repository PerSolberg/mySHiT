package no.shitt.myshit;

//import static android.view.View.VISIBLE;

import no.shitt.myshit.adapters.TripPagerAdapter;
import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.TripList;
import no.shitt.myshit.model.User;
import no.shitt.myshit.ui.ChatThreadFragment;
import no.shitt.myshit.ui.TripDetailsFragment;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.Objects;

public class TripDetailsActivity extends AppCompatActivity
        implements TripDetailsFragment.OnFragmentInteractionListener
                 , ChatThreadFragment.OnFragmentInteractionListener
{
    private static final String LOG_TAG = TripDetailsActivity.class.getSimpleName();
    private AnnotatedTrip annotatedTrip;

    String trip_code;

    private static final int[] tabTitles = new int[]{R.string.trip_page_itinerary, R.string.trip_page_messages};

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
            String extTripId = Objects.requireNonNull(extras).getString(Constants.PushNotificationKeys.TRIP_ID);
            int tripId = Integer.parseInt(Objects.requireNonNull(extTripId));
            annotatedTrip = TripList.getSharedList().tripById(tripId);
            trip_code = annotatedTrip.trip.code;
        }

        // Get the ViewPager and set its PagerAdapter so that it can display items
        TripPagerAdapter  tpa = new TripPagerAdapter(this, annotatedTrip.trip.id, trip_code);
        ViewPager2 viewPager = findViewById(R.id.trip_content);
        viewPager.setAdapter(tpa);
        TabLayout tabs = findViewById(R.id.trip_details_tabbar);
        TabLayoutMediator tlm =  new TabLayoutMediator(tabs, viewPager,
                (tab, position) -> tab.setText(SHiTApplication.getContext().getString(tabTitles[position]))
        );
        tlm.attach();

        // Set up toolbar and enable Up button
        Toolbar myToolbar = findViewById(R.id.trip_details_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(annotatedTrip.trip.name);
        } else {
            Log.e(LOG_TAG, "Cannot find action bar");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        } else if (item.getItemId() == R.id.action_refresh) {
            SwipeRefreshLayout swipeLayout = findViewById(R.id.trip_details_list_container);
            swipeLayout.setRefreshing(true);
            loadTripDetails(true);
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            TripList.getSharedList().clear();
            User.sharedUser.logout();
            finish();
            return true;
        } else {
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
            ViewPager2 viewPager = findViewById(R.id.trip_content);
            int currentTab = viewPager.getCurrentItem();
            SwipeRefreshLayout swipeLayout = null;
            if ( currentTab == 0 ) {
                swipeLayout = findViewById(R.id.trip_details_list_container);
            } else if ( currentTab == 1 ) {
                swipeLayout = findViewById(R.id.trip_chat_container);
            } else {
                Log.e(LOG_TAG, "Invalid tab: " + currentTab);
            }
            if (swipeLayout != null) {
                swipeLayout.setRefreshing(true);
            }
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
