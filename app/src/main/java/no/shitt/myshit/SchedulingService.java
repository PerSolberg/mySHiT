package no.shitt.myshit;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.AnnotatedTripElement;
import no.shitt.myshit.model.Trip;
import no.shitt.myshit.model.TripElement;
import no.shitt.myshit.model.TripList;

/**
 * This {@code IntentService} does the app's actual work.
 * {@code SampleAlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */

public class SchedulingService extends IntentService {
    // An ID used to post the notification.
    private static final int NOTIFICATION_ID = 1;
    private static final long[] VIBRATION_PATTERN = new long[] { 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50 };

    // Keys for intent extras (bundle keys)
    public static final String KEY_TRIP_CODE  = "tripCode";
    public static final String KEY_ELEMENT_ID = "tripElement";
    public static final String KEY_TITLE      = "title";
    public static final String KEY_MESSAGE    = "msg";


    public SchedulingService() {
        super("SchedulingService");
    }


    private NotificationManager mNotificationManager;

    @Override
    protected void onHandleIntent(Intent intent) {
        //Log.d("SchedulingService", "Action: " + intent.getAction() + ", Data: " + intent.getData());
        sendNotification(intent);

        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);
    }

    // Post a notification
    private void sendNotification(Intent intent) {
        Bundle extras = intent.getExtras();
        String tag = intent.getData().toString();
        String actionName = intent.getAction();

        String tripCode = extras.getString(KEY_TRIP_CODE);
        boolean hasElementId = extras.containsKey(KEY_ELEMENT_ID);
        int elementId = extras.getInt(KEY_ELEMENT_ID);

        AnnotatedTrip trip = TripList.getSharedList().tripByCode(tripCode);
        if (trip == null) {
            // If trip not found, it's probably a notification for a deleted trip - ignore
            return;
        }
        AnnotatedTripElement tripElement = null;
        if (hasElementId) {
            tripElement = trip.trip.elementById(elementId);
            if (tripElement == null) {
                // If element not found, it's probably a notification for a deleted element - ignore
                return;
            }
        }

        String title = extras.getString(KEY_TITLE);
        String msg   = extras.getString(KEY_MESSAGE);

        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(SHiTApplication.getContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(largeIcon)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setVibrate(VIBRATION_PATTERN)
                        .setSound(alarmSound)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setCategory(Notification.CATEGORY_EVENT)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg);

        int notificationId = (hasElementId ? elementId : trip.trip.id);
        if (actionName != null) {
            Intent elementIntent;
            if (tripElement == null) {
                elementIntent = trip.trip.getActivityIntent(Trip.ActivityType.POPUP);
            } else {
                elementIntent = tripElement.tripElement.getActivityIntent(TripElement.ActivityType.POPUP);
            }
            elementIntent.setAction(actionName);
            elementIntent.putExtra(Constants.IntentExtra.NOTIFICATION_TAG, tag);

            PendingIntent clickIntent = PendingIntent.getActivity(SHiTApplication.getContext()
                    , notificationId // NOTIFICATION_ID /*elementId*/
                    , elementIntent
                    , PendingIntent.FLAG_UPDATE_CURRENT
                    , extras);
            mBuilder.setContentIntent(clickIntent);
        }
        Notification ntf = mBuilder.build();
        mNotificationManager.notify(tag, notificationId /*NOTIFICATION_ID*/, ntf);
    }
}
