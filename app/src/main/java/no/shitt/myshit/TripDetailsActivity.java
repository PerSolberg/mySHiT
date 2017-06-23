package no.shitt.myshit;

import no.shitt.myshit.adapters.TripPagerAdapter;
import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.ChatMessage;
import no.shitt.myshit.model.TripList;
import no.shitt.myshit.model.User;
import no.shitt.myshit.ui.ChatThreadFragment;
import no.shitt.myshit.ui.TripDetailsFragment;

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

public class TripDetailsActivity extends AppCompatActivity
        implements TripDetailsFragment.OnFragmentInteractionListener
                 , ChatThreadFragment.OnFragmentInteractionListener
{
    //ConnectionDetector cd;
    //AlertDialogueManager alert = new AlertDialogueManager();
    private ProgressDialog pDialog;

    private AnnotatedTrip annotatedTrip;

    String trip_code;
    //String trip_name;

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
                    //.penaltyDeath()
                    .build());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        // Get trip code
        Intent i = getIntent();
        trip_code = i.getStringExtra(Constants.IntentExtra.TRIP_CODE);

        annotatedTrip = TripList.getSharedList().tripByCode(trip_code);

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
        //myToolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(annotatedTrip.trip.name);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.trip_details_tabbar);
        tabLayout.setupWithViewPager(viewPager);
    }

    /*
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

        // get list view
        //ListView lv = getListView();
        listView = (ExpandableListView) findViewById(R.id.trip_details_list);

        /**
         * List view on item click listener
         * * /
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
                } else if ("TRA".equals(element.type) && "TRN".equals(element.subType)) {
                    i = new Intent(getApplicationContext(), ScheduledTransportActivity.class);
                } else if ("TRA".equals(element.type) && "BOAT".equals(element.subType)) {
                    i = new Intent(getApplicationContext(), ScheduledTransportActivity.class);
                } else if ("ACM".equals(element.type) && "HTL".equals(element.subType)) {
                    i = new Intent(getApplicationContext(), HotelActivity.class);
                } else if ("EVT".equals(element.type)) {
                    i = new Intent(getApplicationContext(), EventActivity.class);
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
        LocalBroadcastManager.getInstance(this).registerReceiver(new HandleNotification(), new IntentFilter(Constants.Notification.TRIPS_LOADED));
    }
    */

    /*
    private class HandleNotification extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.Notification.TRIP_DETAILS_LOADED)) {
                serverCallComplete();
            } else if (intent.getAction().equals(Constants.Notification.TRIPS_LOADED)) {
                annotatedTrip = TripList.getSharedList().tripByCode(trip_code);
                serverCallComplete();
            } else if (intent.getAction().equals(Constants.Notification.COMMUNICATION_FAILED)) {
                serverCallFailed();
            }
        }
    }
    */

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

    @Override
    public void onPause() {
        TripList.getSharedList().saveToArchive();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateListView();
    }


    private void updateListView() {
        runOnUiThread(new Runnable() {
            public void run() {
                // Moved to fragment
                /*
                TripElementListAdapter adapter = new TripElementListAdapter(TripDetailsActivity.this, annotatedTrip);
                //setListAdapter(adapter);
                listView.setAdapter(adapter);
                adapter.applyDefaultCollapse(listView);
                */
            }
        });
    }

    /*
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
    */

    private void loadTripDetails(boolean refresh) {
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

    public void sendMessage(View v) {
        Log.d("TripDetailsActivity", "Send message icon clicked");
        EditText textField = (EditText) findViewById(R.id.chatmsg_entry);
        String msgText = textField.getText().toString();
        ChatMessage msg = new ChatMessage(msgText);

        annotatedTrip.trip.chatThread.append(msg);
        textField.getText().clear();
        controlSendButton();

        // Hide keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0 /*InputMethodManager.HIDE_IMPLICIT_ONLY*/);
        }
    }

    public void controlSendButton() {
        EditText textField = (EditText) findViewById(R.id.chatmsg_entry);
        ImageButton button = (ImageButton) findViewById(R.id.chat_send_button);
        if (textField != null && button != null) {
            boolean enabled = textField.getText().length() != 0;
            button.setEnabled(enabled);

            //Drawable originalIcon = SHiTApplication.getContext().getResources().getDrawable(R.mipmap.icon_chat);
            Drawable originalIcon = SHiTApplication.getContext().getDrawable(R.mipmap.icon_chat);
            Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
            button.setImageDrawable(icon);
        }
    }

    private static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        return res;
    }
}
