package no.shitt.myshit;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import no.shitt.myshit.helper.StringUtil;
import no.shitt.myshit.model.GenericTransport;
import no.shitt.myshit.model.TripList;

public class PrivateTransportActivity extends TripElementActivity {
    private static final String LOG_TAG = PrivateTransportActivity.class.getSimpleName();

    // Trip element info
    String trip_code;
    String element_id;

    private GenericTransport transport;
    private Intent intent;

    StringBuilder departureInfo;
    StringBuilder arrivalInfo;
    String        references;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_transport);

        // Set up toolbar and enable Up button
        Toolbar myToolbar = findViewById(R.id.private_transport_toolbar);
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
                transport = (GenericTransport) TripList.getSharedList().tripByCode(trip_code).trip.elementById(Integer.parseInt(element_id)).tripElement;

                departureInfo = new StringBuilder(StringUtil.stringWithDefault(transport.departureStop, ""));
                StringUtil.appendWithLeadingSeparator(departureInfo, transport.departureTerminalName, "\n", false);
                StringUtil.appendWithLeadingSeparator(departureInfo, transport.departureAddress, "\n", false);
                StringUtil.appendWithLeadingSeparator(departureInfo, transport.departureLocation, "\n", false);

                arrivalInfo = new StringBuilder(StringUtil.stringWithDefault(transport.arrivalStop, ""));
                StringUtil.appendWithLeadingSeparator(arrivalInfo, transport.arrivalTerminalName, "\n", false);
                StringUtil.appendWithLeadingSeparator(arrivalInfo, transport.arrivalAddress, "\n", false);
                StringUtil.appendWithLeadingSeparator(arrivalInfo, transport.arrivalLocation, "\n", false);

                references = transport.getReferences(", ", false);
                fillScreen();
            }
            catch (Exception e) {
                Log.e("PrivTransAct/get", "Unexpected error: " + e);
            }
        } );
    }


    private void fillScreen() {
        runOnUiThread( () -> {
            ((TextView) findViewById(R.id.company)).setText(StringUtil.stringWithDefault(transport.companyName, ""));
            ((TextView) findViewById(R.id.departure)).setText(departureInfo.toString());
            ((TextView) findViewById(R.id.arrival)).setText(arrivalInfo.toString());
            ((TextView) findViewById(R.id.phone)).setText(StringUtil.stringWithDefault(transport.companyPhone, ""));
            ((TextView) findViewById(R.id.reference)).setText(references);
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

}
