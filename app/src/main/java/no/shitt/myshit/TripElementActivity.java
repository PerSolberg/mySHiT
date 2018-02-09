package no.shitt.myshit;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by persolberg on 2018-02-07.
 */

public class TripElementActivity extends AppCompatActivity {
    protected void cancelAlert() {
        String strElementId = getIntent().getStringExtra(Constants.IntentExtra.ELEMENT_ID);
        String notificationTag = getIntent().getStringExtra(Constants.IntentExtra.NOTIFICATION_TAG);
        if (strElementId != null && notificationTag != null) {
            int elementId = Integer.parseInt(strElementId);
            NotificationManager notificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationTag, elementId);
        }
    }
}
