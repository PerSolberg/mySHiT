package no.shitt.myshit.model;

import android.content.Intent;
import android.text.format.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import no.shitt.myshit.Constants;
import no.shitt.myshit.EventActivity;
import no.shitt.myshit.EventPopupActivity;
import no.shitt.myshit.R;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.ServerDate;

/******************************************************************************
 * Created by Per Solberg on 2017-01-19.
 *****************************************************************************/

public class Event extends TripElement {
    private static final int TRAVEL_TIME_UNKNOWN = -1;


    // MARK: Properties
    public final String venueName;
    public final String venueAddress;
    public final String venuePostCode;
    public final String venueCity;
    public final String venuePhone;
    private final String startTimeText;  // Hold original value for saving in archive
    private Date startTime;
    private final int    travelTime;
    public final String accessInfo;
    //public String reference;
    private final String timezone;


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
            return getReferences(", ", false);
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
            if (!Objects.equals(this.startTime, otherEvent.startTime))          { return false; }
            if (this.travelTime != otherEvent.travelTime)                       { return false; }
            if (!Objects.equals(this.venueName, otherEvent.venueName))          { return false; }
            if (!Objects.equals(this.venueAddress, otherEvent.venueAddress))    { return false; }
            if (!Objects.equals(this.venuePostCode, otherEvent.venuePostCode))  { return false; }
            if (!Objects.equals(this.venueCity, otherEvent.venueCity))          { return false; }
            if (!Objects.equals(this.venuePhone, otherEvent.venuePhone))        { return false; }
            if (!Objects.equals(this.accessInfo, otherEvent.accessInfo))        { return false; }
            if (!Objects.equals(this.timezone, otherEvent.timezone))            { return false; }

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
        // New, simplified version
        // First delete any existing notifications for this trip element (not needed in Android?)
        //cancelNotifications();

        // Set notification (if we have a start time)
        if (getTense() == Tense.FUTURE) {
            //Context ctx = SHiTApplication.getContext();
            //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);

            int leadTimeEventMinutes = SHiTApplication.getPreferenceInt(Constants.Setting.ALERT_LEAD_TIME_EVENT, LEAD_TIME_MISSING);

            if (leadTimeEventMinutes != LEAD_TIME_MISSING) {
                if (travelTime > 0) {
                    leadTimeEventMinutes += travelTime;
                }

                setNotification(Constants.Setting.ALERT_LEAD_TIME_EVENT
                        , leadTimeEventMinutes
                        , R.string.alert_msg_event
                        , getNotificationClickAction()
                        , null);
            }
        }
    }

    @Override
    public Intent getActivityIntent(ActivityType activityType) {
        Intent i;
        switch (activityType) {
            case REGULAR:
                i = new Intent(SHiTApplication.getContext(), EventActivity.class);
                break;

            case POPUP:
                i = new Intent(SHiTApplication.getContext(), EventPopupActivity.class);
                break;

            default:
                return null;
        }

        i.putExtra(Constants.IntentExtra.TRIP_CODE, tripCode);
        i.putExtra(Constants.IntentExtra.ELEMENT_ID, String.valueOf(id));

        return i;
    }

    @Override
    protected String getNotificationClickAction() {
        return Constants.PushNotificationActions.EVENT_CLICK;
    }
}
