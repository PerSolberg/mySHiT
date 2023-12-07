package no.shitt.myshit;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.FirebaseInstallations;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;


public class SHiTApplication extends Application {
    private final static String LOG_TAG = SHiTApplication.class.getSimpleName();

    private static Application sApplication;
    private static final Handler mHandler = new Handler(Looper.getMainLooper());
    private static String deviceId = null;
    private static final Queue<Consumer<String>> deviceIdListenerQueue = new ArrayDeque<>();

    public static Application getApplication() {
        return sApplication;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    @SuppressWarnings("FinalStaticMethod")
    public static final void runOnUiThread(Runnable runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
        } else {
            mHandler.post(runnable);
        }
    }

    @SuppressLint("DiscouragedApi")
    public static int getStringResourceIdByName(String aString) {
        String packageName = getApplication().getPackageName();
        return getApplication().getResources().getIdentifier(aString, "string", packageName);
    }

    public static int getPreferenceInt(String prefName, int defaultValue) {
        Context ctx = getContext();
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getPackageName() + "_preferences", Context.MODE_PRIVATE);

        int value;

        try {
            value = sharedPref.getInt(prefName, defaultValue);
        }
        catch (Exception e1) {
            try {
                String stringValue = sharedPref.getString(prefName, "");
                value = Integer.parseInt(stringValue);
            }
            catch (Exception e2) {
                value = -1;
            }
        }

        return value;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        Uri chatSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/" + R.raw.chat_new_message);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        createNotificationChannel( Constants.NotificationChannel.CHAT
                , R.string.ntf_channel_name_chat
                , R.string.ntf_channel_desc_chat
                , NotificationManager.IMPORTANCE_HIGH
                , chatSound);
        createNotificationChannel( Constants.NotificationChannel.ALARM
                , R.string.ntf_channel_name_alarm
                , R.string.ntf_channel_desc_alarm
                , NotificationManager.IMPORTANCE_HIGH
                , alarmSound
                , Constants.NOTIFICATION_VIBRATION_PATTERN);
        createNotificationChannel( Constants.NotificationChannel.UPDATE
                , R.string.ntf_channel_name_update
                , R.string.ntf_channel_desc_update
                , NotificationManager.IMPORTANCE_DEFAULT);
    }

    private void createNotificationChannel(String id, int ridName, int ridDescription, int importance) {
        createNotificationChannel(id, ridName, ridDescription, importance, null, null);
    }

    private void createNotificationChannel(String id, int ridName, int ridDescription, int importance, Uri sound) {
        createNotificationChannel(id, ridName, ridDescription, importance, sound, null);
    }

    private void createNotificationChannel(String id, int ridName, int ridDescription, int importance, Uri sound, long[] vibrationPattern) {
        CharSequence name = getString(ridName);
        String description = getString(ridDescription);
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        if (sound != null) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(sound, audioAttributes);
        }
        if (vibrationPattern != null) {
            channel.setVibrationPattern(vibrationPattern);
        }

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public static String getFirebaseId(Consumer<String> callback) {
        if ( deviceId != null ) {
            callback.accept(deviceId);
            return deviceId;
        }

        Log.i(LOG_TAG, "Retrieving device ID");
        if (callback != null) {
            deviceIdListenerQueue.add(callback);
        }
        Task<String> idTask = FirebaseInstallations.getInstance().getId();
        idTask.addOnCompleteListener( (task) -> {
            if (task.isSuccessful()) {
                Log.i(LOG_TAG, "Device ID successfully retrieved");
                deviceId = task.getResult();
            } else {
                Log.w(LOG_TAG, "Unable to retrieve device ID, generating UUID instead");
                deviceId = UUID.randomUUID().toString();
            }
            while ( ! deviceIdListenerQueue.isEmpty() ) {
                Log.i(LOG_TAG, "Calling device ID listener");
                Consumer<String> cb = deviceIdListenerQueue.remove();
                cb.accept(deviceId);
            }
        } );

        return deviceId; // Can be null
    }
}
