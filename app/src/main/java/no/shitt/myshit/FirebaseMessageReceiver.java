package no.shitt.myshit;

//import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import no.shitt.myshit.model.TripList;

public class FirebaseMessageReceiver extends FirebaseMessagingService {
    public FirebaseMessageReceiver() {
    }

    /*
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    */

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        //Log.d("FirebaseMessageReceiver", "From: " + remoteMessage.getFrom());
        //Log.d("FirebaseMessageReceiver", "Notification Message Body: " + remoteMessage.getNotification().getBody());

        TripList.getSharedList().getFromServer();
    }
}
