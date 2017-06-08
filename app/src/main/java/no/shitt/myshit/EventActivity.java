package no.shitt.myshit;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
//import android.view.Menu;
import android.view.MenuItem;
//import android.view.View;
import android.widget.TextView;

//import android.text.format.DateUtils;
import android.util.Log;

import java.text.DateFormat;
import java.util.Iterator;
import java.util.Map;

import no.shitt.myshit.helper.StringUtil;
import no.shitt.myshit.model.Event;
import no.shitt.myshit.model.TripElement;
import no.shitt.myshit.model.TripList;

public class EventActivity extends AppCompatActivity {
    String trip_code;
    String element_id;

    private Event event;
    private Intent intent;

    StringBuilder venueInfo;
    StringBuilder references;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        // Set up toolbar and enable Up button
        Toolbar myToolbar = (Toolbar) findViewById(R.id.event_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        try {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        catch (NullPointerException npe) {
            //Log.e("EventActivity", "Unexpected NullPointerException when setting up toolbar");
        }

        /* From generated code, probably don't need
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        // Get trip code and element ID
        intent = getIntent();
        trip_code = intent.getStringExtra(Constants.IntentExtra.TRIP_CODE);
        element_id = intent.getStringExtra(Constants.IntentExtra.ELEMENT_ID);

        //Log.d("Event", "Invoking background service");
        new getData().execute();
    }

    private void fillScreen() {
        ((TextView) findViewById(R.id.event_venue)).setText(venueInfo.toString());
        ((TextView) findViewById(R.id.event_start)).setText(event.startTime(null, DateFormat.SHORT));
        ((TextView) findViewById(R.id.event_travel_time)).setText(event.travelTime());
        ((TextView) findViewById(R.id.event_reference)).setText(references.toString());
        ((TextView) findViewById(R.id.event_venue_phone)).setText(StringUtil.stringWithDefault(event.venuePhone, ""));
        ((TextView) findViewById(R.id.event_access_info)).setText(StringUtil.stringWithDefault(event.accessInfo, ""));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class getData extends AsyncTask<String,String,String> {
        protected String doInBackground(String... params) {
            try {
                event = (Event) TripList.getSharedList().tripByCode(trip_code).trip.elementById(Integer.valueOf(element_id)).tripElement;

                venueInfo = new StringBuilder(StringUtil.stringWithDefault(event. venueName, ""));
                StringUtil.appendWithLeadingSeparator(venueInfo, event.venueAddress, "\n", false);
                StringUtil.appendWithLeadingSeparator(venueInfo, event.venuePostCode, "\n", false);
                StringUtil.appendWithLeadingSeparator(venueInfo, event.venueCity, "\n", false);

                references = new StringBuilder();
                Iterator i = event.references.iterator();
                String sep = "";
                while (i.hasNext()) {
                    Map<String,String> refMap = (Map<String,String>) i.next();
                    String ref = refMap.get(TripElement.REFTAG_REF_NO);
                    StringUtil.appendWithLeadingSeparator(references, ref, sep, false);
                    sep = ", ";
                }

                // Set title
                //Toolbar myToolbar = (Toolbar) findViewById(R.id.event_toolbar);
                //setSupportActionBar(myToolbar);
                ActionBar ab = getSupportActionBar();
                int titleId = getTitleId();
                if (titleId != 0) {
                    //Log.d("EventActivity", "Setting title id " + Integer.toString(titleId));
                    //ab.setTitle(titleId);
                    setTitle(titleId);
                }
            }
            catch (Exception e) {
                //Log.e("Hotel/get", "Unexpected error: " + e.toString());
            }
            return null;
        }

        protected void onPostExecute(String result) {
            fillScreen();
        }
    }

    private int getTitleId() {
        if (event != null) {
            Context ctx = SHiTApplication.getContext();
            String titleBaseName = "title_event_details";
            String titleName;

            titleName = titleBaseName + "_" + event.subType;
            int titleId = ctx.getResources().getIdentifier(titleName.toLowerCase(), "string", ctx.getPackageName());
            //Log.d("EventActivity", "Checking string resource '" + titleName + "', id = " + Integer.toString(titleId));
            return titleId;
        } else {
            return 0;
        }
    }
}

