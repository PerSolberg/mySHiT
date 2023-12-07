package no.shitt.myshit;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import android.util.Log;

import java.util.Objects;

import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.ChatMessage;
import no.shitt.myshit.model.TripList;


public class ChatMessageReplyReceiver extends BroadcastReceiver {
    private static final String TAG = ChatMessageReplyReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constants.Intent.CHATMSG_REPLY.equals(intent.getAction())) {
            int tripId = intent.getIntExtra(Constants.IntentExtra.TRIP_ID, -1);
            int messageId = intent.getIntExtra(Constants.IntentExtra.MESSAGE_ID, -1);
            CharSequence reply = getMessageText(intent);
            if (tripId != -1 && messageId != -1) {
                Log.d(TAG, "Got reply (" + reply + ") for TripId " + tripId);
                NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(context);

                AnnotatedTrip at = TripList.getSharedList().tripById(tripId);
                if (at != null) {
                    ChatMessage replyMsg = new ChatMessage(Objects.requireNonNull(reply).toString());
                    at.trip.chatThread.append(replyMsg);
                    notificationManager.cancel(Constants.NotificationTag.CHAT, messageId);
                } else {
                    // Update the notification with error to stop the progress spinner.
                    Notification.Builder replyBuilder;
                    replyBuilder = new Notification.Builder(context, Constants.NotificationChannel.CHAT);
                    replyBuilder
                            .setSmallIcon(R.mipmap.icon_chat)
                            .setContentText(context.getString(R.string.chat_reply_error_unknown_trip))
                            .build();

                    // Issue the new notification.
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    notificationManager.notify(Constants.NotificationTag.CHAT, messageId, replyBuilder.build());
                }
            }
        }
    }

    /**
     * Get the message text from the intent.
     * Note that you should call {@code RemoteInput#getResultsFromIntent(intent)} to process
     * the RemoteInput.
     */
    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(Constants.PushNotificationActions.CHATMSG_REPLY);
        }
        return null;
    }
}

