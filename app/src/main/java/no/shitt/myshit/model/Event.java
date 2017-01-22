package no.shitt.myshit.model;

//import android.content.Context;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import no.shitt.myshit.AlarmReceiver;
import no.shitt.myshit.Constants;
//import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.R;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.SchedulingService;
import no.shitt.myshit.helper.ServerDate;
import no.shitt.myshit.helper.StringUtil;

/******************************************************************************
 * Created by Per Solberg on 2017-01-19.
 *****************************************************************************/

public class Event extends TripElement {
    private static final int TRAVEL_TIME_UNKNOWN = -1;


    // MARK: Properties
    public String venueName;
    public String venueAddress;
    public String venuePostCode;
    public String venueCity;
    public String venuePhone;
    private String startTimeText;  // Hold original value for saving in archive
    private Date startTime;
    private int    travelTime;
    public String accessInfo;
    //public String reference;
    private String timezone;


    @Override
    public Date getStartTime() {
        return startTime;
    }
    @Override
    public String getTitle() {
        return venueName;
    }
    @Override
    public String getStartInfo() {
        //Context ctx = SHiTApplication.getContext();
        //DateFormat dateFormatter = android.text.format.DateFormat.getTimeFormat(ctx);

        return startTime(null, DateFormat.SHORT);
    }
    @Override
    public String getEndInfo() {
        //DateFormat dateFormatter = android.text.format.DateFormat.getTimeFormat();
        return null;
        //return dateFormatter.stringFromDate(checkOutTime!)
    }
    @Override
    public String getDetailInfo() {
        if (references != null) {
            String refList = "";
            for (int i = 0; i < references.size(); i++) {
                refList = refList + (refList.equals("") ? "" : ", ") + references.get(i).get("refNo");
            }
            return refList;
        }
        return null;
    }

    // Encode to JSON for saving to file
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jo = super.toJSON();

        jo.putOpt(Constants.JSON.ELEM_EVENT_VENUE_NAME, venueName);
        jo.putOpt(Constants.JSON.ELEM_EVENT_VENUE_ADDR, venueAddress);
        jo.putOpt(Constants.JSON.ELEM_EVENT_VENUE_POST_CODE, venuePostCode);
        jo.putOpt(Constants.JSON.ELEM_EVENT_VENUE_CITY, venueCity);
        jo.putOpt(Constants.JSON.ELEM_EVENT_VENUE_PHONE, venuePhone);
        jo.putOpt(Constants.JSON.ELEM_EVENT_START_TIME, startTimeText);
        jo.putOpt(Constants.JSON.ELEM_EVENT_TIMEZONE, timezone);
        if (travelTime != TRAVEL_TIME_UNKNOWN) {
            jo.putOpt(Constants.JSON.ELEM_EVENT_TRAVEL_TIME, travelTime);
        }
        jo.putOpt(Constants.JSON.ELEM_EVENT_ACCESS_INFO, accessInfo);

        return jo;
    }

    Event(int tripId, String tripCode, JSONObject elementData) {
        super(tripId, tripCode, elementData);

        timezone = elementData.isNull(Constants.JSON.ELEM_EVENT_TIMEZONE) ? null : elementData.optString(Constants.JSON.ELEM_EVENT_TIMEZONE);

        startTimeText = elementData.isNull(Constants.JSON.ELEM_EVENT_START_TIME) ? null : elementData.optString(Constants.JSON.ELEM_EVENT_START_TIME);
        if (startTimeText != null) {
            startTime = ServerDate.convertServerDate(startTimeText, timezone);
        }

        venueName = elementData.isNull(Constants.JSON.ELEM_EVENT_VENUE_NAME) ? null : elementData.optString(Constants.JSON.ELEM_EVENT_VENUE_NAME);
        venueAddress = elementData.isNull(Constants.JSON.ELEM_EVENT_VENUE_ADDR) ? null : elementData.optString(Constants.JSON.ELEM_EVENT_VENUE_ADDR);
        venuePostCode = elementData.isNull(Constants.JSON.ELEM_EVENT_VENUE_POST_CODE) ? null : elementData.optString(Constants.JSON.ELEM_EVENT_VENUE_POST_CODE);
        venueCity = elementData.isNull(Constants.JSON.ELEM_EVENT_VENUE_CITY) ? null : elementData.optString(Constants.JSON.ELEM_EVENT_VENUE_CITY);
        venuePhone = elementData.isNull(Constants.JSON.ELEM_EVENT_VENUE_PHONE) ? null : elementData.optString(Constants.JSON.ELEM_EVENT_VENUE_PHONE);
        accessInfo = elementData.isNull(Constants.JSON.ELEM_EVENT_ACCESS_INFO) ? null : elementData.optString(Constants.JSON.ELEM_EVENT_ACCESS_INFO);
        travelTime = elementData.isNull(Constants.JSON.ELEM_EVENT_TRAVEL_TIME) ? TRAVEL_TIME_UNKNOWN : elementData.optInt(Constants.JSON.ELEM_EVENT_TRAVEL_TIME);

        //Log.d("Event", "Start time text = " + startTimeText
        //             + ", startTime = " + startTime.toString()
        //             + ", time zone = " + timezone);
    }

    // MARK: Methods
    @Override
    public boolean isEqual(Object otherObject) {
        if (this.getClass() != otherObject.getClass()) {
            return false;
        }
        try {
            Event otherEvent = (Event) otherObject;
            if (!ServerDate.equal(this.startTime, otherEvent.startTime))          { return false; }
            if (this.travelTime != otherEvent.travelTime)                         { return false; }
            if (!StringUtil.equal(this.venueName, otherEvent.venueName))          { return false; }
            if (!StringUtil.equal(this.venueAddress, otherEvent.venueAddress))    { return false; }
            if (!StringUtil.equal(this.venuePostCode, otherEvent.venuePostCode))  { return false; }
            if (!StringUtil.equal(this.venueCity, otherEvent.venueCity))          { return false; }
            if (!StringUtil.equal(this.venuePhone, otherEvent.venuePhone))        { return false; }
            if (!StringUtil.equal(this.accessInfo, otherEvent.accessInfo))        { return false; }
            if (!StringUtil.equal(this.timezone, otherEvent.timezone))            { return false; }

            return super.isEqual(otherObject);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String startTime(Integer dateStyle, Integer timeStyle) {
        if (startTime != null) {
            DateFormat dateFormatter;
            if (dateStyle == null) {
                dateFormatter = SimpleDateFormat.getTimeInstance(timeStyle);
            } else if (timeStyle == null) {
                dateFormatter = SimpleDateFormat.getDateInstance(dateStyle);
            } else {
                dateFormatter = SimpleDateFormat.getDateTimeInstance(dateStyle, timeStyle);
            }
            //Log.d("Event", "startTime " + startTime.toString() + ", time zone = " + timezone);
            if (timezone != null) {
                //Log.d("Event", "startTime setting time zone");
                TimeZone tz = TimeZone.getTimeZone(timezone);
                dateFormatter.setTimeZone(tz);
            }

            return dateFormatter.format(startTime);
        }
        return null;
    }

    @Override
    public String endTime(Integer dateStyle, Integer timeStyle) {
        return null;
    }

    public String travelTime() {
        return travelTime == TRAVEL_TIME_UNKNOWN ? "" : ServerDate.formatInterval(travelTime * DateUtils.MINUTE_IN_MILLIS);
    }


    @Override
    public void setNotification() {
        if (getStartTime() == null) {
            return;
        } else if (getTense() != Tense.FUTURE) {
            return;
        }

        //String code = tripCode; //trip.trip.code;
        //Log.d("GenericTransport", "Setting notification for trip element " + tripCode + ":" + Integer.toString(id));

        Context ctx = SHiTApplication.getContext();

        // Get lead time from preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);

        int leadTimeEventMinutes;
        try {
            String leadTimeDeparture = sharedPref.getString("pref_alertLeadTime_event", "");
            leadTimeEventMinutes = Integer.valueOf(leadTimeDeparture);
        }
        catch (Exception e) {
            leadTimeEventMinutes = -1;
        }

        // Set up information to be passed to AlarmReceiver/SchedulingService
        Bundle extras = new Bundle();
        extras.putString(SchedulingService.KEY_TRIP_CODE, tripCode);
        extras.putInt(SchedulingService.KEY_ELEMENT_ID, id);
        extras.putString(SchedulingService.KEY_TITLE, getTitle());

        //AlarmReceiver alarm = new AlarmReceiver();
        Calendar now = Calendar.getInstance();
        //alarm.setAlarm(alarmTime.getTime(), Uri.parse("alarm://test.shitt.no/element/" + tripCode + "/" + Integer.toString(id)), extras);

        if (leadTimeEventMinutes > 0) {
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTime(getStartTime());
            alarmTime.add(Calendar.MINUTE, -leadTimeEventMinutes);

            if (travelTime != TRAVEL_TIME_UNKNOWN) {
                alarmTime.add(Calendar.MINUTE, -travelTime);
            }

            // If we're already past the warning time, set a notification for right now instead
            if (alarmTime.before(now)) {
                alarmTime = now;
            }

            //int actualLeadTime = alarmTime.compareTo(now);
            long actualLeadTime = getStartTime().getTime() - alarmTime.getTimeInMillis();
            String leadTimeText = ServerDate.formatInterval(actualLeadTime);
            extras.putString(SchedulingService.KEY_MESSAGE, ctx.getString(R.string.alert_msg_event, leadTimeText, startTime(null, DateFormat.SHORT)));

            AlarmReceiver departureAlarm = new AlarmReceiver();
            departureAlarm.setAlarm(alarmTime.getTime(), Uri.parse("alarm://shitt.no/event/" + tripCode + "/" + Integer.toString(id)), extras);
        }
    }
}
