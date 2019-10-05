package no.shitt.myshit;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

public class SHiTApplication extends Application {

    private static Application sApplication;
    private static final Handler mHandler = new Handler();

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

    public static int getStringResourceIdByName(String aString) {
        String packageName = getApplication().getPackageName();
        return getApplication().getResources().getIdentifier(aString, "string", packageName);
    }

    public static int getPreferenceInt(String prefName, int defaultValue) {
        Context ctx = getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        int value;

        try {
            value = sharedPref.getInt(prefName, defaultValue);
        }
        catch (Exception e1) {
            try {
                String stringValue = sharedPref.getString(prefName, "");
                value = Integer.valueOf(stringValue);
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
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri chatSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/" + R.raw.chat_new_message);
            //Uri chatSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/chat_new_message");

            createNotificationChannel( Constants.NotificationChannel.CHAT
                    , R.string.ntf_channel_name_chat
                    , R.string.ntf_channel_desc_chat
                    , NotificationManager.IMPORTANCE_HIGH
                    , chatSound);
            createNotificationChannel( Constants.NotificationChannel.ALARM
                    , R.string.ntf_channel_name_alarm
                    , R.string.ntf_channel_desc_alarm
                    , NotificationManager.IMPORTANCE_DEFAULT);
            createNotificationChannel( Constants.NotificationChannel.UPDATE
                    , R.string.ntf_channel_name_update
                    , R.string.ntf_channel_desc_update
                    , NotificationManager.IMPORTANCE_DEFAULT);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String id, int ridName, int ridDescription, int importance) {
        createNotificationChannel(id, ridName, ridDescription, importance, null);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String id, int ridName, int ridDescription, int importance, Uri sound) {
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

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        //notificationManager.deleteNotificationChannel(id); // Delete first if we make changes
        notificationManager.createNotificationChannel(channel);
    }

}
