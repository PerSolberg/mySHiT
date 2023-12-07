package no.shitt.myshit;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import no.shitt.myshit.adapters.ReferenceListAdapter;
import no.shitt.myshit.helper.StringUtil;
import no.shitt.myshit.model.Flight;
import no.shitt.myshit.model.TripList;


public class FlightActivity extends TripElementActivity /*AppCompatActivity*/ {
    private static final String LOG_TAG = FlightActivity.class.getSimpleName();

    // Trip element info
    String trip_code;
    String element_id;

    Flight flight;
    Intent intent;
    ListView refListView;

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
                    .build());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight);

        // Set up toolbar and enable Up button
        Toolbar myToolbar = findViewById(R.id.flight_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        } else {
            Log.e(LOG_TAG, "Cannot find action bar");
        }

        refListView = findViewById(R.id.reference_list);

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
                flight = (Flight) TripList.getSharedList().tripByCode(trip_code).trip.elementById(Integer.parseInt(element_id)).tripElement;

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

                fillScreen(flightNo, departureInfo.toString(), arrivalInfo.toString());
            }
            catch (Exception e) {
                Log.e(LOG_TAG, "Unexpected error: " + e);
            }

        } );
    }


    private void fillScreen(String flightNo, String departureInfo, String arrivalInfo) {
        runOnUiThread( () -> {
            ((TextView) findViewById(R.id.flightNo)).setText(flightNo);
            ((TextView) findViewById(R.id.airline)).setText(flight.companyName);
            ((TextView) findViewById(R.id.departure)).setText(departureInfo);
            ((TextView) findViewById(R.id.arrival)).setText(arrivalInfo);

            Linkify.addLinks(((TextView) findViewById(R.id.departure)), Constants.selectAllButFirstLine, "geo:0,0?q=");
            Linkify.addLinks(((TextView) findViewById(R.id.arrival)), Constants.selectAllButFirstLine, "geo:0,0?q=");

            ListAdapter adapter = new ReferenceListAdapter(FlightActivity.this, flight.references);
            refListView.setAdapter(adapter);
        });
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
}
