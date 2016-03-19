package no.shitt.myshit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

import no.shitt.myshit.model.TripList;

/**
 * This BroadcastReceiver automatically (re)starts the alarm when the device is
 * rebooted. This receiver is set to be disabled (android:enabled="false") in the
 * application's manifest file. When the user sets the alarm, the receiver is enabled.
 * When the user cancels the alarm, the receiver is disabled, so that rebooting the
 * device will not trigger this receiver.
 */
// BEGIN_INCLUDE(autostart)
public class BootReceiver extends BroadcastReceiver {
    AlarmReceiver alarm = new AlarmReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        //Context context = SHiTApplication.getContext();
        //Log.d("BootReceiver", "onReceive");
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")
                || intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON"))
        {
            //Log.d("BootReceiver", "onReceive: BOOT_COMPLETED");
            // Load trips from local archive; alerts will be set as part of the process
            TripList.getSharedList().loadFromArchive();
        }
    }
}
//END_INCLUDE(autostart)
