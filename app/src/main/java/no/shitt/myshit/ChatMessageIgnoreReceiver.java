package no.shitt.myshit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by persolberg on 2018-02-01.
 */

public class ChatMessageIgnoreReceiver extends BroadcastReceiver {
    private static final String TAG = ChatMessageIgnoreReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constants.Intent.CHATMSG_IGNORE.equals(intent.getAction())) {
            int tripId = intent.getIntExtra(Constants.IntentExtra.TRIP_ID, -1);
            int messageId = intent.getIntExtra(Constants.IntentExtra.MESSAGE_ID, -1);

            if (tripId != -1 && messageId != -1) {
                Log.d(TAG, "Ignoring message for TripId " + tripId);

                // Update the notification to stop the progress spinner.
                NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(context);
                notificationManager.cancel(messageId);
            }
        }
    }
}

