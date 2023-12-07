package no.shitt.myshit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import no.shitt.myshit.helper.StringUtil;
import no.shitt.myshit.model.Event;
import no.shitt.myshit.model.TripList;

public class EventActivity extends TripElementActivity {
    private static final String LOG_TAG = FlightActivity.class.getSimpleName();

    String trip_code;
    String element_id;

    private Event event;
    private Intent intent;

    StringBuilder venueInfo;
    String        references;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        // Set up toolbar and enable Up button
        Toolbar myToolbar = findViewById(R.id.event_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        } else {
            Log.e(LOG_TAG, "Cannot find action bar");
        }

        // Get trip code and element ID
        intent = getIntent();
        trip_code = intent.getStringExtra(Constants.IntentExtra.TRIP_CODE);
        element_id = intent.getStringExtra(Constants.IntentExtra.ELEMENT_ID);

        getData();
    }


    private void getData() {
        Executor background = Executors.newSingleThreadExecutor();
        background.execute( () -> {
            try {
                event = (Event) TripList.getSharedList().tripByCode(trip_code).trip.elementById(Integer.parseInt(element_id)).tripElement;

                venueInfo = new StringBuilder(StringUtil.stringWithDefault(event. venueName, ""));
                StringUtil.appendWithLeadingSeparator(venueInfo, event.venueAddress, "\n", false);
                StringUtil.appendWithLeadingSeparator(venueInfo, event.venuePostCode, "\n", false);
                StringUtil.appendWithLeadingSeparator(venueInfo, event.venueCity, "\n", false);

                references = event.getReferences(", ", false);

                // Set title
                int titleId = getTitleId();
                if (titleId != 0) {
                    setTitle(titleId);
                }
                fillScreen();
            }
            catch (Exception e) {
                Log.e("Event/get", "Unexpected error: " + e);
            }
        } );
    }


    private void fillScreen() {
        runOnUiThread( () -> {
            ((TextView) findViewById(R.id.event_venue)).setText(venueInfo.toString());
            ((TextView) findViewById(R.id.event_start)).setText(event.startTime(null, DateFormat.SHORT));
            ((TextView) findViewById(R.id.event_travel_time)).setText(event.travelTime());
            ((TextView) findViewById(R.id.event_reference)).setText(references);
            ((TextView) findViewById(R.id.event_venue_phone)).setText(StringUtil.stringWithDefault(event.venuePhone, ""));
            ((TextView) findViewById(R.id.event_access_info)).setText(StringUtil.stringWithDefault(event.accessInfo, ""));

            Linkify.addLinks(((TextView) findViewById(R.id.event_venue)), Constants.selectAllButFirstLine, "geo:0,0?q=");
        } );
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("DiscouragedApi")
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

