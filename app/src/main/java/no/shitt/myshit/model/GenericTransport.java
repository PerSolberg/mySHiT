package no.shitt.myshit.model;

import org.json.JSONException;
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
        return departureLocation;
    }
    @Override
    public String getEndInfo() {
        return arrivalLocation;
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


    GenericTransport(JSONObject elementData) {
        super(elementData);
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
