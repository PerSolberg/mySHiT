package no.shitt.myshit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.media.RingtoneManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.app.RemoteInput;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.ChatThread;
import no.shitt.myshit.model.TripList;
import no.shitt.myshit.model.User;


public class FirebaseMessageReceiver extends FirebaseMessagingService {
    private static final String LOG_TAG = FirebaseMessageReceiver.class.getSimpleName();

    public FirebaseMessageReceiver() {
    }


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
            int tripId = Integer.parseInt(ntfData.get(Constants.PushNotificationKeys.TRIP_ID));
            if (chatMessage) {
                Log.d(LOG_TAG, "Handle new/read message");
                AnnotatedTrip aTrip = TripList.getSharedList().tripById(tripId);
                if (aTrip != null && aTrip.trip.chatThread != null) {
                    if (insert) {
                        aTrip.trip.chatThread.refresh(ChatThread.RefreshMode.INCREMENTAL);

                        int fromUserId = Integer.parseInt(ntfData.get(Constants.PushNotificationKeys.FROM_USER_ID));
                        int messageId = Integer.parseInt(ntfData.get(Constants.PushNotificationKeys.MESSAGE_ID));
                        if (fromUserId != User.sharedUser.getId()) {
                            //MediaPlayer mPlayer = MediaPlayer.create(SHiTApplication.getContext(), R.raw.chat_new_message);
                            //mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            //mPlayer.start();

                            Context ctx = SHiTApplication.getContext();

                            // Construct loc-args from individual elements (because we use data-only messages)
                            int locArgNo = 1;
                            List<String> locArgs = new ArrayList<>();
                            while (ntfData.containsKey(Constants.PushNotificationKeys.LOC_ARGS + "-" + locArgNo)) {
                                locArgs.add(ntfData.get(Constants.PushNotificationKeys.LOC_ARGS + "-" + locArgNo));
                                locArgNo++;
                            }
                            Object[] locArgsArray = locArgs.toArray();
                            String msg = ctx.getString(R.string.NTF_INSERT_CHATMESSAGE, locArgsArray);
                            sendChatNotification(tripId, messageId, msg);
                        }
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
                Log.d(LOG_TAG, "Received notification for trip update");
                sendNotification(tripId, remoteMessage);
                TripList.getSharedList().getFromServer();
            }
        }
    }


    private void sendNotification(int tripId, RemoteMessage remoteMessage) {
        Context ctx = SHiTApplication.getContext();
        Map<String,String> ntfData = remoteMessage.getData();
        String messageBody = "Unknown update";
        String messageTitle = null;

        String bodyLocKey = remoteMessage.getNotification().getBodyLocalizationKey();
        if (bodyLocKey != null) {
            int locKeyId = SHiTApplication.getStringResourceIdByName(bodyLocKey);
            Object[] locArgs = remoteMessage.getNotification().getBodyLocalizationArgs();
            messageBody = ctx.getString(locKeyId, locArgs);
        }

        String titleLocKey = remoteMessage.getNotification().getTitleLocalizationKey();
        if (titleLocKey != null) {
            int locKeyId = SHiTApplication.getStringResourceIdByName(titleLocKey);
            Object[] locArgs = remoteMessage.getNotification().getTitleLocalizationArgs();
            messageTitle = ctx.getString(locKeyId, locArgs);
        }

        // Intent for clicking the notification
        Intent intent = new Intent(this, TripDetailsPopupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (bodyLocKey != null) {
            intent.setAction(bodyLocKey);
        }
        intent.putExtra(Constants.PushNotificationKeys.TRIP_ID, String.valueOf(tripId));
        //intent.putExtra(Constants.PushNotificationKeys.CHANGE_TYPE, Constants.PushNotificationData.TYPE_CHAT_MESSAGE);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        //Icon smallIcon = Icon.createWithResource(ctx, R.mipmap.icon_chat);

        //String channelId = getString(R.string.ntf_category_default);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //Uri chatSoundUri = Uri.parse("android.resource://" + ctx.getPackageName()+"/"+R.raw.chat_new_message);
        Notification.Builder notificationBuilder = new Notification.Builder(this /*, channelId*/)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(largeIcon)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                //.setVisibility(Notification.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_HIGH)
                //.setCategory(Notification.CATEGORY_EVENT)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(tripId, notificationBuilder.build());
    }


    private void sendChatNotification(int tripId, int messageId, String messageBody) {
        Context ctx = SHiTApplication.getContext();

        // Intent for clicking the notification
        Intent intent = new Intent(this, TripDetailsPopupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Constants.PushNotificationActions.CHATMSG_CLICK);
        intent.putExtra(Constants.PushNotificationKeys.TRIP_ID, String.valueOf(tripId));
        intent.putExtra(Constants.PushNotificationKeys.CHANGE_TYPE, Constants.PushNotificationData.TYPE_CHAT_MESSAGE);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Icon smallIcon = Icon.createWithResource(ctx, R.mipmap.icon_chat);

        // Reply action
        String replyLabel = getResources().getString(R.string.ntf_action_chat_reply);
        RemoteInput remoteInput = new RemoteInput.Builder(Constants.PushNotificationActions.CHATMSG_REPLY)
                .setLabel(replyLabel)
                .build();
        PendingIntent replyPendingIntent =
                PendingIntent.getBroadcast(getApplicationContext(),
                        tripId, //conversation.getConversationId(),
                        getMessageReplyIntent(tripId, messageId),
                        PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action replyAction =
                new Notification.Action.Builder(smallIcon, //R.mipmap.icon_chat,
                        getString(R.string.ntf_action_chat_reply), replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        // Ignore action
        PendingIntent ignorePendingIntent =
                PendingIntent.getBroadcast(getApplicationContext(),
                        tripId, //conversation.getConversationId(),
                        getMessageIgnoreIntent(tripId, messageId),
                        PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action ignoreAction =
                new Notification.Action.Builder(smallIcon, //R.mipmap.icon_chat,
                        getString(R.string.ntf_action_chat_ignore), ignorePendingIntent)
                        //.addRemoteInput(remoteInput)
                        .build();

        //String channelId = getString(R.string.ntf_category_default);
        //Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri chatSoundUri = Uri.parse("android.resource://" + ctx.getPackageName()+"/"+R.raw.chat_new_message);
        //NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this /*, channelId*/)
        Notification.Builder notificationBuilder = new Notification.Builder(this /*, channelId*/)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(largeIcon)
                //.setContentTitle("SHiT FCM Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(chatSoundUri)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .addAction(replyAction)
                .addAction(ignoreAction)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(messageId /* ID of notification */, notificationBuilder.build());
    }


    private Intent getMessageReplyIntent(int tripId, int messageId) {
        return new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction(Constants.Intent.CHATMSG_REPLY)
                .putExtra(Constants.IntentExtra.TRIP_ID, tripId)
                .putExtra(Constants.IntentExtra.MESSAGE_ID, messageId);
    }


    private Intent getMessageIgnoreIntent(int tripId, int messageId) {
        return new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction(Constants.Intent.CHATMSG_IGNORE)
                .putExtra(Constants.IntentExtra.TRIP_ID, tripId)
                .putExtra(Constants.IntentExtra.MESSAGE_ID, messageId);
    }

}
