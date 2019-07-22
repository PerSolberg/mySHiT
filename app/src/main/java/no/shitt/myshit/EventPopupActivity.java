package no.shitt.myshit;


import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.MenuItem;


/**
 * Created by persolberg on 2018-02-06.
 */

public class EventPopupActivity extends EventActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("EventPopup", "Launching Event as pop-up");
        super.onCreate(savedInstanceState);
        cancelAlert();
        /*
        String strElementId = getIntent().getStringExtra(Constants.IntentExtra.ELEMENT_ID);
        String notificationTag = getIntent().getStringExtra(Constants.IntentExtra.NOTIFICATION_TAG);
        if (strElementId != null && notificationTag != null) {
            int elementId = Integer.parseInt(strElementId);
            NotificationManager notificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationTag, elementId);
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
