package no.shitt.myshit.model;

//import android.text.format.Formatter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
import no.shitt.myshit.helper.ServerAPIListener;
import no.shitt.myshit.helper.ServerAPIParams;
import no.shitt.myshit.helper.ServerDate;
import no.shitt.myshit.helper.StringUtil;

public class Trip implements ServerAPIListener, JSONable {
    public int id;
    public String startDateText;  // Hold original value for saving in archive
    public Date startDate;
    public String endDateText;    // Hold original value for saving in archive
    public Date endDate;
    public String tripDescription;
    public String code;
    public String name;
    public String type;
    public List<AnnotatedTripElement> elements;

    private final static String iconBaseName = "icon_trip_";


    public Date getStartTime() {
        return startDate;
    }

    public String getStartTimeZone() {
        if (elements != null && elements.size() > 0) {
            return elements.get(0).tripElement.getStartTimeZone();
        }
        return null;
    }

    public Date getEndTime() {
        return endDate;
    }

    public String getTitle() {
        return name;
    }

    public String getDateInfo() {
        Context ctx = SHiTApplication.getContext();
        //DateFormat dateFormatter = android.text.format.DateFormat.getTimeFormat(ctx);
        //dateFormatter.dateStyle = NSDateFormatterStyle.MediumStyle
        //dateFormatter.timeStyle = NSDateFormatterStyle.NoStyle

        return DateUtils.formatDateRange(ctx, startDate.getTime(), endDate.getTime(), DateUtils.FORMAT_SHOW_DATE + DateUtils.FORMAT_ABBREV_MONTH);
        //return dateFormatter.format(startDate) + " - " + dateFormatter.format(endDate);
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
    public static Trip createFromDictionary( JSONObject elementData, boolean fromServer ) {
        //let tripType = elementData["type"] as? String ?? ""

        return new Trip(elementData, fromServer);
    }

    // Encode to JSON for saving to file
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.put(Constants.JSON.TRIP_ID, id);
        jo.putOpt(Constants.JSON.TRIP_START_DATE, startDateText);
        jo.putOpt(Constants.JSON.TRIP_END_DATE, endDateText);
        jo.putOpt(Constants.JSON.TRIP_DESCRIPTION, tripDescription);
        jo.putOpt(Constants.JSON.TRIP_CODE, code);
        jo.putOpt(Constants.JSON.TRIP_NAME, name);
        jo.putOpt(Constants.JSON.TRIP_TYPE, type);

        JSONArray jate = new JSONArray();
        if (elements != null) {
            Iterator i = elements.iterator();
            while (i.hasNext()) {
                AnnotatedTripElement ate = (AnnotatedTripElement) i.next();
                jate.put(ate.toJSON());
            }
        }
        jo.put(Constants.JSON.TRIP_ELEMENTS, jate);

        return jo;
    }

    Trip(JSONObject elementData, boolean fromServer) {
        super();
        id = elementData.optInt(Constants.JSON.TRIP_ID, -1);
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

        setNotification();
    }

    // MARK: Methods
    public boolean isEqual(Object otherObject) {
        //print("Comparing objects: self.class = \(object_getClassName(self)), object.class = \(object_getClassName(object!))")
        //print("Comparing objects: self.class = \(_stdlib_getDemangledTypeName(self)), object.class = \(_stdlib_getDemangledTypeName(object!))")
        if (this.getClass() != otherObject.getClass()) {
            return false;
        }
        try {
            Trip otherTrip = (Trip) otherObject;
            if (this.id              != otherTrip.id                              ) { return false; }
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

    public void compareTripElements(Trip otherTrip) {
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


    public void setNotification() {
        Context ctx = SHiTApplication.getContext();
        // TO DO...
        if (getStartTime() == null) {
            //Log.d("Trip", "Not setting notification for trip " + code + " without start time");
            return;
        } else if (getTense() != Tense.FUTURE) {
            //Log.d("Trip", "Not setting notification for historic (or started) trip " + code);
            return;
        }
        //Log.d("Trip", "Setting notification for trip " + code);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        int leadTimeTripHours;
        try {
            String leadTimeTrip = sharedPref.getString("pref_alertLeadTime_trip" /*SettingsActivity.KEY_PREF_SYNC_CONN*/, "");
            leadTimeTripHours = Integer.valueOf(leadTimeTrip);
        }
        catch (Exception e) {
            leadTimeTripHours = -1;
        }

        // For testing...
        //alarmcounter++;
        //Date alarmTime = new Date();
        //calendar.setTimeInMillis(System.currentTimeMillis());
        //alarmTime.add(Calendar.SECOND, (alarmcounter % 10) * 20);

        Bundle extras = new Bundle();
        extras.putString(SchedulingService.KEY_MESSAGE, ctx.getString(R.string.alert_msg_trip, "?? minutes", startTime(DateUtils.FORMAT_SHOW_TIME)));
        extras.putString(SchedulingService.KEY_TRIP_CODE, code);
        extras.putString(SchedulingService.KEY_TITLE, getTitle());

        //AlarmReceiver alarm = new AlarmReceiver();
        //alarm.setAlarm(calendar.getTime(), Uri.parse("alarm://test.shitt.no/trip/" + code), extras);

        Calendar now = Calendar.getInstance();

        if (leadTimeTripHours > 0) {
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTime(getStartTime());
            alarmTime.add(Calendar.HOUR, -leadTimeTripHours);

            // If we're already past the warning time, set a notification for right now instead
            if (alarmTime.before(now)) {
                alarmTime = now;
            }

            long actualLeadTime = getStartTime().getTime() - alarmTime.getTimeInMillis(); // alarmTime.compareTo(now);
            String leadTimeText = ServerDate.formatInterval(actualLeadTime);
            extras.putString(SchedulingService.KEY_MESSAGE, ctx.getString(R.string.alert_msg_travel, leadTimeText, startTime(DateUtils.FORMAT_SHOW_TIME)));

            AlarmReceiver tripAlarm = new AlarmReceiver();
            tripAlarm.setAlarm(alarmTime.getTime(), Uri.parse("alarm://shitt.no/trip/" + code), extras);
        }
    }

    public void onRemoteCallComplete(JSONObject response) {
        //Log.d("Trip", "Trip details retrieved");
        int count = response.optInt(Constants.JSON.QUERY_COUNT, -1);
        if (count != 1) {
            //Log.e("Trip", "loadDetails returned " + Integer.toString(count) + " elements, expected 1.");
        } else {
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


    public void refreshNotifications() {
        setNotification();
        for (int i = 0; i < elements.size(); i++) {
            elements.get(i).tripElement.setNotification();
        }
    }

    public void loadDetails() {
        ServerAPIParams params = new ServerAPIParams(ServerAPI.URL_TRIP_INFO, "code", code);
        params.addParameter(ServerAPI.PARAM_USER_NAME, User.sharedUser.getUserName());
        params.addParameter(ServerAPI.PARAM_PASSWORD, User.sharedUser.getPassword());

        new ServerAPI(this).execute(params);
    }
}
