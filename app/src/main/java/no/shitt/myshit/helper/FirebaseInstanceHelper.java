package no.shitt.myshit.helper;

import com.google.firebase.iid.FirebaseInstanceIdService;

/******************************************************************************
 * Created by Per Solberg on 2017-01-18.
 *****************************************************************************/

public class FirebaseInstanceHelper extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        //String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Log.d("FirebaseInstanceHelper", "Refreshed token: " + refreshedToken);

        // sendRegistrationToServer(refreshedToken);
    }
}
