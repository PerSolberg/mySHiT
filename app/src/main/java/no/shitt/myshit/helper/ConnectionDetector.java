package no.shitt.myshit.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
//import android.net.NetworkInfo;

public class ConnectionDetector {

    final private Context _context;

    public ConnectionDetector(Context context){
        this._context = context;
    }

    /**
     * Checking for all possible internet providers
     * **/
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isConnectedToInternet(){
        ConnectivityManager connectivityMgr = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityMgr != null)
        {
            Network activeNetwork = connectivityMgr.getActiveNetwork();
            NetworkCapabilities activeNetCaps = connectivityMgr.getNetworkCapabilities(activeNetwork);
            return activeNetCaps != null && activeNetCaps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&  activeNetCaps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        }
        return false;
    }
}
