package no.shitt.myshit.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

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
            Network[] networks = connectivityMgr.getAllNetworks();
            for (Network n : networks) {
                NetworkInfo networkInfo = connectivityMgr.getNetworkInfo(n);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }

        }
        return false;
    }
}
