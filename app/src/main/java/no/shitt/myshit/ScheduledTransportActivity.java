package no.shitt.myshit;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.DateFormat;

import no.shitt.myshit.helper.StringUtil;
import no.shitt.myshit.model.ScheduledTransport;
import no.shitt.myshit.model.TripList;

public class ScheduledTransportActivity extends TripElementActivity /*AppCompatActivity*/ {
    // Trip element info
    String trip_code;
    String element_id;

    private ScheduledTransport transport;
    private Intent intent;

    StringBuilder departureInfo;
    StringBuilder arrivalInfo;
    String        references;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduled_transport);

        // Set up toolbar and enable Up button
        Toolbar myToolbar = (Toolbar) findViewById(R.id.scheduled_transport_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        try {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        catch (NullPointerException npe) {
            //Log.e("SchedTransportAct", "Unexpected NullPointerException when setting up toolbar");
        }

        // Get trip code and element ID
        intent = getIntent();
        trip_code = intent.getStringExtra(Constants.IntentExtra.TRIP_CODE);
        element_id = intent.getStringExtra(Constants.IntentExtra.ELEMENT_ID);

        new getData().execute();
    }


    private void fillScreen() {
        ((TextView) findViewById(R.id.scheduled_transport_company)).setText(StringUtil.stringWithDefault(transport.companyName, ""));
        ((TextView) findViewById(R.id.scheduled_transport_route_no)).setText(StringUtil.stringWithDefault(transport.routeNo, ""));
        ((TextView) findViewById(R.id.scheduled_transport_departure)).setText(departureInfo.toString());
        ((TextView) findViewById(R.id.scheduled_transport_arrival)).setText(arrivalInfo.toString());
        ((TextView) findViewById(R.id.scheduled_transport_phone)).setText(StringUtil.stringWithDefault(transport.companyPhone, ""));
        ((TextView) findViewById(R.id.scheduled_transport_reference)).setText(references);

        Linkify.addLinks(((TextView) findViewById(R.id.scheduled_transport_departure)), Constants.selectAllButFirstLine, "geo:0,0?q=");
        Linkify.addLinks(((TextView) findViewById(R.id.scheduled_transport_arrival)), Constants.selectAllButFirstLine, "geo:0,0?q=");
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

    private class getData extends AsyncTask<String,String,String> {
        protected String doInBackground(String... params) {
            try {
                transport = (ScheduledTransport) TripList.getSharedList().tripByCode(trip_code).trip.elementById(Integer.valueOf(element_id)).tripElement;

                departureInfo = new StringBuilder();
                StringUtil.appendWithLeadingSeparator(departureInfo, transport.startTime(DateFormat.MEDIUM, DateFormat.SHORT), "", false);
                StringUtil.appendWithLeadingSeparator(departureInfo, transport.departureStop, "\n", false);
                StringUtil.appendWithLeadingSeparator(departureInfo, transport.departureTerminalName, "\n", false);
                StringUtil.appendWithLeadingSeparator(departureInfo, transport.departureAddress, "\n", false);
                StringUtil.appendWithLeadingSeparator(departureInfo, transport.departureLocation, "\n", false);

                arrivalInfo = new StringBuilder();
                StringUtil.appendWithLeadingSeparator(arrivalInfo, transport.endTime(DateFormat.MEDIUM, DateFormat.SHORT), "", false);
                StringUtil.appendWithLeadingSeparator(arrivalInfo, transport.arrivalStop, "\n", false);
                StringUtil.appendWithLeadingSeparator(arrivalInfo, transport.arrivalTerminalName, "\n", false);
                StringUtil.appendWithLeadingSeparator(arrivalInfo, transport.arrivalAddress, "\n", false);
                StringUtil.appendWithLeadingSeparator(arrivalInfo, transport.arrivalLocation, "\n", false);

                references = transport.getReferences(", ", false);
            }
            catch (Exception e) {
                Log.e("SchedTransportAct/get", "Unexpected error: " + e.toString());
            }
            return null;
        }

        protected void onPostExecute(String result) {
            fillScreen();
        }
    }
}
