package no.shitt.myshit;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.ChatThread;
import no.shitt.myshit.model.TripList;

public class FirebaseMessageReceiver extends FirebaseMessagingService {
    private static final String LOG_TAG = FirebaseMessageReceiver.class.getSimpleName();

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
        Log.d(LOG_TAG, "From: " + remoteMessage.getFrom());
        Log.d(LOG_TAG, "Data: " + remoteMessage.getData());
        //Log.d(LOG_TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

        Map<String,String> ntfData = remoteMessage.getData();
        if (ntfData.size() > 0) {
            boolean chatMessage = ntfData.get(Constants.PushNotificationKeys.CHANGE_TYPE).equals(Constants.PushNotificationData.TYPE_CHAT_MESSAGE);
            boolean insert = ntfData.get(Constants.PushNotificationKeys.CHANGE_OPERATION).equals(Constants.PushNotificationData.OP_INSERT);
            if (chatMessage) {
                Log.d(LOG_TAG, "Handle new/read message");
                int tripId = Integer.parseInt(ntfData.get(Constants.PushNotificationKeys.TRIP_ID));
                AnnotatedTrip aTrip = TripList.getSharedList().tripById(tripId);
                if (aTrip != null && aTrip.trip.chatThread != null) {
                    if (insert) {
                        MediaPlayer mPlayer = MediaPlayer.create(SHiTApplication.getContext(), R.raw.chat_new_message);
                        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mPlayer.start();

                        aTrip.trip.chatThread.refresh(ChatThread.RefreshMode.INCREMENTAL);
                    } else {
                        String strLastSeenInfo = ntfData.get(Constants.PushNotificationKeys.LAST_SEEN_INFO);
                        Log.d(LOG_TAG, "Last Seen Info = " + strLastSeenInfo);
                        try {
                            JSONObject lastSeenInfo = new JSONObject(strLastSeenInfo);
                            int lastSeenVersion = lastSeenInfo.optInt(Constants.JSON.CHATTHREAD_LAST_SEEN_VERSION);
                            JSONObject lastSeenByUsers = lastSeenInfo.optJSONObject(Constants.JSON.CHATTHREAD_LAST_SEEN_OTHERS);
                            if (lastSeenByUsers != null) {
                                aTrip.trip.chatThread.updateReadStatus(lastSeenByUsers, lastSeenVersion);
                            } else {
                                Log.e(LOG_TAG, "Missing read status for users");
                            }
                        } catch (JSONException je) {
                            Log.e(LOG_TAG, "Unable to parse JSON for last seen info");
                        }
                    }
                }
            } else {
                TripList.getSharedList().getFromServer();
            }
        }
    }
}
