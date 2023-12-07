package no.shitt.myshit.model;

/*
 *  ChatMessage
 *  ---------------------------------------------------------------------------
 *  Chat message, managed by ChatThread.
 *
 *  Created by Per Solberg on 2017-05-30.
 */

import android.content.Intent;
import android.os.Process;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

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
import no.shitt.myshit.helper.JSONable;
import no.shitt.myshit.helper.ServerAPI;
import no.shitt.myshit.helper.ServerDate;


public class ChatMessage implements JSONable {
    private static final String LOG_TAG = ChatMessage.class.getSimpleName();

    static class LocalId implements JSONable {
        String deviceType;
        String deviceId;
        String localId;

        LocalId () {
            deviceType = null;
            deviceId = null;
            localId = null;
        }

        LocalId (JSONObject elementData) {
            this();
            if (elementData != null) {
                deviceType = elementData.optString(Constants.JSON.CHATMSG_DEVICE_TYPE);
                deviceId = elementData.optString(Constants.JSON.CHATMSG_DEVICE_ID);
                localId = elementData.optString(Constants.JSON.CHATMSG_LOCAL_ID);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null)
                return false;

            if (getClass() == o.getClass()) {
                LocalId otherId = (LocalId) o;

                return (this.deviceType != null
                        && this.deviceId != null
                        && this.localId != null
                        && this.deviceType.equals(otherId.deviceType)
                        && this.deviceId.equals(otherId.deviceId)
                        && this.localId.equals(otherId.localId));
            } else if (o.getClass() == ChatMessage.class) {
                ChatMessage msg = (ChatMessage) o;
                return equals(msg.localId);
            }
            return false;
        }

        // Hash codes aren't used for chat messages, but hashCode() should always be overridden whenever equals() is
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + (this.deviceType == null ? 0 : this.deviceType.hashCode());
            hash = 31 * hash + (this.deviceId == null ? 0 : this.deviceId.hashCode());
            hash = 31 * hash + (this.localId == null ? 0 : this.localId.hashCode());
            return hash;
        }



        public JSONObject toJSON() throws JSONException {
            JSONObject jo = new JSONObject();

            jo.putOpt(Constants.JSON.CHATMSG_DEVICE_TYPE, deviceType);
            jo.putOpt(Constants.JSON.CHATMSG_DEVICE_ID, deviceId);
            jo.putOpt(Constants.JSON.CHATMSG_LOCAL_ID, localId);

            return jo;
        }
    }

    static private final String TIMEZONE = "UTC";
    static         final int    ID_NONE  = -1;


    // MARK: Properties
    int     id;
    final int     userId;
    private final String  userName;
    private final String  userInitials;
    final LocalId localId;
    final private String  messageText;
    private Date    storedTimestamp;
    private Date    createdTimestamp;

    List<String> lastSeenBy;

    private class SaveResponseHandler implements ServerAPI.Listener {
        final ServerAPI.Listener parentResponseHandler;

        SaveResponseHandler(ServerAPI.Listener parentResponseHandler) {
            this.parentResponseHandler = parentResponseHandler;
        }

        public void onRemoteCallComplete(JSONObject response) {
            JSONObject messageData = response.optJSONObject(ServerAPI.ResultItem.MESSAGE);
            if (messageData != null) {
                if (!messageData.isNull(Constants.JSON.CHATMSG_ID) && !messageData.isNull(Constants.JSON.CHATMSG_STORED_TS)) {
                    id = messageData.optInt(Constants.JSON.CHATMSG_ID);

                    String storedTimestampText = messageData.isNull(Constants.JSON.CHATMSG_STORED_TS) ? null : messageData.optString(Constants.JSON.CHATMSG_STORED_TS);
                    storedTimestamp = ServerDate.convertServerDate(storedTimestampText, null);

                    Intent intent = new Intent(Constants.Notification.CHAT_UPDATED);
                    LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
                }
            } else {
                Log.e("ChatMessage.Save", "Incorrect response: " + response);
            }

            if (parentResponseHandler != null) {
                parentResponseHandler.onRemoteCallComplete(response);
            }
        }

        public void onRemoteCallFailed() {
            Intent intent = new Intent(Constants.Notification.COMMUNICATION_FAILED);
            LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
            if (parentResponseHandler != null) {
                parentResponseHandler.onRemoteCallFailed();
            }
        }

        public void onRemoteCallFailed(Exception e) {
            Intent intent = new Intent(Constants.Notification.COMMUNICATION_FAILED);
            intent.putExtra("message", e.getMessage());
            LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
            if (parentResponseHandler != null) {
                parentResponseHandler.onRemoteCallFailed(e);
            }
        }
    }


    private class ReadResponseHandler implements ServerAPI.Listener {
        final ServerAPI.Listener parentResponseHandler;

        ReadResponseHandler(ServerAPI.Listener parentResponseHandler) {
            this.parentResponseHandler = parentResponseHandler;
        }

        public void onRemoteCallComplete(JSONObject response) {
            Log.d("ChatMessage.Read", "Message read status updated");
            if (parentResponseHandler != null) {
                parentResponseHandler.onRemoteCallComplete(response);
            }
        }

        public void onRemoteCallFailed() {
            Intent intent = new Intent(Constants.Notification.COMMUNICATION_FAILED);
            LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
            if (parentResponseHandler != null) {
                parentResponseHandler.onRemoteCallFailed();
            }
        }

        public void onRemoteCallFailed(Exception e) {
            Intent intent = new Intent(Constants.Notification.COMMUNICATION_FAILED);
            intent.putExtra("message", e.getMessage());
            LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
            if (parentResponseHandler != null) {
                parentResponseHandler.onRemoteCallFailed(e);
            }
        }
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getUserInitials() { return userInitials; }
    public String getMessageText() { return messageText; }
    public List<String> getLastSeenBy() { return lastSeenBy; }
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;

        if (getClass() == o.getClass()) {
            ChatMessage otherMsg = (ChatMessage) o;
            return this.localId.equals(otherMsg.localId);
        } else if (o.getClass() == LocalId.class) {
            // Can be compared directly to LocalId
            LocalId otherId = (LocalId) o;
            return this.localId.equals(otherId);
        } else if (o.getClass() == Integer.class) {
            // Can be compared directly to id
            Integer otherId = (Integer) o;
            return this.id == otherId && this.id != ID_NONE;
        }

        return false;
    }

    // Hash codes aren't used for chat messages, but hashCode() should always be overridden whenever equals() is
    public int hashCode() {
        return this.localId.hashCode();
    }

    static private String generateLocalId() {
        return System.currentTimeMillis() + "." + Process.myTid();
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

    public ChatMessage(String message) {
        id = ID_NONE;
        userId = User.sharedUser.getId();
        userName = User.sharedUser.getShortName();
        userInitials = User.sharedUser.getInitials();
        localId = new LocalId();
        localId.deviceType = Constants.DEVICE_TYPE;
        localId.deviceId   = SHiTApplication.getFirebaseId( (id) -> this.localId.deviceId = id);
        localId.localId    = generateLocalId();
        messageText = message;
        storedTimestamp = null;
        createdTimestamp = new Date();
    }

    // MARK: Functions
    void save(int tripId, ServerAPI.Listener parentResponseHandler) {
        if (localId.deviceId == null) {
            // Haven't retrieved Firebase ID yet, unable to save
            Log.i(LOG_TAG, "Device ID not yet populated, need to retry");
            parentResponseHandler.onRemoteCallFailed();
            return;
        }
        JSONObject payload = new JSONObject(this.savePayload());
        ServerAPI.Params params = new ServerAPI.Params(ServerAPI.URL_BASE, ServerAPI.RESOURCE_CHAT, Integer.toString(tripId), null, null);
        params.addParameter(ServerAPI.Param.USER_NAME, User.sharedUser.getUserName());
        params.addParameter(ServerAPI.Param.PASSWORD, User.sharedUser.getPassword());
        params.addParameter(ServerAPI.Param.LANGUAGE, Locale.getDefault().getLanguage());
        params.setPayload(payload);

        new ServerAPI(ServerAPI.Method.PUT, new SaveResponseHandler(parentResponseHandler)).execute(params);
    }

    void read(int tripId, ServerAPI.Listener parentResponseHandler) {
        if (BuildConfig.DEBUG && tripId == ID_NONE) {
            throw new AssertionError("Cannot read messages for unknown trip");
        }

        ServerAPI.Params params = new ServerAPI.Params( ServerAPI.URL_BASE
                                                    , ServerAPI.RESOURCE_CHAT, Integer.toString(tripId)
                                                    , ServerAPI.VERB_MSG_READ, Integer.toString(id));
        params.addParameter(ServerAPI.Param.USER_NAME, User.sharedUser.getUserName());
        params.addParameter(ServerAPI.Param.PASSWORD, User.sharedUser.getPassword());

        new ServerAPI(ServerAPI.Method.POST, new ReadResponseHandler(parentResponseHandler)).execute(params);
    }
}
