package no.shitt.myshit;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
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
    }
}