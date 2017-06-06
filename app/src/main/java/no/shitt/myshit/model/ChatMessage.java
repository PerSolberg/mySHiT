package no.shitt.myshit.model;

/**
 * Created by persolberg on 2017-05-30.
 */

import android.content.Intent;
import android.os.Process;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.iid.InstanceID;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import no.shitt.myshit.BuildConfig;
import no.shitt.myshit.Constants;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.ServerAPI;
import no.shitt.myshit.helper.ServerAPIListener;
import no.shitt.myshit.helper.ServerAPIParams;
import no.shitt.myshit.helper.ServerDate;


public class ChatMessage {
    static class LocalId {
        String deviceType;
        String deviceId;
        String localId;
    }
    static private String TIMEZONE = "UTC";
    static private int    ID_NONE  = -1;


    // MARK: Properties
    int     id;
    int     userId;
    String  userName;
    String  userInitials;
    LocalId localId;
    String  messageText;
    Date    storedTimestamp;
    Date    createdTimestamp;

    List<String> lastSeenBy;

    private class SaveResponseHandler implements ServerAPIListener {
        public void onRemoteCallComplete(JSONObject response) {
            //Log.d("ChatMessage.Save", "Message saved");
            if (response.isNull(Constants.JSON.CHATMSG_ID) || response.isNull(Constants.JSON.CHATMSG_STORED_TS)) {
                id = response.optInt(Constants.JSON.CHATMSG_ID);

                String storedTimestampText = response.isNull(Constants.JSON.CHATMSG_STORED_TS) ? null : response.optString(Constants.JSON.CHATMSG_STORED_TS);
                storedTimestamp = ServerDate.convertServerDate(storedTimestampText, null);

                //Log.d("ChatMessage.Save", "Sending notification");
                Intent intent = new Intent(Constants.Notification.CHAT_UPDATED);
                //intent.putExtra("message", "SHiT Chat messages loaded");
                LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
            } else {
                Log.e("ChatMessage.Save", "Incorrect response: " + response.toString());
            }
        }

        public void onRemoteCallFailed() {
            //Log.d("ChatMessage.Save", "Server call failed");
            Intent intent = new Intent(Constants.Notification.COMMUNICATION_FAILED);
            //intent.putExtra("message", "SHiT trips loaded");
            LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
        }

        public void onRemoteCallFailed(Exception e) {
            //Log.d("ChatMessage.Save", "Server call failed");
            Intent intent = new Intent(Constants.Notification.COMMUNICATION_FAILED);
            intent.putExtra("message", e.getMessage());
            LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
        }
    }

    private class ReadResponseHandler implements ServerAPIListener {
        public void onRemoteCallComplete(JSONObject response) {
            Log.d("ChatMessage.Read", "Message read status updated");
        }

        public void onRemoteCallFailed() {
            //Log.d("ChatMessage.Read", "Server call failed");
            Intent intent = new Intent(Constants.Notification.COMMUNICATION_FAILED);
            //intent.putExtra("message", "SHiT trips loaded");
            LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
        }

        public void onRemoteCallFailed(Exception e) {
            //Log.d("ChatMessage.Read", "Server call failed");
            Intent intent = new Intent(Constants.Notification.COMMUNICATION_FAILED);
            intent.putExtra("message", e.getMessage());
            LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
        }
    }

    public Boolean isStored() {
        return id > 0;
    }

    private Map<String,String> savePayload() {
        Map<String,String> payload = new HashMap<>();

        payload.put("deviceType", localId.deviceType);
        payload.put("deviceId", localId.deviceId);
        payload.put("localId", localId.localId);
        payload.put("message", messageText);
        payload.put("createdTS", ServerDate.convertServerDate(createdTimestamp, ChatMessage.TIMEZONE));

        return payload;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject(); // super.toJSON();

        jo.putOpt(Constants.JSON.CHATMSG_ID, id);
        jo.putOpt(Constants.JSON.CHATMSG_USER_ID, userId);
        jo.putOpt(Constants.JSON.CHATMSG_USER_NAME, userName);
        jo.putOpt(Constants.JSON.CHATMSG_USER_INIT, userInitials);
        jo.putOpt(Constants.JSON.CHATMSG_DEVICE_TYPE, localId.deviceType);
        jo.putOpt(Constants.JSON.CHATMSG_DEVICE_ID, localId.deviceId);
        jo.putOpt(Constants.JSON.CHATMSG_LOCAL_ID, localId.localId);
        jo.putOpt(Constants.JSON.CHATMSG_MESSAGE_TEXT, messageText);
        jo.putOpt(Constants.JSON.CHATMSG_STORED_TS, storedTimestamp);
        jo.putOpt(Constants.JSON.CHATMSG_CREATED_TS, createdTimestamp);

        return jo;
    }

    boolean isEqual(ChatMessage otherMsg) {
        return this.localId == otherMsg.localId;
    }

    static /* synchronized */ private String generateLocalId() {
        return Long.toString(System.currentTimeMillis()) + "." + Long.toString(Process.myTid());
    }

    // MARK: Initialisers
    ChatMessage (JSONObject elementData) {
        super();
        id = elementData.optInt(Constants.JSON.CHATMSG_ID, ID_NONE);
        userId = elementData.optInt(Constants.JSON.CHATMSG_USER_ID, ID_NONE);
        userName  = elementData.optString(Constants.JSON.CHATMSG_USER_NAME, "Unknown");
        userInitials = elementData.optString(Constants.JSON.CHATMSG_USER_INIT, "XXX");

        localId = new LocalId();
        localId.deviceType = elementData.isNull(Constants.JSON.CHATMSG_DEVICE_TYPE) ? null : elementData.optString(Constants.JSON.CHATMSG_DEVICE_TYPE);
        localId.deviceId   = elementData.isNull(Constants.JSON.CHATMSG_DEVICE_ID) ? null : elementData.optString(Constants.JSON.CHATMSG_DEVICE_ID);
        localId.localId    = elementData.isNull(Constants.JSON.CHATMSG_LOCAL_ID) ? null : elementData.optString(Constants.JSON.CHATMSG_LOCAL_ID);

        messageText = elementData.optString(Constants.JSON.CHATMSG_MESSAGE_TEXT);

        String storedTSText = elementData.isNull(Constants.JSON.CHATMSG_STORED_TS) ? null : elementData.optString(Constants.JSON.CHATMSG_STORED_TS);
        if (storedTSText != null) {
            storedTimestamp = ServerDate.convertServerDate(storedTSText, TIMEZONE);
        }

        String createdTSText = elementData.isNull(Constants.JSON.CHATMSG_CREATED_TS) ? null : elementData.optString(Constants.JSON.CHATMSG_CREATED_TS);
        if (createdTSText != null) {
            createdTimestamp = ServerDate.convertServerDate(createdTSText, TIMEZONE);
        }
    }

    ChatMessage(String message) {
        id = ID_NONE;
        userId = User.sharedUser.getId();
        userName = User.sharedUser.getShortName();
        userInitials = User.sharedUser.getInitials();
        localId = new LocalId();
        localId.deviceType = Constants.DEVICE_TYPE;
        localId.deviceId   = InstanceID.getInstance(SHiTApplication.getContext()).getId();
        //localId.deviceId   = Settings.Secure.getString(SHiTApplication.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        localId.localId    = generateLocalId();
        messageText = message;
        storedTimestamp = null;
        createdTimestamp = new Date();
    }

    // MARK: Functions
    void save(int tripId /*, responseHandler parentResponseHandler: @escaping (URLResponse?, NSDictionary?, Error?) -> Void*/) {
        JSONObject payload = new JSONObject(this.savePayload());
        ServerAPIParams params = new ServerAPIParams(ServerAPI.URL_BASE, ServerAPI.RESOURCE_CHAT, Integer.toString(tripId), null, null);
        params.addParameter(ServerAPI.PARAM_USER_NAME, User.sharedUser.getUserName());
        params.addParameter(ServerAPI.PARAM_PASSWORD, User.sharedUser.getPassword());
        params.addParameter(ServerAPI.PARAM_LANGUAGE, Locale.getDefault().getLanguage());
        params.setPayload(payload);

        new ServerAPI(ServerAPI.Method.PUT, new SaveResponseHandler()).execute(params);
    }

    void read(int tripId /* responseHandler parentResponseHandler: @escaping (URLResponse?, NSDictionary?, Error?)  -> Void */) {
        if (BuildConfig.DEBUG && tripId != ID_NONE) {
            throw new AssertionError("Cannot read messages for unknown trip");
        }

        //JSONObject payload = new JSONObject(this.savePayload());
        ServerAPIParams params = new ServerAPIParams( ServerAPI.URL_BASE
                                                    , ServerAPI.RESOURCE_CHAT, Integer.toString(tripId)
                                                    , ServerAPI.VERB_MSG_READ, Integer.toString(id));
        params.addParameter(ServerAPI.PARAM_USER_NAME, User.sharedUser.getUserName());
        params.addParameter(ServerAPI.PARAM_PASSWORD, User.sharedUser.getPassword());
        //params.addParameter(ServerAPI.PARAM_LANGUAGE, Locale.getDefault().getLanguage());
        //params.setPayload(payload);

        new ServerAPI(ServerAPI.Method.POST, new ReadResponseHandler()).execute(params);
    }
}
