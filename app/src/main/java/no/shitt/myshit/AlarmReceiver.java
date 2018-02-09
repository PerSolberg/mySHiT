package no.shitt.myshit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

/**
 * When the alarm fires, this WakefulBroadcastReceiver receives the broadcast Intent
 * and then starts the IntentService {@code SampleSchedulingService} to do some work.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {
    // The app's AlarmManager, which provides access to the system alarm services.
    private AlarmManager alarmMgr;
    // The pending intent that is triggered when the alarm fires.
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "Broadcast received");
        /* Ensure intent extras are passed along to SchedulingService */
        /* Toasts are not good enough */
        ComponentName comp = new ComponentName(context.getPackageName(), SchedulingService.class.getName());
        intent.setComponent(comp);
        startWakefulService(context, intent);
    }

    /**
     * Sets an alarm for a specific time
     * @param alertTime Time when user should be notified
     * @param data      Unique URI for the notification; if notification with the same URI exists, Android will replace it
     * @param extras    Additional information (text to display to user)
     */
    public void setAlarm(Date alertTime, Uri data, String actionName, Bundle extras) {
        //Log.d("AlarmReceiver", "Setting alarm for " + data.toString() + " at " + alertTime.toString());
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

    public void setAlarm(Date alertTime, Uri data, Bundle extras) {
        setAlarm(alertTime, data, null, extras);
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
