package no.shitt.myshit;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.widget.TextView;


import java.text.DateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import no.shitt.myshit.helper.StringUtil;
import no.shitt.myshit.model.Event;
import no.shitt.myshit.model.TripElement;
import no.shitt.myshit.model.TripList;

public class EventActivity extends TripElementActivity {
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

        // Get trip code and element ID
        intent = getIntent();
        trip_code = intent.getStringExtra(Constants.IntentExtra.TRIP_CODE);
        element_id = intent.getStringExtra(Constants.IntentExtra.ELEMENT_ID);

        new getData().execute();
    }

    private void fillScreen() {
        ((TextView) findViewById(R.id.event_venue)).setText(venueInfo.toString());
        ((TextView) findViewById(R.id.event_start)).setText(event.startTime(null, DateFormat.SHORT));
        ((TextView) findViewById(R.id.event_travel_time)).setText(event.travelTime());
        ((TextView) findViewById(R.id.event_reference)).setText(references.toString());
        ((TextView) findViewById(R.id.event_venue_phone)).setText(StringUtil.stringWithDefault(event.venuePhone, ""));
        ((TextView) findViewById(R.id.event_access_info)).setText(StringUtil.stringWithDefault(event.accessInfo, ""));

        Linkify.addLinks(((TextView) findViewById(R.id.event_venue)), Constants.selectAllButFirstLine, "geo:0,0?q=");
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
                int titleId = getTitleId();
                if (titleId != 0) {
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
            return ctx.getResources().getIdentifier(titleName.toLowerCase(), "string", ctx.getPackageName());
        } else {
            return 0;
        }
    }
}

