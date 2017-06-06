package no.shitt.myshit.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import no.shitt.myshit.R;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.SchedulingService;
import no.shitt.myshit.helper.ServerDate;
import no.shitt.myshit.helper.StringUtil;

public class GenericTransport extends TripElement {
    // MARK: Properties
    int segmentId;
    String segmentCode;
    int legNo;
    String departureTimeText;  // Hold original value for saving in archive
    Date   departureTime;
    public String departureLocation;
    public String departureStop;
    public String departureAddress;
    String departureTimeZone;
    String departureCoordinates;
    String departureTerminalCode;
    public String departureTerminalName;
    String arrivalTimeText; // Hold original value for saving in archive
    Date   arrivalTime;
    public String arrivalLocation;
    public String arrivalStop;
    public String arrivalAddress;
    String arrivalTimeZone;
    String arrivalCoordinates;
    String arrivalTerminalCode;
    public String arrivalTerminalName;
    public String routeNo;
    public String companyName;
    public String companyPhone;

    //private static int alarmcounter = 0;

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

    // MARK: Methods
    @Override
    public boolean isEqual(Object otherObject) {
        if (this.getClass() != otherObject.getClass()) {
            //Log.d("GenericTransport", "Changed class!");
            return false;
        }
        try {
            GenericTransport otherTransport = (GenericTransport) otherObject;
            if (this.segmentId             != otherTransport.segmentId                             ) { return false; }
            if (this.legNo                 != otherTransport.legNo                                 ) { return false; }
            if (!StringUtil.equal(this.segmentCode, otherTransport.segmentCode)                    ) { return false; }
            if (!ServerDate.equal(this.departureTime, otherTransport.departureTime)                ) { return false; }
            if (!StringUtil.equal(this.departureLocation, otherTransport.departureLocation)        ) { return false; }
            if (!StringUtil.equal(this.departureStop, otherTransport.departureStop)                ) { return false; }
            if (!StringUtil.equal(this.departureAddress, otherTransport.departureAddress)          ) { return false; }
            if (!StringUtil.equal(this.departureTimeZone, otherTransport.departureTimeZone)        ) { return false; }
            if (!StringUtil.equal(this.departureCoordinates, otherTransport.departureCoordinates)  ) { return false; }
            if (!StringUtil.equal(this.departureTerminalCode, otherTransport.departureTerminalCode)) { return false; }
            if (!StringUtil.equal(this.departureTerminalName, otherTransport.departureTerminalName)) { return false; }
            if (!ServerDate.equal(this.arrivalTime, otherTransport.arrivalTime)                    ) { return false; }
            if (!StringUtil.equal(this.arrivalLocation, otherTransport.arrivalLocation)            ) { return false; }
            if (!StringUtil.equal(this.arrivalStop, otherTransport.arrivalStop)                    ) { return false; }
            if (!StringUtil.equal(this.arrivalAddress, otherTransport.arrivalAddress)              ) { return false; }
            if (!StringUtil.equal(this.arrivalTimeZone, otherTransport.arrivalTimeZone)            ) { return false; }
            if (!StringUtil.equal(this.arrivalCoordinates, otherTransport.arrivalCoordinates)      ) { return false; }
            if (!StringUtil.equal(this.arrivalTerminalCode, otherTransport.arrivalTerminalCode)    ) { return false; }
            if (!StringUtil.equal(this.arrivalTerminalName, otherTransport.arrivalTerminalName)    ) { return false; }
            if (!StringUtil.equal(this.routeNo, otherTransport.routeNo)                            ) { return false; }
            if (!StringUtil.equal(this.companyName, otherTransport.companyName)                    ) { return false; }
            if (!StringUtil.equal(this.companyPhone, otherTransport.companyPhone)                  ) { return false; }

            return super.isEqual(otherObject);
        } catch (Exception e) {
            //Log.d("GenericTransport", "Comparison failed with exception");
            return false;
        }
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
}
