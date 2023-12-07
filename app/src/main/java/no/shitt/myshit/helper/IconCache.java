package no.shitt.myshit.helper;

import android.accounts.AuthenticatorException;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import no.shitt.myshit.SHiTApplication;

public class IconCache {
    private final static String LOG_TAG = IconCache.class.getSimpleName();

    private static final IconCache sharedCache = new IconCache();
    private final static HashMap<IconKey, Icon> cache = new HashMap<>();


    private IconCache() {}

    public static abstract class IconKey {
        public abstract IconKey getParent();
        public abstract String downloadPath();
        public abstract String name();
    }

    public static IconCache getSharedCache() {
        return sharedCache;
    }

    public Icon get(IconKey key, Consumer<Icon> callback) {
        Icon i;
        while (key != null) {
            i = cache.get(key);
            if (i != null) {
                return i;
            } else {
                int iconId = getIconId(key);
                if (iconId != 0) {
                    Context ctx = SHiTApplication.getContext();
                    i = Icon.createWithResource(ctx, iconId);
                    return i;
                } else {
                    Log.i(LOG_TAG, "Icon " + key.name() + " not found, trying to download");
                    downloadIcon(key, callback);
                }
            }
            key = key.getParent();
        }
        return null;
    }

    
    private void downloadIcon(IconKey key, Consumer<Icon> callback) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute( () -> {
            String urlString = key.downloadPath();
            InputStream in;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urlString);
                Log.i(LOG_TAG, "Downloading icon from " + urlString);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(10000);

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new AuthenticatorException();
                }
                in = urlConnection.getInputStream();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inDensity = DisplayMetrics.DENSITY_XXHIGH;
                Bitmap image = BitmapFactory.decodeStream(in, null, options);
                Icon icon = Icon.createWithBitmap(image);
                in.close();
                Log.d(LOG_TAG, "Caching icon " + key.name() );
                cache.put(key, icon);
                if (callback != null) {
                    callback.accept(icon);
                }
            } catch (Exception e) {
                Log.e("downloadIcon", e.toString());
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }

        } ) ;

    }
    
    @SuppressLint("DiscouragedApi")
    private int getIconId(IconKey key) {
        Context ctx = SHiTApplication.getContext();
        return ctx.getResources().getIdentifier(key.name().toLowerCase(), "mipmap", ctx.getPackageName());
    }


}
