package no.shitt.myshit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.util.Log;

import java.util.Date;

/**
 * When the alarm fires, this WakefulBroadcastReceiver receives the broadcast Intent
 * and then starts the IntentService {@code SampleSchedulingService} to do some work.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = AlarmReceiver.class.getSimpleName();

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "Broadcast received");
        /* Ensure intent extras are passed along to AlarmIntentService */
        /* Toasts are not good enough */
        AlarmIntentService.enqueueWork(context, intent);
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
        alarmMgr = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.setData(data);
        intent.putExtras(extras);
        if (actionName != null) {
            intent.setAction(actionName);
        }
        alarmIntent = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, alertTime.getTime(), alarmIntent);
        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.

        ComponentName receiver = new ComponentName(ctx, BootReceiver.class);
        PackageManager pm = ctx.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }


    /**
     * Cancels the alarm.
     */
    public void cancelAlarm() {
        Context ctx = SHiTApplication.getContext();
        // If the alarm has been set, cancel it.
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }

        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(ctx, BootReceiver.class);
        PackageManager pm = ctx.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
