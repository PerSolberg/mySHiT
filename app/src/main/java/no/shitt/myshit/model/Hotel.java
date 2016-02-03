package no.shitt.myshit.model;

import android.content.Context;
import android.text.format.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import no.shitt.myshit.Constants;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.ServerDate;

public class Hotel extends TripElement {
    // MARK: Properties
    String checkInDateText;  // Hold original value for saving in archive
    String checkOutDateText; // Hold original value for saving in archive
    Date checkInDate;
    Date checkOutDate;
    String hotelName;
    String address;
    String postCode;
    String city;
    String phone;
    String transferInfo;
    String timezone;



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
        DateFormat dateFormatter = android.text.format.DateFormat.getTimeFormat(ctx);
        //dateFormatter.dateStyle = NSDateFormatterStyle.MediumStyle
        //dateFormatter.timeStyle = NSDateFormatterStyle.NoStyle

        return DateUtils.formatDateRange(ctx, checkInDate.getTime(), checkOutDate.getTime(), DateUtils.FORMAT_SHOW_DATE + DateUtils.FORMAT_ABBREV_MONTH);
        //return dateFormatter.format(checkInDate) + " - " + dateFormatter.format(checkOutDate);
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

    Hotel(JSONObject elementData) {
        super(elementData);

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

    // MARK: Methods
    @Override
    public boolean isEqual(Object otherObject) {
        if (this.getClass() != otherObject.getClass()) {
            return false;
        }
        try {
            Hotel otherHotel = (Hotel) otherObject;
            if (!this.checkInDate.equals(otherHotel.checkInDate))    { return false; }
            if (!this.checkOutDate.equals(otherHotel.checkOutDate))  { return false; }
            if (!this.hotelName.equals(otherHotel.hotelName))        { return false; }
            if (!this.address.equals(otherHotel.address))            { return false; }
            if (!this.postCode.equals(otherHotel.postCode))          { return false; }
            if (!this.city.equals(otherHotel.city))                  { return false; }
            if (!this.phone.equals(otherHotel.phone))                { return false; }
            if (!this.transferInfo.equals(otherHotel.transferInfo))  { return false; }
            if (!this.timezone.equals(otherHotel.timezone))          { return false; }

            return super.isEqual(otherObject);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String startTime(int dateTimeStyle) {
        if (checkInDate != null) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat();
            //dateFormatter.dateStyle = dateStyle
            //dateFormatter.timeStyle = timeStyle
            if (timezone != null) {
                TimeZone timezone = TimeZone.getTimeZone(this.timezone);
                dateFormatter.setTimeZone(timezone);
            }

            return dateFormatter.format(checkInDate);
        }
        return null;
    }

    @Override
    public String endTime(int dateTimeStyle) {
        if (checkOutDate != null) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat();
            //dateFormatter.dateStyle = dateStyle
            //dateFormatter.timeStyle = timeStyle
            if (timezone != null) {
                TimeZone timezone = TimeZone.getTimeZone(this.timezone);
                dateFormatter.setTimeZone(timezone);
            }

            return dateFormatter.format(checkOutDate);
        }
        return null;
    }

}
