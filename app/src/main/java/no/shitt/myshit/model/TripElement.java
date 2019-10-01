package no.shitt.myshit.model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import no.shitt.myshit.AlarmReceiver;
import no.shitt.myshit.Constants;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.SchedulingService;
import no.shitt.myshit.helper.JSONable;
import no.shitt.myshit.helper.ServerDate;
import no.shitt.myshit.helper.StringUtil;

public abstract class TripElement implements JSONable {
    public enum ActivityType {
        REGULAR, POPUP
    }

    private String type;
    public String subType;
    public int id;
    public List<Map<String,String>> references;

    // Notifications created for this element (used to avoid recreating notifications after they have been triggered)
    private Map<String,NotificationInfo> notifications;

    public String tripCode;
    //private int    tripId;
    private JSONObject serverData;

    public static final String REFTAG_REF_NO          = "refNo";
    public static final String REFTAG_TYPE            = "type";
    public static final String REFTAG_LOOKUP_URL      = "urlLookup";

    public static final String REFTYPE_ELECTRONIC_TKT = "ETKT";

    public class Type {
        public static final String ACCOMMODATION           = "ACM";
        public static final String EVENT                   = "EVT";
        public static final String TRANSPORT               = "TRA";
    }

    public class SubType {
        public static final String AIRLINE                 = "AIR";
        public static final String BOAT                    = "BOAT";
        public static final String BUS                     = "BUS";
        public static final String LIMOUSINE               = "LIMO";
        public static final String PRIVATE_BUS             = "PBUS";
        public static final String TRAIN                   = "TRN";
    }

    private static final String iconBaseName = "icon_tripelement_";

    // Minutes between notifications for same trip element (in milliseconds)
    private static final int MINIMUM_NOTIFICATION_SEPARATION = 10 * 60 * 1000;
    static final int LEAD_TIME_MISSING = -1;

    public Date getStartTime() {
        return null;
    }
    public String getStartTimeZone() {
        return null;
    }
    public Date getEndTime() {
        return null;
    }
    public String getEndTimeZone() {
        return null;
    }
    public String getTitle() {
        return null;
    }
    public String getStartInfo() {
        return null;
    }
    public String getEndInfo() {
        return null;
    }
    public String getDetailInfo() {
        return null;
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
            return null; // Tense.FUTURE;
        }
    }
    public String getReferences(String separator, boolean appendBlank, Set<String> excludeTypes) {
        StringBuilder refList = new StringBuilder();
        Iterator i = references.iterator();
        String sep = "";
        for (Map<String,String> refItem : references) {
            String type = refItem.get(REFTAG_TYPE);
            if (excludeTypes == null || ! excludeTypes.contains(type)) {
                String ref = refItem.get(REFTAG_REF_NO);
                StringUtil.appendWithLeadingSeparator(refList, ref, sep, appendBlank);
                sep = separator;
            }
        }
        return refList.toString();
    }
    public String getReferences(String separator, boolean appendBlank) {
        return getReferences(separator, appendBlank, null);
    }

    public int getIconId() {
        Context ctx = SHiTApplication.getContext();
        String iconType = "default";
        String iconName;
        int    iconId;

        switch (getTense()) {
            case PAST:
                iconType = "historic";
                break;
            case PRESENT:
                iconType = "active";
                break;
            default:
                break;
        }
        iconName = iconBaseName + type + "_" + subType + "_" + iconType;

        // First try exact match
        iconId = ctx.getResources().getIdentifier(iconName.toLowerCase(), "mipmap", ctx.getPackageName());
        if (iconId != 0) {
            return iconId;
        }

        // Try ignoring subtype
        iconName = iconBaseName + type + "_" + iconType;
        iconId = ctx.getResources().getIdentifier(iconName.toLowerCase(), "mipmap", ctx.getPackageName());
        if (iconId != 0) {
            return iconId;
        }

        // Try dummy image
        iconName = iconBaseName + iconType;
        iconId = ctx.getResources().getIdentifier(iconName.toLowerCase(), "mipmap", ctx.getPackageName());
        if (iconId != 0) {
            return iconId;
        }

        return 0;
    }

    // MARK: Factory
    static TripElement createFromDictionary( int tripId, String tripCode, JSONObject elementData ) {
        String elemType = elementData.optString(Constants.JSON.ELEM_TYPE);
        String elemSubType = elementData.optString(Constants.JSON.ELEM_SUB_TYPE);

        TripElement elem;
        if (elemType.equals(Type.TRANSPORT) && elemSubType.equals(SubType.AIRLINE)) {
            elem = new Flight(tripId, tripCode, elementData);
        } else if (elemType.equals(Type.TRANSPORT) && elemSubType.equals(SubType.PRIVATE_BUS)) {
            elem = new PrivateTransport(tripId, tripCode, elementData);
        } else if (elemType.equals(Type.TRANSPORT) && elemSubType.equals(SubType.LIMOUSINE)) {
            elem = new PrivateTransport(tripId, tripCode, elementData);
        } else if (elemType.equals(Type.TRANSPORT) && elemSubType.equals(SubType.BUS)) {
            elem = new ScheduledTransport(tripId, tripCode, elementData);
        } else if (elemType.equals(Type.TRANSPORT) && elemSubType.equals(SubType.TRAIN)) {
            elem = new ScheduledTransport(tripId, tripCode, elementData);
        } else if (elemType.equals(Type.TRANSPORT) && elemSubType.equals(SubType.BOAT)) {
            elem = new ScheduledTransport(tripId, tripCode, elementData);
        } else if (elemType.equals(Type.TRANSPORT)) {
            elem = new GenericTransport(tripId, tripCode, elementData);
        } else if (elemType.equals(Type.ACCOMMODATION)) {
            elem = new Hotel(tripId, tripCode, elementData);
        } else if (elemType.equals(Type.EVENT)) {
            elem = new Event(tripId, tripCode, elementData);
        } else {
            elem = null;
        }

        return elem;
    }

    // Encode to JSON for saving to file
    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.putOpt(Constants.JSON.ELEM_TYPE, type);
        jo.putOpt(Constants.JSON.ELEM_SUB_TYPE, subType);
        jo.putOpt(Constants.JSON.ELEM_ID, id);

        JSONArray jar = new JSONArray();
        for (Map ref : references) {
            jar.put(new JSONObject(ref));
        }
        jo.putOpt(Constants.JSON.ELEM_REFERENCES, jar);

        JSONObject jsonNtf = new JSONObject();
        for (Map.Entry<String, NotificationInfo> ntfInfo : notifications.entrySet()) {
            String key = ntfInfo.getKey();
            NotificationInfo value = ntfInfo.getValue();
            jsonNtf.put(key, value.toJSON());
        }
        jo.put("notifications", jsonNtf);

        return jo;
    }

    TripElement(int id, String type, String subType, List<Map<String,String>> references) {
        // Initialize stored properties.
        //self.visible = visible
        super();
        if (id <= 0 || type == null || subType == null) {
            throw new IllegalArgumentException("id, type or subtype is invalid");
        }

        this.id = id;
        this.type = type;
        this.subType = subType;
        this.references = references;
        this.notifications = new HashMap<>();
    }

    TripElement(int tripId, String tripCode, JSONObject elementData) {
        id = elementData.optInt("id", -1);
        type = elementData.isNull("type") ? null : elementData.optString("type");
        subType = elementData.isNull("subType") ? null : elementData.optString("subType");

        //this.tripId = tripId;
        this.tripCode = tripCode;

        JSONArray serverRefs = elementData.optJSONArray("references");
        if (serverRefs == null) {
            references = null;
        } else {
            references = new ArrayList<>();
            for (int i = 0; i < serverRefs.length(); i++) {
                JSONObject ref = serverRefs.optJSONObject(i);
                Iterator keys = ref.keys();
                Map<String,String> refData = new HashMap<>();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    String value = ref.optString(key);
                    refData.put(key, value);
                }
                references.add(refData);
            }
        }

        notifications = new HashMap<>();
        JSONObject savedNotifications = elementData.optJSONObject("notifications");
        if (savedNotifications != null) {
            Iterator i = savedNotifications.keys();
            while (i.hasNext()) {
                String key = (String) i.next();
                JSONObject savedNtfInfo = savedNotifications.optJSONObject(key);
                if (savedNtfInfo != null) {
                    NotificationInfo ntfInfo = new NotificationInfo(savedNtfInfo);
                    notifications.put(key, ntfInfo);
                } else {
                    Log.e("TripElement", "Unable to rebuild notification info.");
                }
            }
        }

        serverData = elementData;
    }


    // MARK: Methods
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isEqual(Object otherObject) {
        if (this.getClass() != otherObject.getClass()) {
            //Log.d("GenericTransport", "Changed class! " + this.getClass().getCanonicalName() + " != " + otherObject.getClass().getCanonicalName());
            return false;
        }
        try {
            TripElement otherTripElement = (TripElement) otherObject;

            if (this.id != otherTripElement.id)                             { return false; }
            //if (!StringUtil.equal(this.type, otherTripElement.type)      )  { return false; }
            //if (!StringUtil.equal(this.subType, otherTripElement.subType))  { return false; }
            if (!Objects.equals(this.type, otherTripElement.type)      )  { return false; }
            if (!Objects.equals(this.subType, otherTripElement.subType))  { return false; }

            if (this.references != null && otherTripElement.references != null) {
                if (this.references.size() != otherTripElement.references.size()) {
                    return false;
                }

                return this.references.containsAll(otherTripElement.references) && otherTripElement.references.containsAll(this.references);
            } else if (this.references != null || otherTripElement.references != null) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public String startTime(Integer dateStyle, Integer timeStyle) {
        return null;
    }

    public String endTime(Integer dateStyle, Integer timeStyle) {
        return null;
    }

    public void setNotification() {
        // Generic trip element can't have notifications (start date/time not known)
        // Subclasses that support notifications must override this method
    }

    void setNotification(String notificationType, int leadTime, int alertMessageId, String clickAction, Bundle userInfo) {
        // Logic starts here
        NotificationInfo oldInfo = notifications == null ? null : notifications.get(notificationType);  //TODO: Check what happens if key doesn't exist
        NotificationInfo newInfo = new NotificationInfo(getStartTime(), leadTime);

        if (oldInfo == null || oldInfo.needsRefresh(newInfo)) {
            boolean combined = false;

            //Log.d("TripElement", "Setting " + notificationType + " notification for trip element " + id + " at " + newInfo.getNotificationDate());

            Bundle extras = new Bundle();
            //Map<String,Object> actualUserInfo = new HashMap<>();
            if (userInfo != null) {
                extras.putAll(userInfo);
            }
            extras.putString(Constants.NotificationUserInfo.LEAD_TIME_TYPE, notificationType);
            extras.putInt(Constants.NotificationUserInfo.TRIP_ELEMENT_ID, id);
            if (getStartTimeZone() != null) {
                extras.putString(Constants.NotificationUserInfo.TIMEZONE, getStartTimeZone());
            }

            for (String nType : notifications.keySet()) {
                if (!nType.equals(notificationType)) {
                    NotificationInfo n = notifications.get(nType);
                    if (n.getNotificationDate().before(newInfo.getNotificationDate())
                            && (newInfo.getNotificationDate().getTime() - n.getNotificationDate().getTime()) < TripElement.MINIMUM_NOTIFICATION_SEPARATION) {
                        newInfo.combine(n);
                        combined = true;
                    }
                }
            }

            if (!combined) {
                Context ctx = SHiTApplication.getContext();
                Calendar alarmTime = Calendar.getInstance();
                alarmTime.setTime(newInfo.getNotificationDate());

                // Set up information to be passed to AlarmReceiver/SchedulingService
                extras.putString(SchedulingService.KEY_TRIP_CODE, tripCode);
                extras.putInt(SchedulingService.KEY_ELEMENT_ID, id);
                extras.putString(SchedulingService.KEY_TITLE, getTitle());

                long actualLeadTime = getStartTime().getTime() - alarmTime.getTimeInMillis();
                String leadTimeText = ServerDate.formatInterval(actualLeadTime);

                // Set up message based on alertMessage parameter
                extras.putString(SchedulingService.KEY_MESSAGE, ctx.getString(alertMessageId, leadTimeText, startTime(null, DateFormat.SHORT)));

                AlarmReceiver alarm = new AlarmReceiver();
                alarm.setAlarm( alarmTime.getTime()
                        , Uri.parse("alarm://shitt.no/" + notificationType + "/" + tripCode + "/" + id)
                        , clickAction
                        , extras);
            } else {
                Log.d("TripElement", "Not setting " + notificationType + " notification for trip element " + id + " combined with other notification");
            }

            notifications.put(notificationType, newInfo);
        } else {
            Log.d("TripElement", "Not refreshing " + notificationType + " notification for trip element " + id + ", already triggered");
        }

    }

    void setNotification(String notificationType, int leadTime, int alertMessageId, Bundle userInfo) {
        setNotification(notificationType, leadTime, alertMessageId, null, userInfo);
    }

    void copyState(TripElement fromElement) {
        this.notifications = fromElement.notifications;
    }

    abstract public Intent getActivityIntent(ActivityType activityType);

    abstract protected String getNotificationClickAction();
}
