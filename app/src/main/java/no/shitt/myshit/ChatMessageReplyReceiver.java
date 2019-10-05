package no.shitt.myshit;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;

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
                    ChatMessage replyMsg = new ChatMessage(reply.toString());
                    at.trip.chatThread.append(replyMsg);
                    notificationManager.cancel(Constants.NotificationTag.CHAT, messageId);
                } else {
                    // Update the notification with error to stop the progress spinner.
                    //Icon smallIcon = Icon.createWithResource(context, R.mipmap.icon_chat);
                    Notification.Builder replyBuilder;
                    if (Build.VERSION.SDK_INT >= 26) {
                        replyBuilder = new Notification.Builder(context, Constants.NotificationChannel.CHAT);
                    } else {
                        replyBuilder = new Notification.Builder(context);
                    }
                    replyBuilder
                                .setSmallIcon(R.mipmap.icon_chat)
                                .setContentText(context.getString(R.string.chat_reply_error_unknown_trip))
                                .build();

                    // Issue the new notification.
                    //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
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

