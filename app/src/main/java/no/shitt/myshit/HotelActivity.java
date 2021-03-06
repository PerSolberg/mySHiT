package no.shitt.myshit;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.DateFormat;

import no.shitt.myshit.helper.StringUtil;
import no.shitt.myshit.model.Hotel;
import no.shitt.myshit.model.TripList;


public class HotelActivity extends TripElementActivity /*AppCompatActivity*/ {
    private String trip_code;
    String element_id;

    private Hotel hotel;
    private Intent intent;

    StringBuilder hotelInfo;
    String        references;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel);

        // Set up toolbar and enable Up button
        Toolbar myToolbar = (Toolbar) findViewById(R.id.hotel_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        try {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        catch (NullPointerException npe) {
            //Log.e("Hotel", "Unexpected NullPointerException when setting up toolbar");
        }

        // Get trip code and element ID
        intent = getIntent();
        trip_code = intent.getStringExtra(Constants.IntentExtra.TRIP_CODE);
        element_id = intent.getStringExtra(Constants.IntentExtra.ELEMENT_ID);

        new getData().execute();
    }

    private void fillScreen() {
        ((TextView) findViewById(R.id.hotel)).setText(hotelInfo.toString());
        ((TextView) findViewById(R.id.hotel_checkin)).setText(hotel.startTime(DateFormat.MEDIUM, null));
        ((TextView) findViewById(R.id.hotel_checkout)).setText(hotel.endTime(DateFormat.MEDIUM, null));
        ((TextView) findViewById(R.id.hotel_reference)).setText(references);
        ((TextView) findViewById(R.id.hotel_phone)).setText(StringUtil.stringWithDefault(hotel.phone, ""));
        ((TextView) findViewById(R.id.hotel_transfer_info)).setText(StringUtil.stringWithDefault(hotel.transferInfo, ""));

        Linkify.addLinks(((TextView) findViewById(R.id.hotel)), Constants.selectAllButFirstLine, "geo:0,0?q=");
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
                hotel = (Hotel) TripList.getSharedList().tripByCode(trip_code).trip.elementById(Integer.valueOf(element_id)).tripElement;

                hotelInfo = new StringBuilder(StringUtil.stringWithDefault(hotel.hotelName, ""));
                StringUtil.appendWithLeadingSeparator(hotelInfo, hotel.address, "\n", false);
                StringUtil.appendWithLeadingSeparator(hotelInfo, hotel.postCode, "\n", false);
                StringUtil.appendWithLeadingSeparator(hotelInfo, hotel.city, "\n", false);

                references = hotel.getReferences(", ", false);
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
}

