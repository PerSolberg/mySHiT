package no.shitt.myshit.model;

import android.content.Context;
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
import no.shitt.myshit.HotelActivity;
import no.shitt.myshit.HotelPopupActivity;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.ServerDate;

public class Hotel extends TripElement {
    private final static String LOG_TAG = Hotel.class.getSimpleName();

    // MARK: Properties
    private String checkInDateText;  // Hold original value for saving in archive
    private String checkOutDateText; // Hold original value for saving in archive
    private Date checkInDate;
    private Date checkOutDate;
    public String hotelName;
    public String address;
    public String postCode;
    public String city;
    public String phone;
    public String transferInfo;
    private String timezone;


    @Override
    public Date getStartTime() {
        return checkInDate;
    }
    @Override
    public Date getEndTime() {
        return checkOutDate;
    }
    @Override
    public String getTitle() {
        return hotelName;
    }
    @Override
    public String getStartInfo() {
        Context ctx = SHiTApplication.getContext();

        if (checkInDate != null && checkOutDate != null) {
            return DateUtils.formatDateRange(ctx, checkInDate.getTime(), checkOutDate.getTime(), DateUtils.FORMAT_SHOW_DATE + DateUtils.FORMAT_ABBREV_MONTH);
        } else if (checkInDate != null) {
            Log.w(LOG_TAG, "Checkout date missing");
            return startTime(DateFormat.SHORT, DateFormat.SHORT);
        } else if (checkOutDate != null) {
            Log.w(LOG_TAG, "Checkin date missing");
            return endTime(DateFormat.SHORT, DateFormat.SHORT);
        } else {
            Log.w(LOG_TAG, "Checkin and checkout date missing");
            return "";
        }
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

        jo.putOpt(Constants.JSON.ELEM_HOTEL_TIMEZONE, timezone);
        jo.putOpt(Constants.JSON.ELEM_HOTEL_CHECK_IN, checkInDateText);
        jo.putOpt(Constants.JSON.ELEM_HOTEL_CHECK_OUT, checkOutDateText);
        jo.putOpt(Constants.JSON.ELEM_HOTEL_NAME, hotelName);
        jo.putOpt(Constants.JSON.ELEM_HOTEL_ADDR, address);
        jo.putOpt(Constants.JSON.ELEM_HOTEL_POST_CODE, postCode);
        jo.putOpt(Constants.JSON.ELEM_HOTEL_CITY, city);
        jo.putOpt(Constants.JSON.ELEM_HOTEL_PHONE, phone);
        jo.putOpt(Constants.JSON.ELEM_HOTEL_TRANSFER_INFO, transferInfo);

        return jo;
    }

    Hotel(int tripId, String tripCode, JSONObject elementData) {
        super(tripId, tripCode, elementData);

        timezone = elementData.isNull(Constants.JSON.ELEM_HOTEL_TIMEZONE) ? null : elementData.optString(Constants.JSON.ELEM_HOTEL_TIMEZONE);
        checkInDateText = elementData.isNull(Constants.JSON.ELEM_HOTEL_CHECK_IN) ? null : elementData.optString(Constants.JSON.ELEM_HOTEL_CHECK_IN);
        if (checkInDateText != null) {
            checkInDate = ServerDate.convertServerDate(checkInDateText, timezone);
        }
        checkOutDateText = elementData.isNull(Constants.JSON.ELEM_HOTEL_CHECK_OUT) ? null : elementData.optString(Constants.JSON.ELEM_HOTEL_CHECK_OUT);
        if (checkOutDateText != null) {
            checkOutDate = ServerDate.convertServerDate(checkOutDateText, timezone);
        }

        hotelName = elementData.isNull(Constants.JSON.ELEM_HOTEL_NAME) ? null : elementData.optString(Constants.JSON.ELEM_HOTEL_NAME);
        address = elementData.isNull(Constants.JSON.ELEM_HOTEL_ADDR) ? null : elementData.optString(Constants.JSON.ELEM_HOTEL_ADDR);
        postCode = elementData.isNull(Constants.JSON.ELEM_HOTEL_POST_CODE) ? null : elementData.optString(Constants.JSON.ELEM_HOTEL_POST_CODE);
        city = elementData.isNull(Constants.JSON.ELEM_HOTEL_CITY) ? null : elementData.optString(Constants.JSON.ELEM_HOTEL_CITY);
        phone = elementData.isNull(Constants.JSON.ELEM_HOTEL_PHONE) ? null : elementData.optString(Constants.JSON.ELEM_HOTEL_PHONE);
        transferInfo = elementData.isNull(Constants.JSON.ELEM_HOTEL_TRANSFER_INFO) ? null : elementData.optString(Constants.JSON.ELEM_HOTEL_TRANSFER_INFO);
    }


    //
    // MARK: Methods
    //
    @Override
    boolean update(JSONObject elementData) {
        changed = super.update(elementData);

        timezone = updateField(timezone, elementData, Constants.JSON.ELEM_HOTEL_TIMEZONE);
        checkInDateText = updateField(checkInDateText, elementData, Constants.JSON.ELEM_HOTEL_CHECK_IN);
        if (checkInDateText != null) {
            checkInDate = ServerDate.convertServerDate(checkInDateText, timezone);
        }
        checkOutDateText = updateField(checkOutDateText, elementData, Constants.JSON.ELEM_HOTEL_CHECK_OUT);
        if (checkOutDateText != null) {
            checkOutDate = ServerDate.convertServerDate(checkOutDateText, timezone);
        }

        hotelName = updateField(hotelName, elementData, Constants.JSON.ELEM_HOTEL_NAME);
        address = updateField(address, elementData, Constants.JSON.ELEM_HOTEL_ADDR);
        postCode = updateField(postCode, elementData, Constants.JSON.ELEM_HOTEL_POST_CODE);
        city = updateField(city, elementData, Constants.JSON.ELEM_HOTEL_CITY);
        phone = updateField(phone, elementData, Constants.JSON.ELEM_HOTEL_PHONE);
        transferInfo = updateField(transferInfo, elementData, Constants.JSON.ELEM_HOTEL_TRANSFER_INFO);

        if (changed && ( this.getClass() == Hotel.class) ) {
            setNotification();
        }
        return changed;
    }


    @Override
    public String startTime(Integer dateStyle, Integer timeStyle) {
        if (checkInDate != null) {
            DateFormat dateFormatter;
            if (dateStyle == null) {
                dateFormatter = SimpleDateFormat.getTimeInstance(timeStyle);
            } else if (timeStyle == null) {
                dateFormatter = SimpleDateFormat.getDateInstance(dateStyle);
            } else {
                dateFormatter = SimpleDateFormat.getDateTimeInstance(dateStyle, timeStyle);
            }
            if (timezone != null) {
                TimeZone timezone = TimeZone.getTimeZone(this.timezone);
                dateFormatter.setTimeZone(timezone);
            }

            return dateFormatter.format(checkInDate);
        }
        return null;
    }

    @Override
    public String endTime(Integer dateStyle, Integer timeStyle) {
        if (checkOutDate != null) {
            DateFormat dateFormatter;
            if (dateStyle == null) {
                dateFormatter = SimpleDateFormat.getTimeInstance(timeStyle);
            } else if (timeStyle == null) {
                dateFormatter = SimpleDateFormat.getDateInstance(dateStyle);
            } else {
                dateFormatter = SimpleDateFormat.getDateTimeInstance(dateStyle, timeStyle);
            }
            if (timezone != null) {
                TimeZone timezone = TimeZone.getTimeZone(this.timezone);
                dateFormatter.setTimeZone(timezone);
            }

            return dateFormatter.format(checkOutDate);
        }
        return null;
    }


    @Override
    public Intent getActivityIntent(ActivityType activityType) {
        Intent i;
        switch (activityType) {
            case REGULAR:
                i = new Intent(SHiTApplication.getContext(), HotelActivity.class);
                break;

            case POPUP:
                i = new Intent(SHiTApplication.getContext(), HotelPopupActivity.class);
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
        return Constants.PushNotificationActions.HOTEL_CLICK;
    }
}
