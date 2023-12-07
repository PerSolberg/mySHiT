package no.shitt.myshit;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.util.Log;

import java.util.Date;
import java.util.Objects;

import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.AnnotatedTripElement;
import no.shitt.myshit.model.Trip;
import no.shitt.myshit.model.TripElement;
import no.shitt.myshit.model.TripList;

/**
 * When the alarm fires, this WakefulBroadcastReceiver receives the broadcast Intent
 * and then starts the IntentService {@code SampleSchedulingService} to do some work.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "Broadcast received");
        /* Ensure intent extras are passed along to AlarmIntentService */
        /* Toasts are not good enough */
        //AlarmIntentService.enqueueWork(context, intent);

        sendNotification(context, intent);
    }

    private void sendNotification(Context context, Intent intent) {
        Bundle extras = Objects.requireNonNull(intent.getExtras());
        String tag = Objects.requireNonNull(intent.getData()).toString();
        String actionName = intent.getAction();

        String tripCode = extras.getString(Constants.IntentExtra.TRIP_CODE);
        boolean hasElementId = extras.containsKey(Constants.IntentExtra.ELEMENT_ID);
        int elementId = extras.getInt(Constants.IntentExtra.ELEMENT_ID);

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

        String title = extras.getString(Constants.IntentExtra.TITLE);
        String msg   = extras.getString(Constants.IntentExtra.MESSAGE);
        Log.d(LOG_TAG, "Creating notification: " + msg);

        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        Notification.Builder notificationBuilder = new Notification.Builder(context, Constants.NotificationChannel.ALARM);
        notificationBuilder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(largeIcon)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_EVENT)
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true)
        ;

        int notificationId = hasElementId ? elementId : trip.trip.id;
        String notificationTag = hasElementId ? Constants.NotificationTag.TRIP_ELEMENT : Constants.NotificationTag.TRIP;
        if (actionName != null) {
            Intent elementIntent;
            if (tripElement == null) {
                elementIntent = trip.trip.getActivityIntent(Trip.ActivityType.POPUP);
            } else {
                elementIntent = tripElement.tripElement.getActivityIntent(TripElement.ActivityType.POPUP);
            }
            elementIntent.setAction(actionName);
            elementIntent.putExtra(Constants.IntentExtra.NOTIFICATION_TAG, tag);

            PendingIntent clickIntent = PendingIntent.getActivity(SHiTApplication.getContext() /*this?*/
                    , notificationId
                    , elementIntent
                    , PendingIntent.FLAG_UPDATE_CURRENT
                    , extras);
            notificationBuilder.setContentIntent(clickIntent);
        }
        mNotificationManager.notify(notificationTag, notificationId, notificationBuilder.build());
    }


    /**
     * Sets an alarm for a specific time
     * @param alertTime Time when user should be notified
     * @param data      Unique URI for the notification; if notification with the same URI exists, Android will replace it
     * @param extras    Additional information (text to display to user)
     */
    public void setAlarm(Date alertTime, Uri data, String actionName, Bundle extras) {
        Log.d(LOG_TAG, "Setting alarm for " + data.toString() + " at " + alertTime.toString());
        Context ctx = SHiTApplication.getContext();
        AlarmManager alarmMgr = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.setData(data);
        intent.putExtras(extras);
        if (actionName != null) {
            intent.setAction(actionName);
        }
        PendingIntent alarmIntent = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmMgr.canScheduleExactAlarms()) {
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, alertTime.getTime(), alarmIntent);
        } else {
            Log.i(LOG_TAG, "Not permitted to schedule exact alarm, falling back");
            alarmMgr.set(AlarmManager.RTC_WAKEUP, alertTime.getTime(), alarmIntent);
        }

        ComponentName receiver = new ComponentName(ctx, BootReceiver.class);
        PackageManager pm = ctx.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }


}
