package no.shitt.myshit.model;

import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private final static String LOG_TAG = Event.class.getSimpleName();
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
        return startTime(null, DateFormat.SHORT);
    }
    @Override
    public String getEndInfo() {
        return null;
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
    }


    //
    // MARK: Methods
    //
    @Override
    boolean update(JSONObject elementData) {
        changed = super.update(elementData);

        timezone = updateField(timezone, elementData, Constants.JSON.ELEM_EVENT_TIMEZONE);

        startTimeText = updateField(startTimeText, elementData, Constants.JSON.ELEM_EVENT_START_TIME);
        if (startTimeText != null) {
            startTime = ServerDate.convertServerDate(startTimeText, timezone);
        }

        venueName = updateField(venueName, elementData, Constants.JSON.ELEM_EVENT_VENUE_NAME);
        venueAddress = updateField(venueAddress, elementData, Constants.JSON.ELEM_EVENT_VENUE_ADDR);
        venuePostCode = updateField(venuePostCode, elementData, Constants.JSON.ELEM_EVENT_VENUE_POST_CODE);
        venueCity = updateField(venueCity, elementData, Constants.JSON.ELEM_EVENT_VENUE_CITY);
        venuePhone = updateField(venuePhone, elementData, Constants.JSON.ELEM_EVENT_VENUE_PHONE);
        accessInfo = updateField(accessInfo, elementData, Constants.JSON.ELEM_EVENT_ACCESS_INFO);
        travelTime = updateField(travelTime, elementData.optInt(Constants.JSON.ELEM_EVENT_TRAVEL_TIME, TRAVEL_TIME_UNKNOWN));

        if (changed && ( this.getClass() == Event.class) ) {
            setNotification();
        }
        return changed;
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
        // Set notification (if we have a start time)
        if (getTense() == Tense.FUTURE) {
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
