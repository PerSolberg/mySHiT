package no.shitt.myshit.model;

import android.content.Intent;
import android.database.DataSetObservable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import no.shitt.myshit.Constants;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.JSONable;
import no.shitt.myshit.helper.ServerAPI;

/*
 *  ChatThread
 *  ---------------------------------------------------------------------------
 *  Manages all messages in a thread (i.e., a trip)
 *
 *  Created by Per Solberg on 2017-06-10.
 */

public class ChatThread extends DataSetObservable implements JSONable {
    private final static String LOG_TAG = ChatThread.class.getSimpleName();

    private static final int POOL_SIZE_MAXIMUM = 1;

    public static final String LAST_SEEN_BY_EVERYONE = "(ALL)";
    private static final Map<Integer,Double> retryDelays;

    static {
        retryDelays = new TreeMap<>(Comparator.reverseOrder());
        retryDelays.put(1, 5.0);
        retryDelays.put(10, 30.0);
        retryDelays.put(20, 300.0);
        retryDelays.put(30, 1800.0);
    }

    public enum RefreshMode {
        FULL,
        INCREMENTAL;
    }

    private class LastSeenInfo {
        private final Map<String,ArrayList<LastSeenUser>> lastSeen;

        LastSeenInfo(JSONObject elementData) {
            lastSeen = new HashMap<>();
            //noinspection ConstantValue
            if (elementData == null || elementData.keys() == null) {
                return;
            }

            for (Iterator<String> msgIdList = elementData.keys(); msgIdList.hasNext();) {
                String msgId = msgIdList.next();
                ArrayList<LastSeenUser> userInfoList = new ArrayList<>();
                JSONArray jsonUserInfoList = elementData.optJSONArray(msgId);
                for (int i = 0; i < Objects.requireNonNull(jsonUserInfoList).length(); i++) {
                    LastSeenUser userInfo = new LastSeenUser(jsonUserInfoList.optJSONObject(i));
                    if (userInfo.userId != User.sharedUser.getId()) {
                        userInfoList.add(userInfo);
                    } else if (msgId.matches("^\\s*\\d+\\s*$")) {
                        lastSeenByUserServer = Integer.parseInt(msgId);
                    }
                }
                if (userInfoList.size() > 0) {
                    lastSeen.put(msgId, userInfoList);
                }
            }
        }

        private class LastSeenUser {
            final int userId;
            final String userName;

            LastSeenUser(JSONObject elementData) {
                userId = elementData.optInt("id");
                userName = elementData.optString("name");
            }
        }

        int size() {
            return lastSeen.size();
        }

        boolean containsMessage(int msgId) {
            return lastSeen.containsKey(Integer.toString(msgId));
        }

        ArrayList<LastSeenUser> get(int msgId) {
            return lastSeen.get(Integer.toString(msgId));
        }
    }

    private static final int ITEM_NONE = -1;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);

    private ArrayList<ChatMessage> messages = new ArrayList<>();
    private int messageVersion;
    private LastSeenInfo lastSeenByOthers;
    private ChatMessage.LocalId lastDisplayedId;
    private int lastSeenByUserLocal = ITEM_NONE;
    private int lastSeenByUserServer;
    private int lastSeenVersion;
    private int tripId;

    private int retryCount = 0;
    private final ScheduledThreadPoolExecutor mNetworkThreadPool;

    private ChatMessage.LocalId savedPosition;

    // MARK: Properties
    public int count() {
        rwl.readLock().lock();
        int count = messages.size();
        rwl.readLock().unlock();

        return count;
    }

    private Double retryDelay() {
        if (retryCount > 0) {
            for (Map.Entry<Integer, Double> entry : retryDelays.entrySet()) {
                if (entry.getKey() <= retryCount) {
                    Log.d(LOG_TAG, "Retrying: count = " + retryCount + ", delay = " + entry.getValue());
                    return entry.getValue();
                }
            }
        }
        return 0.0;
    }

    @SuppressWarnings("unused")
    int unreadCount() {
        int count = 0;

        rwl.readLock().lock();
        for (ChatMessage msg: messages) {
            count += (msg.isStored() && msg.id > lastSeenByUserLocal) ? 1 : 0;
        }
        rwl.readLock().unlock();

        return count;
    }

    public void savePosition() {
        int pos = lastDisplayedItem();

        if (pos != ITEM_NONE) {
            savedPosition = messages.get(pos).localId;
        }
    }

    public boolean restorePosition() {
        if (savedPosition != null) {
            lastDisplayedId = savedPosition;
            savedPosition = null;
            return true;
        }
        return false;
    }

    public int lastDisplayedItem() {
        int item = ITEM_NONE;

        rwl.readLock().lock();
        if (lastDisplayedId != null) {
            //noinspection SuspiciousMethodCalls
            item = messages.indexOf(lastDisplayedId);
        } else if (lastSeenByUserLocal != ChatMessage.ID_NONE) {
            //item = messages.indexOf(lastSeenByUserLocal);  Doesn't work because indexOf uses .equals on the parameter not the array elements
            for (int i = 0; i < messages.size(); i++) {
                if (messages.get(i).id == lastSeenByUserLocal) {
                    item = i;
                    break;
                }
            }
        } else if (lastSeenByUserServer != ChatMessage.ID_NONE) {
            //item = messages.indexOf(lastSeenByUserServer);
            for (int i = 0; i < messages.size(); i++) {
                if (messages.get(i).id == lastSeenByUserServer) {
                    item = i;
                    break;
                }
            }
        }
        rwl.readLock().unlock();

        return item;
    }

    public ChatMessage get(int index) {
        ChatMessage message;

        rwl.readLock().lock();
        if (index >= this.messages.size()) {
            return null;
        }
        message = this.messages.get(index);
        lastDisplayedId = message.localId;
        if (message.id != ChatMessage.ID_NONE && message.id > lastSeenByUserLocal) {
            lastSeenByUserLocal = message.id;
            read(message);
        }

        if (message.id != ChatMessage.ID_NONE) {
            if (lastSeenByOthers.size() == 1 && lastSeenByOthers.containsMessage(message.id)) {
                message.lastSeenBy = new ArrayList<>();
                message.lastSeenBy.add(LAST_SEEN_BY_EVERYONE);
            } else if (lastSeenByOthers.containsMessage(message.id)) {
                message.lastSeenBy = new ArrayList<>();
                for (LastSeenInfo.LastSeenUser userInfo: lastSeenByOthers.get(message.id)) {
                    message.lastSeenBy.add(userInfo.userName);
                }
            } else {
                message.lastSeenBy = null;
            }
        } else {
            message.lastSeenBy = null;
        }
        rwl.readLock().unlock();

        return message;
    }


    // Appends a new message (no need to check for duplicates)
    public void append(ChatMessage msg) {
        rwl.writeLock().lock();
        messages.add(msg);
        save();
        rwl.writeLock().unlock();
    }

    // Adds a message from the server that may or may not be a duplicate (but will always have an ID)
    private void add(ChatMessage msg) {
        if (msg.id == ChatMessage.ID_NONE) {
            return;
        }

        // First check if message already exists and has been saved in non-blocking thread
        rwl.readLock().lock();
        int matchedIdx = messages.indexOf(msg);
        rwl.readLock().unlock();

        if (matchedIdx == ITEM_NONE) {
            // Message not found, updating array in thread safe manner
            rwl.writeLock().lock();
            int insertBeforeIdx = ITEM_NONE;
            for (int i = 0; i < messages.size(); i++) {
                if ( !messages.get(i).isStored() || messages.get(i).id >= msg.id) {
                    insertBeforeIdx = i;
                    break;
                }
            }
            int removeIdx = messages.indexOf(msg);

            if (insertBeforeIdx != ITEM_NONE && removeIdx != ITEM_NONE) {
                if (insertBeforeIdx == removeIdx) {
                    messages.set(insertBeforeIdx, msg);
                } else {
                    messages.remove(removeIdx);
                    messages.add(insertBeforeIdx, msg);
                }
            } else if (insertBeforeIdx != ITEM_NONE) {
                messages.add(insertBeforeIdx, msg);
            } else if (removeIdx != ITEM_NONE) {
                Log.e(LOG_TAG, "Inconsistent thread update");
            } else {
                messages.add(msg);
            }
            rwl.writeLock().unlock();
        }
    }


    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.putOpt(Constants.JSON.CHATTHREAD_TRIP_ID, tripId);
        jo.putOpt(Constants.JSON.CHATTHREAD_LAST_DISPLAYED_ID, (lastDisplayedId == null ? null : lastDisplayedId.toJSON()));
        jo.putOpt(Constants.JSON.CHATTHREAD_LAST_SEEN_USER_LOCAL, lastSeenByUserLocal);
        jo.putOpt(Constants.JSON.CHATTHREAD_LAST_SEEN_USER_SRV, lastSeenByUserServer);
        jo.putOpt(Constants.JSON.CHATTHREAD_LAST_SEEN_OTHERS, lastSeenByOthers);
        jo.putOpt(Constants.JSON.CHATTHREAD_MSG_VERSION, messageVersion);
        jo.putOpt(Constants.JSON.CHATTHREAD_LAST_SEEN_VERSION, lastSeenVersion);

        JSONArray jm = new JSONArray();
        if (messages != null) {
            for (ChatMessage m : messages) {
                jm.put(m.toJSON());
            }
        }
        jo.putOpt(Constants.JSON.CHATTHREAD_MESSAGES, jm);

        return jo;
    }

    // MARK: Constructors
    ChatThread(int tripId) {
        super();
        mNetworkThreadPool = new ScheduledThreadPoolExecutor(POOL_SIZE_MAXIMUM);
        this.tripId = tripId;
    }

    ChatThread(JSONObject elementData) {
        mNetworkThreadPool = new ScheduledThreadPoolExecutor(POOL_SIZE_MAXIMUM);
        messages = new ArrayList<>();

        if (elementData != null) {
            tripId = elementData.optInt(Constants.JSON.CHATTHREAD_TRIP_ID);
            lastDisplayedId = new ChatMessage.LocalId(elementData.optJSONObject(Constants.JSON.CHATTHREAD_LAST_DISPLAYED_ID));
            lastSeenByUserLocal = elementData.optInt(Constants.JSON.CHATTHREAD_LAST_SEEN_USER_LOCAL);
            lastSeenByUserServer = elementData.optInt(Constants.JSON.CHATTHREAD_LAST_SEEN_USER_SRV);
            messageVersion = elementData.optInt(Constants.JSON.CHATTHREAD_MSG_VERSION);
            lastSeenVersion = elementData.optInt(Constants.JSON.CHATTHREAD_LAST_SEEN_VERSION);

            lastSeenByOthers = new LastSeenInfo(elementData.optJSONObject(Constants.JSON.CHATTHREAD_LAST_SEEN_OTHERS));

            JSONArray jsonMessages = elementData.optJSONArray(Constants.JSON.CHATTHREAD_MESSAGES);
            if (jsonMessages != null) {
                for (int i = 0; i < jsonMessages.length(); i++) {
                    JSONObject jsonMessage = jsonMessages.optJSONObject(i);
                    messages.add(new ChatMessage(jsonMessage));
                }
            }
        }
    }


    // NB! Must be called inside read/write lock
    private ArrayList<ChatMessage> unsavedMessages() {
        ArrayList<ChatMessage> unsavedMessages = new ArrayList<>();
        for (ChatMessage msg: messages) {
            if (!msg.isStored()) {
                unsavedMessages.add(msg);
            }
        }
        return unsavedMessages;
    }

    // Response handler
    private class SaveResponseHandler implements ServerAPI.Listener {
        public void onRemoteCallComplete(JSONObject response) {
            retryCount = 0;
            save();
        }

        public void onRemoteCallFailed() {
            retryCount += 1;

            mNetworkThreadPool.schedule(ChatThread.this::performSave, retryDelay().longValue(), TimeUnit.SECONDS);
        }

        public void onRemoteCallFailed(Exception e) {
            retryCount += 1;

            mNetworkThreadPool.schedule(ChatThread.this::performSave, retryDelay().longValue(), TimeUnit.SECONDS);
        }
    }

    private class ReadResponseHandler implements ServerAPI.Listener {
        final ChatMessage messageBeingRead;

        ReadResponseHandler(ChatMessage msg) {
            messageBeingRead = msg;
        }

        public void onRemoteCallComplete(JSONObject response) {
            retryCount = 0;
            if (   response.has(Constants.JSON.CHATTHREAD_LAST_SEEN_BY_ME)
                && response.has(Constants.JSON.CHATTHREAD_LAST_SEEN_OTHERS)
                && response.has(Constants.JSON.CHATTHREAD_LAST_SEEN_VERSION) ) {

                int lastSeenByMe = response.optInt(Constants.JSON.CHATTHREAD_LAST_SEEN_BY_ME);
                if (lastSeenByMe > lastSeenByUserServer) {
                    lastSeenByUserServer = lastSeenByMe;
                }
                int respLastSeenVersion = response.optInt(Constants.JSON.CHATTHREAD_LAST_SEEN_VERSION);
                if (respLastSeenVersion > lastSeenVersion) {
                    lastSeenVersion = respLastSeenVersion;
                    lastSeenByOthers = new LastSeenInfo(response.optJSONObject(Constants.JSON.CHATTHREAD_LAST_SEEN_OTHERS));
                }

                Intent intent = new Intent(Constants.Notification.CHAT_UPDATED);
                LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
            }
        }

        public void onRemoteCallFailed() {
            retryCount += 1;
            mNetworkThreadPool.schedule(new ReadRunnable(messageBeingRead), retryDelay().longValue(), TimeUnit.SECONDS);
        }

        public void onRemoteCallFailed(Exception e) {
            retryCount += 1;
            mNetworkThreadPool.schedule(new ReadRunnable(messageBeingRead), retryDelay().longValue(), TimeUnit.SECONDS);
        }
    }

    private void performSave() {
        rwl.readLock().lock();
        ArrayList<ChatMessage> unsavedMessages = unsavedMessages();
        rwl.readLock().unlock();
        if (!unsavedMessages.isEmpty()) {
            unsavedMessages.get(0).save(tripId, new SaveResponseHandler());
        }
    }

    private void save() {
        mNetworkThreadPool.execute(this::performSave);
    }


    private void performRead(ChatMessage message) {
        if (!User.sharedUser.hasCredentials()) {
            return;
        }
        if (!message.isStored()) {
            return;
        }
        if (message.userId == User.sharedUser.getId()) {
            // No point in telling server user has read his own messages.
            return;
        }
        if (message.id < lastSeenByUserServer) {
            // Already marked as seen on the server, no need to update
            return;
        }
        message.read(tripId, new ReadResponseHandler(message));
    }

    private void read(ChatMessage message) {
        mNetworkThreadPool.execute(new ReadRunnable(message));
        //ChatThread.dqServerComm.async { self.performRead(message: message) }
    }

    private class LoadChatResponseHandler implements ServerAPI.Listener {
        final RefreshMode mode;

        LoadChatResponseHandler(RefreshMode refreshMode) {
            mode = refreshMode;
        }

        public void onRemoteCallComplete(JSONObject response) {
            retryCount = 0;

            if (   response.has(Constants.JSON.CHATTHREAD_MESSAGES)
                && response.has(Constants.JSON.CHATTHREAD_LAST_SEEN_OTHERS)
                && response.has(Constants.JSON.CHATTHREAD_MSG_VERSION)) {
                lastSeenByUserServer = response.optInt(Constants.JSON.CHATTHREAD_LAST_SEEN_BY_ME);
                lastSeenByOthers = new LastSeenInfo(response.optJSONObject(Constants.JSON.CHATTHREAD_LAST_SEEN_OTHERS));
                JSONArray jsonMessages = response.optJSONArray(Constants.JSON.CHATTHREAD_MESSAGES);
                if (jsonMessages != null && jsonMessages.length() > 0) {
                    int srvMessageVersion = response.optInt(Constants.JSON.CHATTHREAD_MSG_VERSION);
                    if (mode == RefreshMode.INCREMENTAL) {
                        if (srvMessageVersion > messageVersion) {
                            for (int i = 0; i < jsonMessages.length(); i++) {
                                add(new ChatMessage(jsonMessages.optJSONObject(i)));
                            }
                        }
                        Log.d("ChatThread.Load", "Refreshed incrementally, notifying screen");
                    } else {
                        rwl.writeLock().lock();
                        ArrayList<ChatMessage> newMessages = new ArrayList<>();
                        for (int i = 0; i < jsonMessages.length(); i++) {
                            newMessages.add(new ChatMessage(jsonMessages.optJSONObject(i)));
                        }
                        ArrayList<ChatMessage> unsavedMessages = unsavedMessages();
                        for (ChatMessage msg: unsavedMessages) {
                            if (!newMessages.contains(msg)) {
                                newMessages.add(msg);
                            }
                        }
                        messages = newMessages;
                        rwl.writeLock().unlock();
                    }
                    TripList.getSharedList().saveToArchive();
                }
                notifyChanged();
                Intent intent = new Intent(Constants.Notification.CHAT_UPDATED);
                LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
            } else {
                Log.e("ChatThread.Load", "Incorrect response: " + response);
            }
        }

        public void onRemoteCallFailed() {
            Intent intent = new Intent(Constants.Notification.COMMUNICATION_FAILED);
            LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
        }

        public void onRemoteCallFailed(Exception e) {
            Intent intent = new Intent(Constants.Notification.COMMUNICATION_FAILED);
            LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
        }
    }

    public void refresh(RefreshMode mode) {
        ServerAPI.Params params = new ServerAPI.Params(ServerAPI.URL_BASE, ServerAPI.RESOURCE_CHAT, Integer.toString(tripId), null, null);
        params.addParameter(ServerAPI.Param.USER_NAME, User.sharedUser.getUserName());
        params.addParameter(ServerAPI.Param.PASSWORD, User.sharedUser.getPassword());
        params.addParameter(ServerAPI.Param.LANGUAGE, Locale.getDefault().getLanguage());

        if (messageVersion != ChatMessage.ID_NONE && mode == RefreshMode.INCREMENTAL) {
            params.addParameter(ServerAPI.Param.LAST_MESSAGE_ID, Integer.toString(messageVersion));
        }

        new ServerAPI(new LoadChatResponseHandler(mode)).execute(params);
    }

    public void updateReadStatus(JSONObject jsonLastSeenByUsers, int lastSeenVersion) {
        // TODO: Perform in background
        if (lastSeenVersion <= this.lastSeenVersion) {
            return;
        }

        rwl.writeLock().lock();
        lastSeenByOthers = new LastSeenInfo(jsonLastSeenByUsers);
        Log.d(LOG_TAG, "Updated last seen by me: " + lastSeenByUserServer + ", other users: " + lastSeenByOthers);
        rwl.writeLock().unlock();
        notifyChanged();
        Intent intent = new Intent(Constants.Notification.CHAT_UPDATED);
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }


    private class ReadRunnable implements Runnable {
        final ChatMessage messageToRead;

        ReadRunnable(ChatMessage msg) {
            messageToRead = msg;
        }
        @Override
        public void run() {
            performRead(messageToRead);
        }
    }
}
