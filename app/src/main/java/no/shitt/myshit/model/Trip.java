package no.shitt.myshit.model;

//import android.text.format.Formatter;

import android.content.Context;
import android.content.Intent;
//import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
//import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
//import java.text.DateFormat;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import no.shitt.myshit.AlarmReceiver;
import no.shitt.myshit.Constants;
import no.shitt.myshit.R;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.SchedulingService;
import no.shitt.myshit.helper.JSONable;
import no.shitt.myshit.helper.ServerAPI;
import no.shitt.myshit.helper.ServerDate;
import no.shitt.myshit.helper.StringUtil;

public class Trip implements ServerAPI.Listener, JSONable {
    public int id;
    private int itineraryId;
    private final String startDateText;  // Hold original value for saving in archive
    private Date startDate;
    private final String endDateText;    // Hold original value for saving in archive
    private Date endDate;
    public String tripDescription;
    public String code;
    public String name;
    public String type;
    public List<AnnotatedTripElement> elements;
    public ChatThread chatThread;

    // Notifications created for this element (used to avoid recreating notifications after they have been triggered)
    private Map<String,NotificationInfo> notifications;

    private final static String iconBaseName = "icon_trip_";
    // Minutes between notifications for same trip element (in milliseconds)
    private static final int MINIMUM_NOTIFICATION_SEPARATION = 10 * 60 * 1000;
    private static final int LEAD_TIME_MISSING = -1;


    public Date getStartTime() {
        return startDate;
    }

    private String getStartTimeZone() {
        if (elements != null && elements.size() > 0) {
            return elements.get(0).tripElement.getStartTimeZone();
        }
        return null;
    }

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

    public int getIconId() {
        Context ctx = SHiTApplication.getContext();
        String path = iconBaseName;
        String iconName;
        int    iconId;

        switch (getTense()) {
        case PAST:
            path = iconBaseName + "historic_";
            break;
        case PRESENT:
            path = iconBaseName + "active_";
            break;
        case FUTURE:
            path = iconBaseName;
            break;
        }
        iconName = path + type;

        // First try exact match
        iconId = ctx.getResources().getIdentifier(iconName.toLowerCase(), "mipmap", ctx.getPackageName());
        if (iconId != 0) {
            return iconId;
        }

        // Try default variant for trip type
        iconName = iconBaseName + type;
        iconId = ctx.getResources().getIdentifier(iconName.toLowerCase(), "mipmap", ctx.getPackageName());
        if (iconId != 0) {
            return iconId;
        }

        // Try dummy image
        iconName = iconBaseName + "UNKNOWN";
        iconId = ctx.getResources().getIdentifier(iconName.toLowerCase(), "mipmap", ctx.getPackageName());
        if (iconId != 0) {
            return iconId;
        }

        return 0;
    }


    // MARK: Factory
    private static Trip createFromDictionary( JSONObject elementData, boolean fromServer ) {
        return new Trip(elementData, fromServer);
    }

    // Encode to JSON for saving to file
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.put(Constants.JSON.TRIP_ID, id);
        jo.put(Constants.JSON.TRIP_ITINERARY_ID, itineraryId);
        jo.putOpt(Constants.JSON.TRIP_START_DATE, startDateText);
        jo.putOpt(Constants.JSON.TRIP_END_DATE, endDateText);
        jo.putOpt(Constants.JSON.TRIP_DESCRIPTION, tripDescription);
        jo.putOpt(Constants.JSON.TRIP_CODE, code);
        jo.putOpt(Constants.JSON.TRIP_NAME, name);
        jo.putOpt(Constants.JSON.TRIP_TYPE, type);

        JSONArray jate = new JSONArray();
        if (elements != null) {
            for (AnnotatedTripElement ate : elements) {
                jate.put(ate.toJSON());
            }
            /* Iterator i = elements.iterator();
            while (i.hasNext()) {
                AnnotatedTripElement ate = (AnnotatedTripElement) i.next();
                jate.put(ate.toJSON());
            } */
        }
        jo.put(Constants.JSON.TRIP_ELEMENTS, jate);

        JSONObject jani = new JSONObject();
        if (notifications != null) {
            for (String niKey : notifications.keySet()) {
                NotificationInfo ni = notifications.get(niKey);
                jani.put(niKey, ni.toJSON());
            }
        }
        jo.put(Constants.JSON.TRIP_NOTIFICATIONS, jani);
        jo.put(Constants.JSON.TRIP_CHAT, chatThread.toJSON());

        return jo;
    }

    Trip(JSONObject elementData, boolean fromServer) {
        super();
        id = elementData.optInt(Constants.JSON.TRIP_ID, -1);
        itineraryId = elementData.optInt(Constants.JSON.TRIP_ITINERARY_ID, -1);
        startDateText = elementData.isNull(Constants.JSON.TRIP_START_DATE) ? null : elementData.optString(Constants.JSON.TRIP_START_DATE);
        startDate = ServerDate.convertServerDate(startDateText, null);
        endDateText = elementData.isNull(Constants.JSON.TRIP_END_DATE) ? null : elementData.optString(Constants.JSON.TRIP_END_DATE);
        endDate = ServerDate.convertServerDate(endDateText, null);
        tripDescription = elementData.isNull(Constants.JSON.TRIP_DESCRIPTION) ? null : elementData.optString(Constants.JSON.TRIP_DESCRIPTION);
        code = elementData.isNull(Constants.JSON.TRIP_CODE) ? null : elementData.optString(Constants.JSON.TRIP_CODE);
        name = elementData.isNull(Constants.JSON.TRIP_NAME) ? null : elementData.optString(Constants.JSON.TRIP_NAME);
        type = elementData.isNull(Constants.JSON.TRIP_TYPE) ? null : elementData.optString(Constants.JSON.TRIP_TYPE);
        JSONArray tripElements = elementData.optJSONArray(Constants.JSON.TRIP_ELEMENTS);
        if (tripElements != null) {
            elements = new ArrayList<>();
            for (int i = 0; i < tripElements.length(); i++) {
                JSONObject srvElement = tripElements.optJSONObject(i);
                if (fromServer) {
                    TripElement tripElement = TripElement.createFromDictionary(id, code, srvElement);
                    tripElement.setNotification();
                    elements.add( new AnnotatedTripElement(tripElement));
                } else {
                    AnnotatedTripElement annotatedElement = new AnnotatedTripElement(id, code, srvElement);
                    annotatedElement.tripElement.setNotification();
                    elements.add(annotatedElement);
                }
            }
        }

        notifications = new HashMap<>();
        JSONObject tripNotifications = elementData.optJSONObject(Constants.JSON.TRIP_NOTIFICATIONS);
        if (tripNotifications != null) {
            for (Iterator<String> ntfTypes = tripNotifications.keys(); ntfTypes.hasNext();) {
                String ntfType = ntfTypes.next();
                JSONObject ntfJSON = tripNotifications.optJSONObject(ntfType);
                NotificationInfo ntf = new NotificationInfo(ntfJSON);
                notifications.put(ntfType, ntf);
            }
        }

        if (elementData.has(Constants.JSON.TRIP_CHAT) && elementData.optJSONObject(Constants.JSON.TRIP_CHAT) != null) {
            chatThread = new ChatThread(elementData.optJSONObject(Constants.JSON.TRIP_CHAT));
        } else {
            chatThread = new ChatThread(id);
        }

        setNotification();
        registerForPushNotifications();
    }

    // MARK: Methods
    boolean isEqual(Object otherObject) {
        if (this.getClass() != otherObject.getClass()) {
            return false;
        }
        try {
            Trip otherTrip = (Trip) otherObject;
            if (this.id              != otherTrip.id                              ) { return false; }
            if (this.itineraryId     != otherTrip.itineraryId                     ) { return false; }
            if (!ServerDate.equal(this.startDate, otherTrip.startDate)            ) { return false; }
            if (!ServerDate.equal(this.endDate, otherTrip.endDate)                ) { return false; }
            if (!StringUtil.equal(this.tripDescription, otherTrip.tripDescription)) { return false; }
            if (!StringUtil.equal(this.code, otherTrip.code)                      ) { return false; }
            if (!StringUtil.equal(this.name, otherTrip.name)                      ) { return false; }
            if (!StringUtil.equal(this.type, otherTrip.type)                      ) { return false; }

            return areTripElementsUnchanged();
        }
        catch (Exception e) {
            return false;
        }
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

    void compareTripElements(Trip otherTrip) {
        if (elements == null || otherTrip.elements == null || elements.size() == 0 || otherTrip.elements.size() == 0) {
            //Log.d("Trip", "compareTripElements: Empty element list in one or both trips");
            return;
        }

        // Determine changes
        for (int i = 0; i < elements.size(); i++) {
            AnnotatedTripElement e1 = elements.get(i);
            AnnotatedTripElement e2 = null;
            for (int j = 0; j < otherTrip.elements.size(); j++) {
                if (otherTrip.elements.get(j).tripElement.id == e1.tripElement.id) {
                    //Log.d("Trip", "compareTripElements: Found matching element for " + e1.tripElement.id);
                    e2 = otherTrip.elements.get(j);
                    break;
                }
            }
            if (e2 == null) {
                //Log.d("Trip", "compareTripElements: Did not find matching element for " + e1.tripElement.id);
                e1.modified = ChangeState.NEW;
            } else if (!e1.tripElement.isEqual(e2.tripElement)) {
                //Log.d("Trip", "compareTripElements: Differences in element " + e1.tripElement.id);
                e1.modified = ChangeState.CHANGED;
            } else {
                // Keep modification flag from old trip
                e1.modified = e2.modified;
            }
        }
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

    private void setNotification(String notificationType, int leadTime, int alertMessageId, /*Map<String,Object>*/ Bundle userInfo) {
        // Logic starts here
        NotificationInfo oldInfo = notifications.get(notificationType);  //TODO: Check what happens if key doesn't exist
        NotificationInfo newInfo = new NotificationInfo(getStartTime(), leadTime);

        if (oldInfo == null || oldInfo.needsRefresh(newInfo)) {
            boolean combined = false;

            Log.d("Trip", "Setting " + notificationType + " notification for trip " + id + " at " + newInfo.getNotificationDate());

            Bundle extras = new Bundle();
            //Map<String,Object> actualUserInfo = new HashMap<>();
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
                    NotificationInfo n = notifications.get(nType);
                    if (n.getNotificationDate().before(newInfo.getNotificationDate())
                            && (newInfo.getNotificationDate().getTime() - n.getNotificationDate().getTime()) < Trip.MINIMUM_NOTIFICATION_SEPARATION) {
                        newInfo.combine(n);
                        combined = true;
                    }
                }
            }

            if (!combined) {
                // iOS
                // notification.soundName = UILocalNotificationDefaultSoundName
                // notification.userInfo = actualUserInfo

                Context ctx = SHiTApplication.getContext();
                Calendar alarmTime = Calendar.getInstance();
                alarmTime.setTime(newInfo.getNotificationDate());

                // Set up information to be passed to AlarmReceiver/SchedulingService
                extras.putString(SchedulingService.KEY_TRIP_CODE, code);
                extras.putString(SchedulingService.KEY_TITLE, getTitle());

                //extras.putAll(actualUserInfo);

                //long actualLeadTime = getStartTime().getTime() - alarmTime.getTimeInMillis();
                //String leadTimeText = ServerDate.formatInterval(actualLeadTime);

                // Set up message based on alertMessage parameter
                // TripElement:extras.putString(SchedulingService.KEY_MESSAGE, ctx.getString(alertMessageId, leadTimeText, startTime(null, DateFormat.SHORT)));
                extras.putString(SchedulingService.KEY_MESSAGE, ctx.getString(alertMessageId, "?? minutes", startTime(DateUtils.FORMAT_SHOW_TIME)));

                AlarmReceiver alarm = new AlarmReceiver();
                alarm.setAlarm(alarmTime.getTime(), Uri.parse("alarm://shitt.no/" + notificationType + "/" + code + "/" + Integer.toString(id)), extras);
            } else {
                Log.d("Trip", "Not setting " + notificationType + " notification for trip " + id + " combined with other notification");
            }

            notifications.put(notificationType, newInfo);
        } else {
            Log.d("Trip", "Not refreshing " + notificationType + " notification for trip  " + id + ", already triggered");
        }
    }

    private void setNotification() {
        // First delete any existing notifications for this trip element (either one or two)
        //cancelNotifications();

        // Set notification(s) (if we have a start date)
        if (getTense() == Tense.FUTURE) {
            //Context ctx = SHiTApplication.getContext();
            //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);

            int leadTimeTripHours = SHiTApplication.getPreferenceInt(Constants.Setting.ALERT_LEAR_TIME_TRIP, LEAD_TIME_MISSING);

            if (leadTimeTripHours != LEAD_TIME_MISSING) {
                setNotification(Constants.Setting.ALERT_LEAD_TIME_CONNECTION, leadTimeTripHours * 60, R.string.alert_msg_travel, null);
            }
        }
    }

    public void onRemoteCallComplete(JSONObject response) {
        //Log.d("Trip", "Trip details retrieved");
        int count = response.optInt(Constants.JSON.QUERY_COUNT, -1);
        if (count == 1) {
            JSONArray results = response.optJSONArray(Constants.JSON.QUERY_RESULTS);
            JSONObject serverData = null;
            Trip newTrip = null;
            if (results != null && results.length() == 1) {
                serverData = results.optJSONObject(0);
            }
            if (serverData != null) {
                newTrip = Trip.createFromDictionary(serverData, true);
            }
            if (newTrip != null) {
                //Log.d("Trip", "Comparing new trip details to existing");
                newTrip.compareTripElements(this);
                //Log.d("Trip", "Updating trip");
                this.id              = newTrip.id;
                this.itineraryId     = newTrip.itineraryId;
                this.startDate       = newTrip.startDate;
                this.endDate         = newTrip.endDate;
                this.tripDescription = newTrip.tripDescription;
                this.code            = newTrip.code;
                this.name            = newTrip.name;
                this.type            = newTrip.type;
                this.elements        = newTrip.elements;
            }
        }

        //Log.d("Trip", "Sending notification");
        Intent intent = new Intent(Constants.Notification.TRIP_DETAILS_LOADED);
        //intent.putExtra("message", "SHiT trips loaded");
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }

    public void onRemoteCallFailed() {
        //Log.d("Trip", "Server call failed");
        Intent intent = new Intent(Constants.Notification.COMMUNICATION_FAILED);
        //intent.putExtra("message", "SHiT trips loaded");
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }

    public void onRemoteCallFailed(Exception e) {
        //Log.d("Trip", "Server call failed");
        Intent intent = new Intent(Constants.Notification.COMMUNICATION_FAILED);
        intent.putExtra("message", e.getMessage());
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }


    void refreshNotifications() {
        setNotification();
        if (elements != null) {
            for (int i = 0; i < elements.size(); i++) {
                elements.get(i).tripElement.setNotification();
            }
        }
    }

    void refreshMessages() {
        chatThread.refresh(ChatThread.RefreshMode.FULL);
    }

    private void registerForPushNotifications() {
        String topicTrip = Constants.PushNotification.TOPIC_ROOT_TRIP + id;
        //Log.d("Trip", "registerForPushNotifications: Register for topic " + topicTrip);
        FirebaseMessaging.getInstance().subscribeToTopic(topicTrip);

        // TODO: Or not?
        if (itineraryId > 0) {
            String topicItinerary = Constants.PushNotification.TOPIC_ROOT_ITINERARY + itineraryId;
            //Log.d("Trip", "registerForPushNotifications: Register for topic " + topicItinerary);
            FirebaseMessaging.getInstance().subscribeToTopic(topicItinerary);
        }
    }

    void deregisterFromPushNotifications() {
        String topicTrip = Constants.PushNotification.TOPIC_ROOT_TRIP + id;
        //Log.d("Trip", "registerForPushNotifications: Deregister from topic " + topicTrip);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicTrip);

        // TODO: Or not?
        if (itineraryId > 0) {
            String topicItinerary = Constants.PushNotification.TOPIC_ROOT_TRIP + id;
            //Log.d("Trip", "registerForPushNotifications: Deregister from topic " + topicItinerary);
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topicItinerary);
        }
    }

    public void loadDetails() {
        //ServerAPI.Params params = new ServerAPI.Params(ServerAPI.URL_TRIP_INFO, "code", code);
        ServerAPI.Params params = new ServerAPI.Params(ServerAPI.URL_BASE, ServerAPI.RESOURCE_TRIP, code, null, null);
        params.addParameter(ServerAPI.PARAM_USER_NAME, User.sharedUser.getUserName());
        params.addParameter(ServerAPI.PARAM_PASSWORD, User.sharedUser.getPassword());
        params.addParameter(ServerAPI.PARAM_LANGUAGE, Locale.getDefault().getLanguage());

        new ServerAPI(this).execute(params);
    }

    void copyState(Trip fromTrip) {
        this.notifications = fromTrip.notifications;
        chatThread = fromTrip.chatThread;

        // Copy state for all elements
        if (this.elements != null && fromTrip.elements != null) {
            for (AnnotatedTripElement newElement: elements) {
                AnnotatedTripElement oldElement = fromTrip.elementById(newElement.tripElement.id);
                if (oldElement != null) {
                    newElement.tripElement.copyState(oldElement.tripElement);
                }
            }
        }
    }
}
