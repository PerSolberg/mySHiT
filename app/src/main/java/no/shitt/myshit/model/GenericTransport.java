package no.shitt.myshit.model;

import android.content.Context;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import no.shitt.myshit.Constants;
import no.shitt.myshit.helper.ServerDate;

public class GenericTransport extends TripElement {
    // MARK: Properties
    int segmentId;
    String segmentCode;
    int legNo;
    Date departureTime;
    String departureLocation;
    String departureStop;
    String departureAddress;
    String departureTimeZone;
    String departureCoordinates;
    String departureTerminalCode;
    String departureTerminalName;
    Date arrivalTime;
    String arrivalLocation;
    String arrivalStop;
    String arrivalAddress;
    String arrivalTimeZone;
    String arrivalCoordinates;
    String arrivalTerminalCode;
    String arrivalTerminalName;
    String routeNo;
    String companyName;
    String companyPhone;

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
    public String getTitle(Context ctx) {
        return companyName;
    }
    @Override
    public String getStartInfo(Context ctx) {
        return departureLocation;
    }
    @Override
    public String getEndInfo(Context ctx) {
        return arrivalLocation;
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
        static let segmentIdKey = "segmentId"
        static let segmentCodeKey = "segmentCode"
        static let legNoKey = "legNo"
        static let departureTimeKey = "departureTime"
        static let departureLocationKey = "departureLocation"
        static let departureStopKey = "departureStop"
        static let departureAddressKey = "departureAddress"
        static let departureTimeZoneKey = "departureTimeZone"
        static let departureCoordinatesKey = "departureCoordinates"
        static let departureTerminalCodeKey = "departureTerminalCode"
        static let departureTerminalNameKey = "departureTerminalName"
        static let arrivalTimeKey = "arrivalTime"
        static let arrivalLocationKey = "arrivalLocation"
        static let arrivalStopKey = "arrivalStop"
        static let arrivalAddressKey = "arrivalAddress"
        static let arrivalTimeZoneKey = "arrivalTimeZone"
        static let arrivalCoordinatesKey = "arrivalCoordinates"
        static let arrivalTerminalCodeKey = "arrivalTerminalCode"
        static let arrivalTerminalNameKey = "arrivalTerminalName"
        static let routeNoKey = "routeNo"
        static let companyNameKey = "companyName"
        static let companyPhoneKey = "companyPhone"
    }
    */

    /* Encode for keyed archive (iOS only?)
    // MARK: NSCoding
    override func encodeWithCoder(aCoder: NSCoder) {
        super.encodeWithCoder(aCoder)
        aCoder.encodeObject(segmentId, forKey: PropertyKey.segmentIdKey)
        aCoder.encodeObject(segmentCode, forKey: PropertyKey.segmentCodeKey)
        aCoder.encodeObject(legNo, forKey: PropertyKey.legNoKey)
        aCoder.encodeObject(departureTime, forKey: PropertyKey.departureTimeKey)
        aCoder.encodeObject(departureLocation, forKey: PropertyKey.departureLocationKey)
        aCoder.encodeObject(departureStop, forKey: PropertyKey.departureStopKey)
        aCoder.encodeObject(departureAddress, forKey: PropertyKey.departureAddressKey)
        aCoder.encodeObject(departureTimeZone, forKey: PropertyKey.departureTimeZoneKey)
        aCoder.encodeObject(departureCoordinates, forKey: PropertyKey.departureCoordinatesKey)
        aCoder.encodeObject(departureTerminalCode, forKey: PropertyKey.departureTerminalCodeKey)
        aCoder.encodeObject(departureTerminalName, forKey: PropertyKey.departureTerminalNameKey)
        aCoder.encodeObject(arrivalTime, forKey: PropertyKey.arrivalTimeKey)
        aCoder.encodeObject(arrivalLocation, forKey: PropertyKey.arrivalLocationKey)
        aCoder.encodeObject(arrivalStop, forKey: PropertyKey.arrivalStopKey)
        aCoder.encodeObject(arrivalAddress, forKey: PropertyKey.arrivalAddressKey)
        aCoder.encodeObject(arrivalTimeZone, forKey: PropertyKey.arrivalTimeZoneKey)
        aCoder.encodeObject(arrivalCoordinates, forKey: PropertyKey.arrivalCoordinatesKey)
        aCoder.encodeObject(arrivalTerminalCode, forKey: PropertyKey.arrivalTerminalCodeKey)
        aCoder.encodeObject(arrivalTerminalName, forKey: PropertyKey.arrivalTerminalNameKey)
        aCoder.encodeObject(routeNo, forKey: PropertyKey.routeNoKey)
        aCoder.encodeObject(companyName, forKey: PropertyKey.companyNameKey)
        aCoder.encodeObject(companyPhone, forKey: PropertyKey.companyPhoneKey)
    }
    */

    // MARK: Constructors
    /* Decode from keyed archive (iOS only?)
    required init?(coder aDecoder: NSCoder) {
        // NB: use conditional cast (as?) for any optional properties
        super.init(coder: aDecoder)
        segmentId = aDecoder.decodeObjectForKey(PropertyKey.segmentIdKey) as? Int
        segmentCode = aDecoder.decodeObjectForKey(PropertyKey.segmentCodeKey) as? String
        legNo = aDecoder.decodeObjectForKey(PropertyKey.legNoKey) as? Int
        departureTime  = aDecoder.decodeObjectForKey(PropertyKey.departureTimeKey) as? NSDate
        departureLocation = aDecoder.decodeObjectForKey(PropertyKey.departureLocationKey) as? String
        departureStop = aDecoder.decodeObjectForKey(PropertyKey.departureStopKey) as? String
        departureAddress = aDecoder.decodeObjectForKey(PropertyKey.departureAddressKey) as? String
        departureTimeZone = aDecoder.decodeObjectForKey(PropertyKey.departureTimeZoneKey) as? String
        departureCoordinates = aDecoder.decodeObjectForKey(PropertyKey.departureCoordinatesKey) as? String
        departureTerminalCode = aDecoder.decodeObjectForKey(PropertyKey.departureTerminalCodeKey) as? String
        departureTerminalName = aDecoder.decodeObjectForKey(PropertyKey.departureTerminalNameKey) as? String
        arrivalTime = aDecoder.decodeObjectForKey(PropertyKey.arrivalTimeKey) as? NSDate
        arrivalLocation = aDecoder.decodeObjectForKey(PropertyKey.arrivalLocationKey) as? String
        arrivalStop = aDecoder.decodeObjectForKey(PropertyKey.arrivalStopKey) as? String
        arrivalAddress = aDecoder.decodeObjectForKey(PropertyKey.arrivalAddressKey) as? String
        arrivalTimeZone = aDecoder.decodeObjectForKey(PropertyKey.arrivalTimeZoneKey) as? String
        arrivalCoordinates = aDecoder.decodeObjectForKey(PropertyKey.arrivalCoordinatesKey) as? String
        arrivalTerminalCode = aDecoder.decodeObjectForKey(PropertyKey.arrivalTerminalCodeKey) as? String
        arrivalTerminalName = aDecoder.decodeObjectForKey(PropertyKey.arrivalTerminalNameKey) as? String
        routeNo = aDecoder.decodeObjectForKey(PropertyKey.routeNoKey) as? String
        companyName = aDecoder.decodeObjectForKey(PropertyKey.companyNameKey) as? String
        companyPhone = aDecoder.decodeObjectForKey(PropertyKey.companyPhoneKey) as? String
    }
    */

    GenericTransport(JSONObject elementData) {
        super(elementData);
        segmentId = elementData.optInt(Constants.JSON.ELEM_SEGMENT_ID);
        segmentCode = elementData.optString(Constants.JSON.ELEM_SEGMENT_CODE);
        legNo = elementData.optInt(Constants.JSON.ELEM_LEG_NO);

        departureLocation = elementData.optString(Constants.JSON.ELEM_DEP_LOCATION);
        departureStop = elementData.optString(Constants.JSON.ELEM_DEP_STOP);
        departureAddress = elementData.optString(Constants.JSON.ELEM_DEP_ADDR);
        departureTimeZone = elementData.optString(Constants.JSON.ELEM_DEP_TZ);
        String depTimeText = elementData.optString(Constants.JSON.ELEM_DEP_TIME);
        if (depTimeText != null) {
            departureTime = ServerDate.convertServerDate(depTimeText, departureTimeZone);
        }
        departureCoordinates = elementData.optString(Constants.JSON.ELEM_DEP_COORDINATES);
        departureTerminalCode = elementData.optString(Constants.JSON.ELEM_DEP_TERMINAL_CODE);
        departureTerminalName = elementData.optString(Constants.JSON.ELEM_DEP_TERMINAL_NAME);

        arrivalLocation = elementData.optString(Constants.JSON.ELEM_ARR_LOCATION);
        arrivalStop = elementData.optString(Constants.JSON.ELEM_ARR_STOP);
        arrivalAddress = elementData.optString(Constants.JSON.ELEM_ARR_ADDR);
        arrivalTimeZone = elementData.optString(Constants.JSON.ELEM_ARR_TZ);
        String arrTimeText = elementData.optString(Constants.JSON.ELEM_ARR_TIME);
        if (arrTimeText != null) {
            arrivalTime = ServerDate.convertServerDate(arrTimeText, arrivalTimeZone);
        }
        arrivalCoordinates = elementData.optString(Constants.JSON.ELEM_ARR_COORDINATES);
        arrivalTerminalCode = elementData.optString(Constants.JSON.ELEM_ARR_TERMINAL_CODE);
        arrivalTerminalName = elementData.optString(Constants.JSON.ELEM_ARR_TERMINAL_NAME);

        routeNo = elementData.optString(Constants.JSON.ELEM_ROUTE_NO);
        companyName = elementData.optString(Constants.JSON.ELEM_COMPANY);
        companyPhone = elementData.optString(Constants.JSON.ELEM_PHONE);
    }

    // MARK: Methods
    @Override
    public boolean isEqual(Object otherObject) {
        if (this.getClass() != otherObject.getClass()) {
            return false;
        }
        try {
            GenericTransport otherTransport = (GenericTransport) otherObject;
            if (this.segmentId             != otherTransport.segmentId                  ) { return false; }
            if (this.legNo                 != otherTransport.legNo                      ) { return false; }
            if (!this.segmentCode.equals(otherTransport.segmentCode)                    ) { return false; }
            if (!this.departureTime.equals(otherTransport.departureTime)                ) { return false; }
            if (!this.departureLocation.equals(otherTransport.departureLocation)        ) { return false; }
            if (!this.departureStop.equals(otherTransport.departureStop)                ) { return false; }
            if (!this.departureAddress.equals(otherTransport.departureAddress)          ) { return false; }
            if (!this.departureTimeZone.equals(otherTransport.departureTimeZone)        ) { return false; }
            if (!this.departureCoordinates.equals(otherTransport.departureCoordinates)  ) { return false; }
            if (!this.departureTerminalCode.equals(otherTransport.departureTerminalCode)) { return false; }
            if (!this.departureTerminalName.equals(otherTransport.departureTerminalName)) { return false; }
            if (!this.arrivalTime.equals(otherTransport.arrivalTime)                    ) { return false; }
            if (!this.arrivalLocation.equals(otherTransport.arrivalLocation)            ) { return false; }
            if (!this.arrivalStop.equals(otherTransport.arrivalStop)                    ) { return false; }
            if (!this.arrivalAddress.equals(otherTransport.arrivalAddress)              ) { return false; }
            if (!this.arrivalTimeZone.equals(otherTransport.arrivalTimeZone)            ) { return false; }
            if (!this.arrivalCoordinates.equals(otherTransport.arrivalCoordinates)      ) { return false; }
            if (!this.arrivalTerminalCode.equals(otherTransport.arrivalTerminalCode)    ) { return false; }
            if (!this.arrivalTerminalName.equals(otherTransport.arrivalTerminalName)    ) { return false; }
            if (!this.routeNo.equals(otherTransport.routeNo)                            ) { return false; }
            if (!this.companyName.equals(otherTransport.companyName)                    ) { return false; }
            if (!this.companyPhone.equals(otherTransport.companyPhone)                  ) { return false; }

            return super.isEqual(otherObject);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String startTime(int dateTimeStyle) {
        if (departureTime != null) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat();
            //dateFormatter.dateStyle = dateStyle
            //dateFormatter.timeStyle = timeStyle
            if (departureTimeZone != null) {
                TimeZone timezone = TimeZone.getTimeZone(departureTimeZone);
                dateFormatter.setTimeZone(timezone);
            }

            return dateFormatter.format(departureTime);
        }
        return null;
    }

    @Override
    public String endTime(int dateTimeStyle) {
        if (arrivalTime != null) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat();
            //dateFormatter.dateStyle = dateStyle
            //dateFormatter.timeStyle = timeStyle
            if (arrivalTimeZone != null) {
                TimeZone timezone = TimeZone.getTimeZone(arrivalTimeZone);
                dateFormatter.setTimeZone(timezone);
            }

            return dateFormatter.format(arrivalTime);
        }
        return null;
    }

    @Override
    public void setNotification() {
        // First delete any existing notifications for this trip element (either one or two)

        /*
        for notification in UIApplication.sharedApplication().scheduledLocalNotifications! as [UILocalNotification] {
            if (notification.userInfo!["TripElementID"] as? Int == id) {
                UIApplication.sharedApplication().cancelLocalNotification(notification)
            }
        }

        // Set notification (if we have a start date)
        if let tripStart = startTime {
            if tense == .future {
                let defaults = NSUserDefaults.standardUserDefaults()
                let departureLeadtime = Int(defaults.floatForKey("dept_notification_leadtime"))
                let legLeadtime = Int(defaults.floatForKey("leg_notification_leadtime"))
                let startTimeText = startTime(dateStyle: .NoStyle, timeStyle: .ShortStyle)
                let now = NSDate()
                let dcf = NSDateComponentsFormatter()
                let genericAlertMessage = NSLocalizedString("%@ departs in %@, at %@", comment: "Some dummy comment")

                dcf.unitsStyle = .Short
                dcf.zeroFormattingBehavior = .DropAll

                var userInfo: [String:NSObject] = ["TripElementID": id]
                if let departureTimeZone = departureTimeZone {
                    userInfo["TimeZone"] = departureTimeZone
                }


                if (departureLeadtime ?? -1) > 0 && (legNo ?? 1) == 1 {
                    var alertTime = tripStart.addMinutes( -departureLeadtime )
                    // If we're already past the warning time, set a notification for right now instead
                    if alertTime.isLessThanDate(now) {
                        alertTime = now
                    }
                    let notification = UILocalNotification()

                    let actualLeadTime = tripStart.timeIntervalSinceDate(alertTime)
                    let leadTimeText = dcf.stringFromTimeInterval(actualLeadTime)
                    //notification.alertBody = "\(title!) departs in \(leadTimeText!), at \(startTimeText!)"
                    notification.alertBody = NSString.localizedStringWithFormat(genericAlertMessage, title!, leadTimeText!, startTimeText!) as String
                    //notification.alertAction = "open" // text that is displayed after "slide to..." on the lock screen - defaults to "slide to view"
                    notification.fireDate = alertTime
                    notification.soundName = UILocalNotificationDefaultSoundName
                    notification.userInfo = userInfo
                    notification.category = "SHiT"
                    UIApplication.sharedApplication().scheduleLocalNotification(notification)
                }
                setLegNotification: if (legLeadtime ?? -1) > 0 {
                    var alertTime = tripStart.addMinutes( -legLeadtime )
                    // If we're already past the warning time, set a notification for right now instead
                    // unless it's the first leg, in which case we already have one from above
                    if alertTime.isLessThanDate(now) {
                        if (legNo ?? 1) == 1 {
                            break setLegNotification
                        } else {
                            alertTime = now
                        }
                    }
                    let notification = UILocalNotification()

                    let actualLeadTime = tripStart.timeIntervalSinceDate(alertTime)
                    let leadTimeText = dcf.stringFromTimeInterval(actualLeadTime)

                    notification.alertBody = NSString.localizedStringWithFormat(genericAlertMessage, title!, leadTimeText!, startTimeText!) as String
                    //"\(title!) departs in \(leadTimeText!), at \(startTimeText!)"
                    //notification.alertAction = "open" // text that is displayed after "slide to..." on the lock screen - defaults to "slide to view"
                    notification.fireDate = alertTime
                    notification.soundName = UILocalNotificationDefaultSoundName
                    notification.userInfo = userInfo
                    notification.category = "SHiT"
                    UIApplication.sharedApplication().scheduleLocalNotification(notification)
                }
            }
        }
        */
    }
}
