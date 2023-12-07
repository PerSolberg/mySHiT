package no.shitt.myshit.model;

import static java.util.Map.entry;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import no.shitt.myshit.AlarmReceiver;
import no.shitt.myshit.Constants;
import no.shitt.myshit.R;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.TripDetailsActivity;
import no.shitt.myshit.TripDetailsPopupActivity;
import no.shitt.myshit.helper.IconCache;
import no.shitt.myshit.helper.JSONable;
import no.shitt.myshit.helper.ServerAPI;
import no.shitt.myshit.helper.ServerDate;
import no.shitt.myshit.helper.StringUtil;

public class Trip implements ServerAPI.Listener, JSONable {
    //
    // Constants
    //
    private final static String LOG_TAG = Trip.class.getSimpleName();
    private final static String iconBaseName = "icon_trip_";
    // Minutes between notifications for same trip element (in milliseconds)
    private final static int MINIMUM_NOTIFICATION_SEPARATION = 10 * 60 * 1000;
    private final static int LEAD_TIME_MISSING = -1;
    private final static String UNKNOWN_TYPE = "UNKNOWN";
    private static final Map<Tense,String> tenseNames = Map.ofEntries(entry(Tense.PAST, "historic")
                                                                     ,entry(Tense.PRESENT, "active")
    );
    private static final String defaultTenseName = "default";

    public enum ActivityType {
        REGULAR, POPUP
    }

    public final int id;
    private int itineraryId;
    private String startDateText;  // Hold original value for saving in archive
    private String startTimezone;  // Hold original value for saving in archive
    private Date startDate;
    private String endDateText;    // Hold original value for saving in archive
    private String endTimezone;    // Hold original value for saving in archive
    private Date endDate;
    public String tripDescription;
    public String code;
    public String name;
    private String type;
    @SuppressWarnings("CanBeFinal")
    private int elementCount;
    public List<AnnotatedTripElement> elements;
    public final ChatThread chatThread;
    private ServerTimestamp lastUpdateTS;

    private boolean changed;

    // Notifications created for this element (used to avoid recreating notifications after they have been triggered)
    private final Map<String,NotificationInfo> notifications;

    public class TripIconKey extends IconCache.IconKey {
        final String type;
        final Tense  tense;


        TripIconKey(String type, Tense tense) {
            this.type = type;
            this.tense = tense;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null)
                return false;

            if (getClass() == o.getClass()) {
                TripIconKey otherKey = (TripIconKey) o;

                boolean sameType = (this.type == null && otherKey.type == null)
                        || (this.type != null && otherKey.type != null && this.type.equals(otherKey.type));
                boolean sameTense = (this.tense == null && otherKey.tense == null)
                        || (this.tense != null && otherKey.tense != null && this.tense.equals(otherKey.tense));
                return sameType && sameTense;
            }
            return false;
        }


        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + (type == null ? 0 : type.hashCode());
            hash = 31 * hash + (tense == null ? 0 : tense.hashCode());
            return hash;
        }


        @Override
        public IconCache.IconKey getParent() {
            if (tense != Tense.FUTURE) {
                return new TripIconKey(type, Tense.FUTURE);
            } else if ( ! UNKNOWN_TYPE.equals(type) ) {
                return new TripIconKey(UNKNOWN_TYPE, Tense.FUTURE);
            } else {
                return null;
            }
        }


        public String downloadPath() {
            StringBuilder path = new StringBuilder("https://shitt.no/mySHiT/v2/icons/trip/");
            StringUtil.appendWithTrailingSeparator(path, type, ".", false);
            path.append(getTenseNameWithDefault()).append(".xxxhdpi.png");
            return path.toString();

        }


        private String getTenseName() {
            return tenseNames.get(tense);
        }

        private String getTenseNameWithDefault(String defaultName) {
            String tenseName = getTenseName();
            if (tenseName == null) {
                return defaultName;
            }
            return tenseName;
        }

        private String getTenseNameWithDefault() {
            return getTenseNameWithDefault(defaultTenseName);
        }

        public String name() {
            StringBuilder name = new StringBuilder(iconBaseName);
            StringUtil.appendWithTrailingSeparator(name, getTenseName(), "_", false);
            name.append(type);
            return name.toString();
        }
    }

    public Date getStartTime() {
        return startDate;
    }

    private String getStartTimeZone() { return startTimezone; }

    private Date getEndTime() {
        return endDate;
    }

    public String getTitle() {
        return name;
    }

    public String getDateInfo() {
        Context ctx = SHiTApplication.getContext();
        return DateUtils.formatDateRange(ctx, startDate.getTime(), endDate.getTime(), DateUtils.FORMAT_SHOW_DATE + DateUtils.FORMAT_ABBREV_MONTH);
    }

    @SuppressWarnings("unused")
    public String getDetailInfo() {
        return tripDescription;
    }

    public Tense getTense() {
        Date startTime = getStartTime();
        if (startTime != null) {
            Date today = new Date();

            // If end time isn't set, assume duration of 1 day
            Date endTime = getEndTime();
            if (endTime == null) {
                Calendar cal = GregorianCalendar.getInstance();
                cal.setTime(startTime);
                cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
                endTime = cal.getTime();
            }

            if (today.after(endTime)) {
                return Tense.PAST;
            } else if (today.before(startTime)) {
                return Tense.FUTURE;
            } else {
                return Tense.PRESENT;
            }
        } else {
            return Tense.FUTURE;
        }
    }


    public Icon getIcon() {
        TripIconKey ik = new TripIconKey(type, getTense());
        return IconCache.getSharedCache().get(ik, (icon) -> {
            Log.d(LOG_TAG, "getIcon callback");
            Intent intent = new Intent(Constants.Notification.TRIPS_LOADED);
            LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
        });
    }


    private boolean detailsLoaded() {
        return ( elements != null && elements.size() != 0 && elementCount > 0 );
    }


    // Encode to JSON for saving to file
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.put(Constants.JSON.TRIP_ID, id);
        jo.put(Constants.JSON.TRIP_ITINERARY_ID, itineraryId);
        jo.putOpt(Constants.JSON.TRIP_START_DATE, startDateText);
        jo.putOpt(Constants.JSON.TRIP_START_TIMEZONE, startTimezone);
        jo.putOpt(Constants.JSON.TRIP_END_DATE, endDateText);
        jo.putOpt(Constants.JSON.TRIP_END_TIMEZONE, endTimezone);
        jo.putOpt(Constants.JSON.TRIP_DESCRIPTION, tripDescription);
        jo.putOpt(Constants.JSON.TRIP_CODE, code);
        jo.putOpt(Constants.JSON.TRIP_NAME, name);
        jo.putOpt(Constants.JSON.TRIP_TYPE, type);
        jo.putOpt(Constants.JSON.TRIP_ELEMENT_COUNT, elementCount);

        JSONArray jate = new JSONArray();
        if (elements != null) {
            for (AnnotatedTripElement ate : elements) {
                jate.put(ate.toJSON());
            }
        }
        jo.put(Constants.JSON.TRIP_ELEMENTS, jate);

        JSONObject jani = new JSONObject();
        if (notifications != null) {
            for (String niKey : notifications.keySet()) {
                NotificationInfo ni = Objects.requireNonNull(notifications.get(niKey));
                jani.put(niKey, ni.toJSON());
            }
        }
        jo.put(Constants.JSON.TRIP_NOTIFICATIONS, jani);
        jo.put(Constants.JSON.TRIP_CHAT, chatThread.toJSON());

        return jo;
    }


    Trip(JSONObject elementData) {
        super();
        changed = false;
        id = elementData.optInt(Constants.JSON.TRIP_ID, -1);
        itineraryId = elementData.optInt(Constants.JSON.TRIP_ITINERARY_ID, -1);
        startDateText = elementData.isNull(Constants.JSON.TRIP_START_DATE) ? null : elementData.optString(Constants.JSON.TRIP_START_DATE);
        startTimezone = elementData.isNull(Constants.JSON.TRIP_START_TIMEZONE) ? null : elementData.optString(Constants.JSON.TRIP_START_TIMEZONE);
        startDate = ServerDate.convertServerDate(startDateText, startTimezone);
        endDateText = elementData.isNull(Constants.JSON.TRIP_END_DATE) ? null : elementData.optString(Constants.JSON.TRIP_END_DATE);
        endTimezone = elementData.isNull(Constants.JSON.TRIP_END_TIMEZONE) ? null : elementData.optString(Constants.JSON.TRIP_END_TIMEZONE);
        endDate = ServerDate.convertServerDate(endDateText, endTimezone);
        tripDescription = elementData.isNull(Constants.JSON.TRIP_DESCRIPTION) ? null : elementData.optString(Constants.JSON.TRIP_DESCRIPTION);
        code = elementData.isNull(Constants.JSON.TRIP_CODE) ? null : elementData.optString(Constants.JSON.TRIP_CODE);
        name = elementData.isNull(Constants.JSON.TRIP_NAME) ? null : elementData.optString(Constants.JSON.TRIP_NAME);
        type = elementData.isNull(Constants.JSON.TRIP_TYPE) ? null : elementData.optString(Constants.JSON.TRIP_TYPE);
        elementCount = elementData.optInt(Constants.JSON.TRIP_ELEMENT_COUNT, -1);
        JSONArray tripElements = elementData.optJSONArray(Constants.JSON.TRIP_ELEMENTS);
        if (tripElements != null) {
            elements = new ArrayList<>();
            for (int i = 0; i < tripElements.length(); i++) {
                JSONObject srvElement = tripElements.optJSONObject(i);
                AnnotatedTripElement ate = new AnnotatedTripElement(id, code, srvElement);
                elements.add(ate);
            }
        }

        notifications = new HashMap<>();
        JSONObject tripNotifications = elementData.optJSONObject(Constants.JSON.TRIP_NOTIFICATIONS);
        if (tripNotifications != null) {
            for (Iterator<String> ntfTypes = tripNotifications.keys(); ntfTypes.hasNext();) {
                String ntfType = ntfTypes.next();
                JSONObject ntfJSON = Objects.requireNonNull(tripNotifications.optJSONObject(ntfType));
                NotificationInfo ntf = new NotificationInfo(ntfJSON);
                notifications.put(ntfType, ntf);
            }
        }

        if (elementData.has(Constants.JSON.TRIP_CHAT) && elementData.optJSONObject(Constants.JSON.TRIP_CHAT) != null) {
            chatThread = new ChatThread(elementData.optJSONObject(Constants.JSON.TRIP_CHAT));
        } else {
            chatThread = new ChatThread(id);
        }

        registerForPushNotifications();
    }


    //
    // MARK: Field update functions
    //
    private int updateField(int oldValue, int newValue) {
        changed = changed || (oldValue != newValue);
        return newValue;
    }


    private String updateField(String oldValue, JSONObject json, String elementName) {
        String newValue = json.isNull(elementName) ? null : json.optString(elementName);
        changed = changed || !Objects.equals(oldValue, newValue);
        return newValue;
    }


    //
    // MARK: Methods
    //
    boolean update(JSONObject elementData, ServerTimestamp updateTS) {
        Log.d(LOG_TAG, "Updating trip with data: " + elementData);
        int elemId = elementData.optInt(Constants.JSON.TRIP_ID, -1);
        if (elemId != id) {
            Log.e(LOG_TAG, "Update error: Trip ID mismatch: " + elementData);
            return false;
        }

        if (lastUpdateTS != null && lastUpdateTS.after(updateTS)) {
            // Old update - ignore
            Log.d(LOG_TAG, "Ignoring old update");
            return false;
        }

        changed = false;

        lastUpdateTS = updateTS;
        itineraryId = updateField(itineraryId, elementData.optInt(Constants.JSON.TRIP_ITINERARY_ID, -1));
        startDateText = updateField(startDateText, elementData, Constants.JSON.TRIP_START_DATE);
        startTimezone = updateField(startTimezone, elementData, Constants.JSON.TRIP_START_TIMEZONE);
        startDate = ServerDate.convertServerDate(startDateText, startTimezone);
        endDateText = updateField(endDateText, elementData, Constants.JSON.TRIP_END_DATE);
        endTimezone = updateField(endTimezone, elementData, Constants.JSON.TRIP_END_TIMEZONE);
        endDate = ServerDate.convertServerDate(endDateText, endTimezone);
        tripDescription = updateField(tripDescription, elementData, Constants.JSON.TRIP_DESCRIPTION);
        code = updateField(code, elementData, Constants.JSON.TRIP_CODE);
        name = updateField(name, elementData, Constants.JSON.TRIP_NAME);
        type = updateField(type, elementData, Constants.JSON.TRIP_TYPE);
        elementCount = updateField(elementCount, elementData.optInt(Constants.JSON.TRIP_ELEMENT_COUNT, -1));

        JSONArray tripElements = elementData.optJSONArray(Constants.JSON.TRIP_ELEMENTS);
        if (tripElements != null) {
            boolean detailsAlreadyLoaded = detailsLoaded();
            boolean elementsChanged = updateElements(tripElements);
            if (detailsAlreadyLoaded) {
                changed = changed || elementsChanged;
            }
        }

        if (changed) {
            setNotification();
        }

        return changed;
    }


    private boolean updateElements(JSONArray elementList) {
        // This function should only be called if we received details from the server
        boolean detailsAlreadyLoaded = detailsLoaded();
        if (elements == null) {
            elements = new ArrayList<>();
        }

        List<Integer> elementIDs = new ArrayList<>(elementList.length());
        boolean added = false;
        boolean changed = false;

        // Add or update trips received from server
        for (int i = 0; i < elementList.length(); i++) {
            JSONObject srvElement = elementList.optJSONObject(i);
            int elementId = srvElement.optInt(Constants.JSON.ELEM_ID, -1);
            elementIDs.add(elementId);

            AnnotatedTripElement ate = elementById(elementId);
            if (ate != null) {
                boolean elementChanged = ate.tripElement.update(srvElement);
                ate.modified = elementChanged ? ChangeState.CHANGED : ate.modified;
                changed = changed || elementChanged;
            } else {
                AnnotatedTripElement newATE = new AnnotatedTripElement(id, code, srvElement);
                newATE.modified = detailsAlreadyLoaded ? ChangeState.NEW : ChangeState.UNCHANGED;
                elements.add(newATE);
                added = true;
            }
        }

        // Remove elements no longer in list
        for (int i = elements.size() - 1; i >= 0; i-- ) {
            int elementId = elements.get(i).tripElement.id;
            if ( ! elementIDs.contains(elementId) ) {
                changed = true;
                elements.remove(i);
            }
        }

        // If new elements were added, sort the list in same order as the server list
        if (added) {
            elements.sort(Comparator.comparingInt(ate -> elementIDs.indexOf(ate.tripElement.id)));
            changed = true;
        }

        if (changed) {
            setNotification();
        }

        return changed;
    }


    public boolean areTripElementsUnchanged() {
        if (elements != null) {
            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i).modified == ChangeState.NEW || elements.get(i).modified == ChangeState.CHANGED) {
                    return false;
                }
            }
        }
        return true;
    }


    public String startTime(int dateTimeStyle) {
        if (getStartTime() != null && getStartTimeZone() != null) {
            Formatter f = new Formatter(new StringBuilder(50), Locale.getDefault());
            long time = getStartTime().getTime();
            return android.text.format.DateUtils.formatDateRange(SHiTApplication.getContext(), f, time, time, dateTimeStyle, getStartTimeZone()).toString();
        } else if (getStartTime() != null) {
            return startDateText;
        }
        return null;
    }


    public int elementCount() {
        if (elements == null)
            return 0;
        else
            return elements.size();
    }


    public boolean hasElements() {
        return ( (elements == null) ? elementCount : elements.size() ) > 0;
    }


    public boolean elementsLoaded() {
        return (elements != null);
    }


    public AnnotatedTripElement elementById(int elementId) {
        for (int i = 0; i < elements.size(); i++) {
            if (elementId == elements.get(i).tripElement.id) {
                return elements.get(i);
            }
        }
        return null;
    }


    public AnnotatedTripElement elementByPosition(int position) {
        if (position >= 0 && position < elements.size())
            return elements.get(position);
        else
            return null;
    }


    private void setNotification(String notificationType, int leadTime, int alertMessageId, Bundle userInfo) {
        NotificationInfo oldInfo = notifications.get(notificationType);
        NotificationInfo newInfo = new NotificationInfo(getStartTime(), leadTime);

        if (oldInfo == null || oldInfo.needsRefresh(newInfo)) {
            boolean combined = false;

            Bundle extras = new Bundle();
            if (userInfo != null) {
                extras.putAll(userInfo);
            }
            extras.putString(Constants.NotificationUserInfo.LEAD_TIME_TYPE, notificationType);
            extras.putInt(Constants.NotificationUserInfo.TRIP_ID, id);
            if (getStartTimeZone() != null) {
                extras.putString(Constants.NotificationUserInfo.TIMEZONE, getStartTimeZone());
            }

            for (String nType : notifications.keySet()) {
                if (!nType.equals(notificationType)) {
                    NotificationInfo n = Objects.requireNonNull(notifications.get(nType));
                    if (n.getNotificationDate().before(newInfo.getNotificationDate())
                            && (newInfo.getNotificationDate().getTime() - n.getNotificationDate().getTime()) < Trip.MINIMUM_NOTIFICATION_SEPARATION) {
                        newInfo.combine(n);
                        combined = true;
                    }
                }
            }

            if (!combined) {
                Context ctx = SHiTApplication.getContext();
                Calendar alarmTime = Calendar.getInstance();
                alarmTime.setTime(newInfo.getNotificationDate());

                // Set up information to be passed to AlarmReceiver
                extras.putString(Constants.IntentExtra.TRIP_CODE, code);
                extras.putString(Constants.IntentExtra.TITLE, getTitle());

                //extras.putAll(actualUserInfo);
                long actualLeadTime = getStartTime().getTime() - alarmTime.getTimeInMillis();
                String leadTimeText = ServerDate.formatInterval(actualLeadTime);

                extras.putString(Constants.IntentExtra.MESSAGE, ctx.getString(alertMessageId, leadTimeText, startTime(DateUtils.FORMAT_SHOW_TIME)));

                AlarmReceiver alarm = new AlarmReceiver();
                alarm.setAlarm(alarmTime.getTime()
                        , Uri.parse("alarm://shitt.no/" + notificationType + "/" + code + "/" + id)
                        , Constants.PushNotificationActions.TRIP_CLICK
                        , extras);
            } else {
                Log.d(LOG_TAG, "Not setting " + notificationType + " notification for trip " + id + " combined with other notification");
            }

            notifications.put(notificationType, newInfo);
        } else {
            Log.d(LOG_TAG, "Not refreshing " + notificationType + " notification for trip  " + id + ", already triggered");
        }
    }


    private void setNotification() {
        if (getTense() == Tense.FUTURE) {
            int leadTimeTripHours = SHiTApplication.getPreferenceInt(Constants.Setting.ALERT_LEAD_TIME_TRIP, LEAD_TIME_MISSING);

            if (leadTimeTripHours != LEAD_TIME_MISSING) {
                setNotification(Constants.Setting.ALERT_LEAD_TIME_TRIP, leadTimeTripHours * 60, R.string.alert_msg_trip, null);
            }
        }
    }


    public void onRemoteCallComplete(JSONObject response) {
        int count = response.optInt(ServerAPI.ResultItem.COUNT, -1);
        if (count == 1) {
            JSONArray results = response.optJSONArray(ServerAPI.ResultItem.TRIP_LIST);
            JSONObject serverData = null;
            if (results != null && results.length() == 1) {
                serverData = results.optJSONObject(0);
            }
            if (serverData != null) {
                update(serverData, null);
            }
        }

        Intent intent = new Intent(Constants.Notification.TRIP_DETAILS_LOADED);
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }


    public void onRemoteCallFailed() {
        Intent intent = new Intent(Constants.Notification.COMMUNICATION_FAILED);
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }


    public void onRemoteCallFailed(Exception e) {
        Intent intent = new Intent(Constants.Notification.COMMUNICATION_FAILED);
        intent.putExtra("message", e.getMessage());
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }


    void refreshNotifications() {
        Log.d(LOG_TAG, "Refreshing trip notifications");
        setNotification();
        if (elements != null) {
            for (int i = 0; i < elements.size(); i++) {
                elements.get(i).tripElement.setNotification();
            }
        }
    }


    private void registerForPushNotifications() {
        String topicTrip = Constants.PushNotification.TOPIC_ROOT_TRIP + id;
        FirebaseMessaging.getInstance().subscribeToTopic(topicTrip);

        if (itineraryId > 0) {
            String topicItinerary = Constants.PushNotification.TOPIC_ROOT_ITINERARY + itineraryId;
            FirebaseMessaging.getInstance().subscribeToTopic(topicItinerary);
        }
    }


    void deregisterFromPushNotifications() {
        String topicTrip = Constants.PushNotification.TOPIC_ROOT_TRIP + id;
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicTrip);

        if (itineraryId > 0) {
            String topicItinerary = Constants.PushNotification.TOPIC_ROOT_TRIP + id;
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topicItinerary);
        }
    }


    public void loadDetails() {
        ServerAPI.Params params = new ServerAPI.Params(ServerAPI.URL_BASE, ServerAPI.RESOURCE_TRIP, code, null, null);
        params.addParameter(ServerAPI.Param.USER_NAME, User.sharedUser.getUserName());
        params.addParameter(ServerAPI.Param.PASSWORD, User.sharedUser.getPassword());
        params.addParameter(ServerAPI.Param.LANGUAGE, Locale.getDefault().getLanguage());

        new ServerAPI(this).execute(params);
    }


    public Intent getActivityIntent(ActivityType activityType) {
        Intent i;
        switch (activityType) {
            case REGULAR:
                i = new Intent(SHiTApplication.getContext(), TripDetailsActivity.class);
                break;

            case POPUP:
                i = new Intent(SHiTApplication.getContext(), TripDetailsPopupActivity.class);
                i.putExtra(Constants.PushNotificationKeys.TRIP_ID, String.valueOf(id));
                break;

            default:
                return null;
        }

        i.putExtra(Constants.IntentExtra.TRIP_CODE, code);
        i.putExtra(Constants.IntentExtra.TRIP_ID, String.valueOf(id));

        return i;
    }

}
