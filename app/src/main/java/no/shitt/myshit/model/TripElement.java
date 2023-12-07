package no.shitt.myshit.model;

import static java.util.Map.entry;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import no.shitt.myshit.AlarmReceiver;
import no.shitt.myshit.Constants;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.IconCache;
import no.shitt.myshit.helper.JSONable;
import no.shitt.myshit.helper.ServerDate;
import no.shitt.myshit.helper.StringUtil;

public abstract class TripElement implements JSONable {
    private static final String LOG_TAG = TripElement.class.getSimpleName();

    public enum ActivityType {
        REGULAR, POPUP
    }

    private String type;
    public String subType;
    public final int id;
    public Set<Map<String,String>> references;

    // Notifications created for this element (used to avoid recreating notifications after they have been triggered)
    private final Map<String,NotificationInfo> notifications;

    public final String tripCode;
    private JSONObject serverData;
    protected boolean changed;

    public static final String REFTAG_REF_NO          = "refNo";
    public static final String REFTAG_TYPE            = "type";
    public static final String REFTAG_LOOKUP_URL      = "urlLookup";

    public static final String REFTYPE_ELECTRONIC_TKT = "ETKT";

    private class Type {
        private static final String ACCOMMODATION           = "ACM";
        private static final String EVENT                   = "EVT";
        private static final String TRANSPORT               = "TRA";
    }

    private class SubType {
        private static final String AIRLINE                 = "AIR";
        private static final String BOAT                    = "BOAT";
        private static final String BUS                     = "BUS";
        private static final String LIMOUSINE               = "LIMO";
        private static final String PRIVATE_BUS             = "PBUS";
        private static final String TRAIN                   = "TRN";
    }

    private static final String iconBaseName = "icon_tripelement";

    // Minutes between notifications for same trip element (in milliseconds)
    private static final int MINIMUM_NOTIFICATION_SEPARATION = 10 * 60 * 1000;
    static final int LEAD_TIME_MISSING = -1;
    private static final Map<Tense,String> tenseNames = Map.ofEntries(entry(Tense.PAST, "historic")
            ,entry(Tense.PRESENT, "active")
    );
    private static final String defaultTenseName = "default";

    public class TripElementIconKey extends IconCache.IconKey {
        final String type;
        final String subType;
        final Tense  tense;

        TripElementIconKey(String type, String subType, Tense tense) {
            this.type = type;
            this.subType = subType;
            this.tense = tense;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null)
                return false;

            if (getClass() == o.getClass()) {
                TripElementIconKey otherKey = (TripElementIconKey) o;

                boolean sameType = (this.type == null && otherKey.type == null)
                        || (this.type != null && otherKey.type != null && this.type.equals(otherKey.type));
                boolean sameSubType = (this.subType == null && otherKey.subType == null)
                        || (this.subType != null && otherKey.subType != null && this.subType.equals(otherKey.subType));
                boolean sameTense = (this.tense == null && otherKey.tense == null)
                        || (this.tense != null && otherKey.tense != null && this.tense.equals(otherKey.tense));
                return sameType && sameSubType && sameTense;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + (type == null ? 0 : type.hashCode());
            hash = 31 * hash + (subType == null ? 0 : subType.hashCode());
            hash = 31 * hash + (tense == null ? 0 : tense.hashCode());
            return hash;
        }


        @Override
        public IconCache.IconKey getParent() {
            if (subType != null) {
                return new TripElementIconKey(type, null, tense);
            } else if ( type != null ) {
                return new TripElementIconKey(null, null, tense);
            } else {
                return null;
            }
        }

        public String downloadPath() {
            StringBuilder path = new StringBuilder("https://shitt.no/mySHiT/v2/icons/tripelement/");
            StringUtil.appendWithTrailingSeparator(path, type, ".", false);
            StringUtil.appendWithTrailingSeparator(path, subType, ".", false);
            path.append(getTenseNameWithDefault()).append(".xxxhdpi.png");
            return path.toString();
        }


        private String getTenseName() {
            return tenseNames.get(tense);
        }

        private String getTenseNameWithDefault() {
            return Objects.requireNonNullElse(getTenseName(), defaultTenseName);
        }

        public String name() {
            StringBuilder iconName = new StringBuilder(iconBaseName);
            if (type != null) {
                iconName.append("_").append(type);
            }
            if (subType != null) {
                iconName.append("_").append(subType);
            }
            iconName.append("_").append(getTenseNameWithDefault());
            return iconName.toString();
        }
    }

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
            return null;
        }
    }
    public String getReferences(String separator, boolean appendBlank, Set<String> excludeTypes) {
        StringBuilder refList = new StringBuilder();
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


    public Icon getIcon() {
        TripElementIconKey ik = new TripElementIconKey(type, subType, getTense());
        return IconCache.getSharedCache().get(ik, (icon) -> {
            Log.d(LOG_TAG, "getIcon callback");
            Intent intent = new Intent(Constants.Notification.TRIP_DETAILS_LOADED);
            LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
        });
    }

    //
    // MARK: Factory
    //
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
        for (Map<String,String> ref : references) {
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


    TripElement(int ignoredTripId, String tripCode, JSONObject elementData) {
        id = elementData.optInt(Constants.JSON.ELEM_ID, -1);
        type = elementData.isNull(Constants.JSON.ELEM_TYPE) ? null : elementData.optString(Constants.JSON.ELEM_TYPE);
        subType = elementData.isNull(Constants.JSON.ELEM_SUB_TYPE) ? null : elementData.optString(Constants.JSON.ELEM_SUB_TYPE);

        //this.tripId = tripId;
        this.tripCode = tripCode;

        JSONArray serverRefs = elementData.optJSONArray(Constants.JSON.ELEM_REFERENCES);
        if (serverRefs == null) {
            references = null;
        } else {
            references = mapReferences(serverRefs);
        }

        notifications = new HashMap<>();
        JSONObject savedNotifications = elementData.optJSONObject("notifications");
        if (savedNotifications != null) {
            Iterator<String> i = savedNotifications.keys();
            while (i.hasNext()) {
                String key = /*(String)*/ i.next();
                JSONObject savedNtfInfo = savedNotifications.optJSONObject(key);
                if (savedNtfInfo != null) {
                    NotificationInfo ntfInfo = new NotificationInfo(savedNtfInfo);
                    notifications.put(key, ntfInfo);
                } else {
                    Log.e(LOG_TAG, "Unable to rebuild notification info.");
                }
            }
        }

        serverData = elementData;
    }


    //
    // MARK: Field update functions
    //
    int updateField(int oldValue, int newValue) {
        changed = changed || (oldValue != newValue);
        return newValue;
    }


    String updateField(String oldValue, JSONObject json, String elementName) {
        String newValue = json.isNull(elementName) ? null : json.optString(elementName);
        changed = changed || !Objects.equals(oldValue, newValue);
        return newValue;
    }


    //
    // MARK: Methods
    //
    private Set<Map<String,String>> mapReferences(JSONArray json) {
        if (json == null) {
            return null;
        }
        Set<Map<String,String>> refs = new HashSet<>();
        for (int i = 0; i < json.length(); i++) {
            JSONObject ref = json.optJSONObject(i);
            Iterator<String> keys = ref.keys();
            Map<String,String> refData = new HashMap<>();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = ref.optString(key);
                refData.put(key, value);
            }
            refs.add(refData);
        }
        return refs;
    }


    boolean update(JSONObject elementData) {
        if (id != elementData.optInt(Constants.JSON.ELEM_ID, -1)) {
            throw new AssertionError("Update error: inconsistent trip element IDs");
        }

        changed = false;

        type = updateField(type, elementData, Constants.JSON.ELEM_TYPE);
        subType = updateField(subType, elementData, Constants.JSON.ELEM_SUB_TYPE);

        JSONArray serverRefs = elementData.optJSONArray(Constants.JSON.ELEM_REFERENCES);
        if (serverRefs == null) {
            changed = changed || (references != null);
            references = null;
        } else {
            Set<Map<String,String>> newReferences = mapReferences(serverRefs);
            changed = changed || (!references.equals(newReferences));
            references = newReferences;
        }

        serverData = elementData;

        return changed;
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
        Log.d(LOG_TAG, "Setting notification for " + notificationType);
        NotificationInfo oldInfo = notifications == null ? null : notifications.get(notificationType);
        NotificationInfo newInfo = new NotificationInfo(getStartTime(), leadTime);

        if (oldInfo == null || oldInfo.needsRefresh(newInfo)) {
            boolean combined = false;

            Bundle extras = new Bundle();
            if (userInfo != null) {
                extras.putAll(userInfo);
            }
            extras.putString(Constants.NotificationUserInfo.LEAD_TIME_TYPE, notificationType);
            extras.putInt(Constants.NotificationUserInfo.TRIP_ELEMENT_ID, id);
            if (getStartTimeZone() != null) {
                extras.putString(Constants.NotificationUserInfo.TIMEZONE, getStartTimeZone());
            }

            for (Map.Entry<String,NotificationInfo> ntf : Objects.requireNonNull(notifications).entrySet()) {
                if (!ntf.getKey().equals(notificationType)) {
                    NotificationInfo n = ntf.getValue();
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

                // Set up information to be passed to AlarmReceiver
                extras.putString(Constants.IntentExtra.TRIP_CODE, tripCode);
                extras.putInt(Constants.IntentExtra.ELEMENT_ID, id);
                extras.putString(Constants.IntentExtra.TITLE, getTitle());

                long actualLeadTime = getStartTime().getTime() - alarmTime.getTimeInMillis();
                String leadTimeText = ServerDate.formatInterval(actualLeadTime);

                // Set up message based on alertMessage parameter
                extras.putString(Constants.IntentExtra.MESSAGE, ctx.getString(alertMessageId, leadTimeText, startTime(null, DateFormat.SHORT)));

                AlarmReceiver alarm = new AlarmReceiver();
                alarm.setAlarm( alarmTime.getTime()
                        , Uri.parse("alarm://shitt.no/" + notificationType + "/" + tripCode + "/" + id)
                        , clickAction
                        , extras);
            } else {
                Log.d(LOG_TAG, "Not setting " + notificationType + " notification for trip element " + id + " combined with other notification");
            }

            notifications.put(notificationType, newInfo);
        } else {
            Log.d(LOG_TAG, "Not refreshing " + notificationType + " notification for trip element " + id + ", already triggered");
        }

    }

    @SuppressWarnings("unused")
    void setNotification(String notificationType, int leadTime, int alertMessageId, Bundle userInfo) {
        setNotification(notificationType, leadTime, alertMessageId, null, userInfo);
    }

    abstract public Intent getActivityIntent(ActivityType activityType);

    abstract protected String getNotificationClickAction();
}
