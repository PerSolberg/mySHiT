package no.shitt.myshit.model;

import android.content.Context;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import no.shitt.myshit.Constants;
import no.shitt.myshit.helper.ServerDate;

public class Hotel extends TripElement {
    // MARK: Properties
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
    public String getTitle(Context ctx) {
        return hotelName;
    }
    @Override
    public String getStartInfo(Context ctx) {
        DateFormat dateFormatter = android.text.format.DateFormat.getTimeFormat(ctx);
        //dateFormatter.dateStyle = NSDateFormatterStyle.MediumStyle
        //dateFormatter.timeStyle = NSDateFormatterStyle.NoStyle

        return dateFormatter.format(checkInDate) + " - " + dateFormatter.format(checkOutDate);
    }
    @Override
    public String getEndInfo(Context ctx) {
        //DateFormat dateFormatter = android.text.format.DateFormat.getTimeFormat();
        return null;
        //return dateFormatter.stringFromDate(checkOutTime!)
    }
    @Override
    public String getDetailInfo(Context ctx) {
        if (references != null) {
            String refList = "";
            for (int i = 0; i < references.size(); i++) {
                refList = refList + (refList.equals("") ? "" : ", ") + references.get(i).get("refNo");
            }
            return refList;
        }
        return null;
    }

    /* Identifiers for keyed archive (iOS only?)
    struct PropertyKey {
        static let checkInDateKey = "checkInDate"
        static let checkOutDateKey = "checkOutDate"
        static let hotelNameKey = "hotelName"
        static let addressKey = "address"
        static let postCodeKey = "postCode"
        static let cityKey = "city"
        static let phoneKey = "phone"
        static let transferInfoKey = "transferInfo"
        static let timezoneKey = "timezone"
    }
    */

    /* Encode for keyed archive (iOS only?)
    // MARK: NSCoding
    override func encodeWithCoder(aCoder: NSCoder) {
        super.encodeWithCoder(aCoder)
        aCoder.encodeObject(checkInDate, forKey: PropertyKey.checkInDateKey)
        aCoder.encodeObject(checkOutDate, forKey: PropertyKey.checkOutDateKey)
        aCoder.encodeObject(hotelName, forKey: PropertyKey.hotelNameKey)
        aCoder.encodeObject(address, forKey: PropertyKey.addressKey)
        aCoder.encodeObject(postCode, forKey: PropertyKey.postCodeKey)
        aCoder.encodeObject(city, forKey: PropertyKey.cityKey)
        aCoder.encodeObject(phone, forKey: PropertyKey.phoneKey)
        aCoder.encodeObject(transferInfo, forKey: PropertyKey.transferInfoKey)
        aCoder.encodeObject(timezone, forKey: PropertyKey.timezoneKey)
    }
    */

    // MARK: Constructors
    /* Decode from keyed archive (iOS only?)
    required init?(coder aDecoder: NSCoder) {
        // NB: use conditional cast (as?) for any optional properties
        super.init(coder: aDecoder)
        checkInDate = aDecoder.decodeObjectForKey(PropertyKey.checkInDateKey) as? NSDate
        checkOutDate = aDecoder.decodeObjectForKey(PropertyKey.checkOutDateKey) as? NSDate
        hotelName = aDecoder.decodeObjectForKey(PropertyKey.hotelNameKey) as? String
        address = aDecoder.decodeObjectForKey(PropertyKey.addressKey) as? String
        postCode = aDecoder.decodeObjectForKey(PropertyKey.postCodeKey) as? String
        city = aDecoder.decodeObjectForKey(PropertyKey.cityKey) as? String
        phone = aDecoder.decodeObjectForKey(PropertyKey.phoneKey) as? String
        transferInfo = aDecoder.decodeObjectForKey(PropertyKey.transferInfoKey) as? String
        timezone = aDecoder.decodeObjectForKey(PropertyKey.timezoneKey) as? String
    }
    */

    Hotel(JSONObject elementData) {
        super(elementData);

        String checkInDateText = elementData.optString(Constants.JSON.ELEM_HOTEL_CHECK_IN);
        if (checkInDateText != null) {
            checkInDate = ServerDate.convertServerDate(checkInDateText, timezone);
        }
        String checkOutDateText = elementData.optString(Constants.JSON.ELEM_HOTEL_CHECK_OUT);
        if (checkOutDateText != null) {
            checkOutDate = ServerDate.convertServerDate(checkOutDateText, timezone);
        }

        hotelName = elementData.optString(Constants.JSON.ELEM_HOTEL_NAME);
        address = elementData.optString(Constants.JSON.ELEM_HOTEL_ADDR);
        postCode = elementData.optString(Constants.JSON.ELEM_HOTEL_POST_CODE);
        city = elementData.optString(Constants.JSON.ELEM_HOTEL_CITY);
        phone = elementData.optString(Constants.JSON.ELEM_HOTEL_PHONE);
        transferInfo = elementData.optString(Constants.JSON.ELEM_HOTEL_TRANSFER_INFO);
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
