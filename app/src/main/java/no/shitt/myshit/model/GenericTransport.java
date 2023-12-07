package no.shitt.myshit.model;

import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import no.shitt.myshit.Constants;
import no.shitt.myshit.PrivateTransportActivity;
import no.shitt.myshit.PrivateTransportPopupActivity;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.ServerDate;
import no.shitt.myshit.helper.StringUtil;

public class GenericTransport extends TripElement {
    private final static String LOG_TAG = GenericTransport.class.getSimpleName();
    // MARK: Properties
    private int segmentId;
    private String segmentCode;
    int legNo;
    private String departureTimeText;  // Hold original value for saving in archive
    private Date   departureTime;
    public String departureLocation;
    public String departureStop;
    public String departureAddress;
    private String departureTimeZone;
    private String departureCoordinates;
    String departureTerminalCode;
    public String departureTerminalName;
    private String arrivalTimeText; // Hold original value for saving in archive
    private Date   arrivalTime;
    public String arrivalLocation;
    public String arrivalStop;
    public String arrivalAddress;
    private String arrivalTimeZone;
    private String arrivalCoordinates;
    String arrivalTerminalCode;
    public String arrivalTerminalName;
    public String routeNo;
    public String companyName;
    public String companyPhone;

    @Override
    public Date getStartTime() {
        return departureTime;
    }
    @Override
    public String getStartTimeZone() {
        return departureTimeZone;
    }
    @Override
    public Date getEndTime() {
        return arrivalTime;
    }
    @Override
    public String getEndTimeZone() {
        return arrivalTimeZone;
    }
    @Override
    public String getTitle() {
        return companyName;
    }
    @Override
    public String getStartInfo() {
        StringBuilder sb = new StringBuilder();
        StringUtil.appendWithLeadingSeparator(sb, departureStop, "", false);
        StringUtil.appendWithLeadingSeparator(sb, departureLocation, ", ", false);
        return sb.toString();
    }
    @Override
    public String getEndInfo() {
        StringBuilder sb = new StringBuilder();
        StringUtil.appendWithLeadingSeparator(sb, arrivalStop, "", false);
        StringUtil.appendWithLeadingSeparator(sb, arrivalLocation, ", ", false);
        return sb.toString();
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

        jo.put(Constants.JSON.ELEM_SEGMENT_ID, segmentId);
        jo.putOpt(Constants.JSON.ELEM_SEGMENT_CODE, segmentCode);
        jo.put(Constants.JSON.ELEM_LEG_NO, legNo);
        jo.putOpt(Constants.JSON.ELEM_DEP_LOCATION, departureLocation);
        jo.putOpt(Constants.JSON.ELEM_DEP_STOP, departureStop);
        jo.putOpt(Constants.JSON.ELEM_DEP_ADDR, departureAddress);
        jo.putOpt(Constants.JSON.ELEM_DEP_TZ, departureTimeZone);
        jo.putOpt(Constants.JSON.ELEM_DEP_TIME, departureTimeText);
        jo.putOpt(Constants.JSON.ELEM_DEP_COORDINATES, departureCoordinates);
        jo.putOpt(Constants.JSON.ELEM_DEP_TERMINAL_CODE, departureTerminalCode);
        jo.putOpt(Constants.JSON.ELEM_DEP_TERMINAL_NAME, departureTerminalName);
        jo.putOpt(Constants.JSON.ELEM_ARR_LOCATION, arrivalLocation);
        jo.putOpt(Constants.JSON.ELEM_ARR_STOP, arrivalStop);
        jo.putOpt(Constants.JSON.ELEM_ARR_ADDR, arrivalAddress);
        jo.putOpt(Constants.JSON.ELEM_ARR_TZ, arrivalTimeZone);
        jo.putOpt(Constants.JSON.ELEM_ARR_TIME, arrivalTimeText);
        jo.putOpt(Constants.JSON.ELEM_ARR_COORDINATES, arrivalCoordinates);
        jo.putOpt(Constants.JSON.ELEM_ARR_TERMINAL_CODE, arrivalTerminalCode);
        jo.putOpt(Constants.JSON.ELEM_ARR_TERMINAL_NAME, arrivalTerminalName);
        jo.putOpt(Constants.JSON.ELEM_ROUTE_NO, routeNo);
        jo.putOpt(Constants.JSON.ELEM_COMPANY, companyName);
        jo.putOpt(Constants.JSON.ELEM_PHONE, companyPhone);

        return jo;
    }


    GenericTransport(int tripId, String tripCode, JSONObject elementData) {
        super(tripId, tripCode, elementData);
        segmentId = elementData.optInt(Constants.JSON.ELEM_SEGMENT_ID);
        segmentCode = elementData.isNull(Constants.JSON.ELEM_SEGMENT_CODE) ? null : elementData.optString(Constants.JSON.ELEM_SEGMENT_CODE);
        legNo = elementData.optInt(Constants.JSON.ELEM_LEG_NO);

        departureLocation = elementData.isNull(Constants.JSON.ELEM_DEP_LOCATION) ? null : elementData.optString(Constants.JSON.ELEM_DEP_LOCATION);
        departureStop = elementData.isNull(Constants.JSON.ELEM_DEP_STOP) ? null : elementData.optString(Constants.JSON.ELEM_DEP_STOP);
        departureAddress = elementData.isNull(Constants.JSON.ELEM_DEP_ADDR) ? null : elementData.optString(Constants.JSON.ELEM_DEP_ADDR);
        departureTimeZone = elementData.isNull(Constants.JSON.ELEM_DEP_TZ) ? null : elementData.optString(Constants.JSON.ELEM_DEP_TZ);
        departureTimeText = elementData.isNull(Constants.JSON.ELEM_DEP_TIME) ? null : elementData.optString(Constants.JSON.ELEM_DEP_TIME);
        if (departureTimeText != null) {
            departureTime = ServerDate.convertServerDate(departureTimeText, departureTimeZone);
        }
        departureCoordinates = elementData.isNull(Constants.JSON.ELEM_DEP_COORDINATES) ? null : elementData.optString(Constants.JSON.ELEM_DEP_COORDINATES);
        departureTerminalCode = elementData.isNull(Constants.JSON.ELEM_DEP_TERMINAL_CODE) ? null : elementData.optString(Constants.JSON.ELEM_DEP_TERMINAL_CODE);
        departureTerminalName = elementData.isNull(Constants.JSON.ELEM_DEP_TERMINAL_NAME) ? null : elementData.optString(Constants.JSON.ELEM_DEP_TERMINAL_NAME);

        arrivalLocation = elementData.isNull(Constants.JSON.ELEM_ARR_LOCATION) ? null : elementData.optString(Constants.JSON.ELEM_ARR_LOCATION);
        arrivalStop = elementData.isNull(Constants.JSON.ELEM_ARR_STOP) ? null : elementData.optString(Constants.JSON.ELEM_ARR_STOP);
        arrivalAddress = elementData.isNull(Constants.JSON.ELEM_ARR_ADDR) ? null : elementData.optString(Constants.JSON.ELEM_ARR_ADDR);
        arrivalTimeZone = elementData.isNull(Constants.JSON.ELEM_ARR_TZ) ? null : elementData.optString(Constants.JSON.ELEM_ARR_TZ);
        arrivalTimeText = elementData.isNull(Constants.JSON.ELEM_ARR_TIME) ? null : elementData.optString(Constants.JSON.ELEM_ARR_TIME);
        if (arrivalTimeText != null) {
            arrivalTime = ServerDate.convertServerDate(arrivalTimeText, arrivalTimeZone);
        }
        arrivalCoordinates = elementData.isNull(Constants.JSON.ELEM_ARR_COORDINATES) ? null : elementData.optString(Constants.JSON.ELEM_ARR_COORDINATES);
        arrivalTerminalCode = elementData.isNull(Constants.JSON.ELEM_ARR_TERMINAL_CODE) ? null : elementData.optString(Constants.JSON.ELEM_ARR_TERMINAL_CODE);
        arrivalTerminalName = elementData.isNull(Constants.JSON.ELEM_ARR_TERMINAL_NAME) ? null : elementData.optString(Constants.JSON.ELEM_ARR_TERMINAL_NAME);

        routeNo = elementData.isNull(Constants.JSON.ELEM_ROUTE_NO) ? null : elementData.optString(Constants.JSON.ELEM_ROUTE_NO);
        companyName = elementData.isNull(Constants.JSON.ELEM_COMPANY) ? null : elementData.optString(Constants.JSON.ELEM_COMPANY);
        companyPhone = elementData.isNull(Constants.JSON.ELEM_PHONE) ? null : elementData.optString(Constants.JSON.ELEM_PHONE);
    }


    //
    // MARK: Methods
    //
    @Override
    boolean update(JSONObject elementData) {
        changed = super.update(elementData);

        subType = updateField(subType, elementData, Constants.JSON.ELEM_SUB_TYPE);

        segmentId = updateField(segmentId, elementData.optInt(Constants.JSON.ELEM_SEGMENT_ID));
        segmentCode = updateField(segmentCode, elementData, Constants.JSON.ELEM_SEGMENT_CODE);
        legNo = updateField(legNo, elementData.optInt(Constants.JSON.ELEM_LEG_NO));

        departureLocation = updateField(departureLocation, elementData, Constants.JSON.ELEM_DEP_LOCATION);
        departureStop = updateField(departureStop, elementData, Constants.JSON.ELEM_DEP_STOP);
        departureAddress = updateField(departureAddress, elementData, Constants.JSON.ELEM_DEP_ADDR);
        departureTimeZone = updateField(departureTimeZone, elementData, Constants.JSON.ELEM_DEP_TZ);
        departureTimeText = updateField(departureTimeText, elementData, Constants.JSON.ELEM_DEP_TIME);
        if (departureTimeText != null) {
            departureTime = ServerDate.convertServerDate(departureTimeText, departureTimeZone);
        }
        departureCoordinates = updateField(departureCoordinates, elementData, Constants.JSON.ELEM_DEP_COORDINATES);
        departureTerminalCode = updateField(departureTerminalCode, elementData, Constants.JSON.ELEM_DEP_TERMINAL_CODE);
        departureTerminalName = updateField(departureTerminalName, elementData, Constants.JSON.ELEM_DEP_TERMINAL_NAME);

        arrivalLocation = updateField(arrivalLocation, elementData, Constants.JSON.ELEM_ARR_LOCATION);
        arrivalStop = updateField(arrivalStop, elementData, Constants.JSON.ELEM_ARR_STOP);
        arrivalAddress = updateField(arrivalAddress, elementData, Constants.JSON.ELEM_ARR_ADDR);
        arrivalTimeZone = updateField(arrivalTimeZone, elementData, Constants.JSON.ELEM_ARR_TZ);
        arrivalTimeText = updateField(arrivalTimeText, elementData, Constants.JSON.ELEM_ARR_TIME);
        if (arrivalTimeText != null) {
            arrivalTime = ServerDate.convertServerDate(arrivalTimeText, arrivalTimeZone);
        }
        arrivalCoordinates = updateField(arrivalCoordinates, elementData, Constants.JSON.ELEM_ARR_COORDINATES);
        arrivalTerminalCode = updateField(arrivalTerminalCode, elementData, Constants.JSON.ELEM_ARR_TERMINAL_CODE);
        arrivalTerminalName = updateField(arrivalTerminalName, elementData, Constants.JSON.ELEM_ARR_TERMINAL_NAME);

        routeNo = updateField(routeNo, elementData, Constants.JSON.ELEM_ROUTE_NO);
        companyName = updateField(companyName, elementData, Constants.JSON.ELEM_COMPANY);
        companyPhone = updateField(companyPhone, elementData, Constants.JSON.ELEM_PHONE);

        if (changed && ( this.getClass() == GenericTransport.class) ) {
            setNotification();
        }
        return changed;
    }


    @Override
    public String startTime(Integer dateStyle, Integer timeStyle) {
        if (departureTime != null) {
            DateFormat dateFormatter;
            if (dateStyle == null) {
                dateFormatter = SimpleDateFormat.getTimeInstance(timeStyle);
            } else if (timeStyle == null) {
                dateFormatter = SimpleDateFormat.getDateInstance(dateStyle);
            } else {
                dateFormatter = SimpleDateFormat.getDateTimeInstance(dateStyle, timeStyle);
            }
            if (departureTimeZone != null) {
                TimeZone timezone = TimeZone.getTimeZone(departureTimeZone);
                dateFormatter.setTimeZone(timezone);
            }

            return dateFormatter.format(departureTime);
        }
        return null;
    }

    @Override
    public String endTime(Integer dateStyle, Integer timeStyle) {
        if (arrivalTime != null) {
            DateFormat dateFormatter;
            if (dateStyle == null) {
                dateFormatter = SimpleDateFormat.getTimeInstance(timeStyle);
            } else if (timeStyle == null) {
                dateFormatter = SimpleDateFormat.getDateInstance(dateStyle);
            } else {
                dateFormatter = SimpleDateFormat.getDateTimeInstance(dateStyle, timeStyle);
            }
            if (arrivalTimeZone != null) {
                TimeZone timezone = TimeZone.getTimeZone(arrivalTimeZone);
                dateFormatter.setTimeZone(timezone);
            }

            return dateFormatter.format(arrivalTime);
        }
        return null;
    }

    // TODO: Create new activity for GenericTransport
    @Override
    public Intent getActivityIntent(ActivityType activityType) {
        Intent i;
        switch (activityType) {
            case REGULAR:
                i = new Intent(SHiTApplication.getContext(), PrivateTransportActivity.class);
                break;

            case POPUP:
                i = new Intent(SHiTApplication.getContext(), PrivateTransportPopupActivity.class);
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
        return Constants.PushNotificationActions.PRIVATE_TRANSPORT_CLICK;
    }
}
