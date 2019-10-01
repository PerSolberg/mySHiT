package no.shitt.myshit;

import android.os.Bundle;
//import android.util.Log;
import android.view.MenuItem;

/**
 * Created by Per Solberg on 2018-02-07.
 */

public class ScheduledTransportPopupActivity extends ScheduledTransportActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.d("ScheduledTransportPopup", "Launching ScheduledTransport as pop-up");
        super.onCreate(savedInstanceState);
        cancelAlert();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
