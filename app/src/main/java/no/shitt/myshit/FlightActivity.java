package no.shitt.myshit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;

import no.shitt.myshit.adapters.ReferenceListAdapter;
import no.shitt.myshit.adapters.TripElementListAdapter;
import no.shitt.myshit.helper.StringUtil;
import no.shitt.myshit.model.Flight;
import no.shitt.myshit.model.TripList;


public class FlightActivity extends AppCompatActivity {

    // Trip element info
    String trip_code;
    String element_id;

    Flight flight;
    Intent intent;
    ListView refListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight);

        // Set up toolbar and enable Up button
        Toolbar myToolbar = (Toolbar) findViewById(R.id.flight_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        try {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        catch (NullPointerException npe) {
            Log.e("FlightActivity", "Unexpected NullPointerException when setting up toolbar");
        }

        refListView = (ListView) findViewById(R.id.reference_list);

        // Get trip code and element ID
        intent = getIntent();
        trip_code = intent.getStringExtra(Constants.IntentExtra.TRIP_CODE);
        element_id = intent.getStringExtra(Constants.IntentExtra.ELEMENT_ID);

        // calling background thread
        //new LoadSingleTrack().execute();
        try {
            flight = (Flight) TripList.getSharedList().tripByCode(trip_code).trip.elementById(Integer.valueOf(element_id)).tripElement;

            String flightNo = StringUtil.stringWithDefault(flight.airlineCode, "XX") + " " + StringUtil.stringWithDefault(flight.routeNo, "***");
            StringBuilder departureInfo = new StringBuilder(flight.startTime(DateFormat.MEDIUM, DateFormat.SHORT));
            departureInfo.append("\n");
            departureInfo.append(StringUtil.stringWithDefault(flight.departureStop, StringUtil.stringWithDefault(flight.departureLocation, "")));
            if (flight.departureTerminalName != null && !flight.departureTerminalName.isEmpty()) {
                departureInfo.append("\n");
                departureInfo.append(flight.departureTerminalName);
            }
            if (flight.departureAddress != null && !flight.departureAddress.isEmpty()) {
                departureInfo.append("\n");
                departureInfo.append(flight.departureAddress);
            }
            StringBuilder arrivalInfo = new StringBuilder(flight.endTime(DateFormat.MEDIUM, DateFormat.SHORT));
            arrivalInfo.append("\n");
            arrivalInfo.append(StringUtil.stringWithDefault(flight.arrivalStop, StringUtil.stringWithDefault(flight.arrivalLocation, "")));
            if (flight.arrivalTerminalName != null && !flight.arrivalTerminalName.isEmpty()) {
                arrivalInfo.append("\n");
                arrivalInfo.append(flight.arrivalTerminalName);
            }
            if (flight.arrivalAddress != null && !flight.arrivalAddress.isEmpty()) {
                arrivalInfo.append("\n");
                arrivalInfo.append(flight.arrivalAddress);
            }

            ((TextView) findViewById(R.id.flightNo)).setText(flightNo);
            ((TextView) findViewById(R.id.airline)).setText(flight.companyName);
            ((TextView) findViewById(R.id.departure)).setText(departureInfo.toString());
            ((TextView) findViewById(R.id.arrival)).setText(arrivalInfo.toString());
            //((TextView) findViewById(R.id.referenceTitle)).setText();
            runOnUiThread(new Runnable() {
                public void run() {
                    ListAdapter adapter = new ReferenceListAdapter(FlightActivity.this, flight.references);
                    //setListAdapter(adapter);
                    refListView.setAdapter(adapter);
                }
            });

        }
        catch (Exception e) {
            Log.e("FlightActivity", "Unexpected error: " + e.toString());
        }

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
}
