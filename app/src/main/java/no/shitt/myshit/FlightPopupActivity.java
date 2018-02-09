package no.shitt.myshit;

import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.MenuItem;

/**
 * Created by persolberg on 2018-02-07.
 */

public class FlightPopupActivity extends FlightActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("FlightPopup", "Launching Flight as pop-up");
        super.onCreate(savedInstanceState);
        cancelAlert();
        /*
        int elementId = getIntent().getIntExtra(Constants.IntentExtra.ELEMENT_ID, -1);
        if (elementId != -1) {
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(SHiTApplication.getContext());
            notificationManager.cancel(elementId);
        }
        */
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
