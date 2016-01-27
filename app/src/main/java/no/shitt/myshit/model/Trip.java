package no.shitt.myshit.model;

//import android.text.format.Formatter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import no.shitt.myshit.Constants;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.ServerAPI;
import no.shitt.myshit.helper.ServerAPIListener;
import no.shitt.myshit.helper.ServerDate;

public class Trip /* NSObject, NSCoding */ implements ServerAPIListener {
    public int id;
    public Date startDate;
    public Date endDate;
    public String tripDescription;
    public String code;
    public String name;
    public String type;
    public List<AnnotatedTripElement> elements;

    private final static String iconBaseName = "icon_trip_";

    private static final String URL_PART1 = "http://www.shitt.no/mySHiT/trip/code/";
    private static final String URL_PART2 = "?userName=persolberg@hotmail.com&password=Vertex70&sectioned=0&details=non-historic";

    //static let webServiceRootPath = "trip/code/"

    public Date getStartTime() {
        return startDate;
    }

    public String getStartTimeZone() {
        if (elements != null && elements.size() > 0) {
            return elements.get(0).tripElement.getStartTimeZone();
        }
        return null;
    }

    public Date getEndTime() {
        return endDate;
    }

    public String getTitle() {
        return name;
    }

    public String getDateInfo(Context ctx) {
        DateFormat dateFormatter = android.text.format.DateFormat.getTimeFormat(ctx);
        //dateFormatter.dateStyle = NSDateFormatterStyle.MediumStyle
        //dateFormatter.timeStyle = NSDateFormatterStyle.NoStyle

        return dateFormatter.format(startDate) + " - " + dateFormatter.format(endDate);
    }

    public String getDetailInfo() {
        return tripDescription;
    }

    public Tense getTense() {
        Date startTime = getStartTime();
        if (startTime != null) {
            Date today = new Date();

            // If end time isn't set, assume duration of 1 day
            Date endTime = getEndTime();
            if (endTime == null) {
                Calendar cal = GregorianCalendar.getInstance();
                cal.setTime(startTime);
                cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
                endTime = cal.getTime();
            }

            if (today.after(endTime)) {
                return Tense.PAST;
            } else if (today.before(startTime)) {
                return Tense.FUTURE;
            } else {
                return Tense.PRESENT;
            }
        } else {
            return Tense.FUTURE;
        }
    }

    public int getIconId(Context ctx) {
        String path = iconBaseName;
        String iconName;
        int    iconId;

        switch (getTense()) {
        case PAST:
            path = iconBaseName + "historic_";
            break;
        case PRESENT:
            path = iconBaseName + "active_";
            break;
        case FUTURE:
            path = iconBaseName;
            break;
        }
        iconName = path + type;

        // First try exact match
        iconId = ctx.getResources().getIdentifier(iconName.toLowerCase(), "mipmap", ctx.getPackageName());
        if (iconId != 0) {
            return iconId;
        }

        // Try default variant for trip type
        iconName = iconBaseName + type;
        iconId = ctx.getResources().getIdentifier(iconName.toLowerCase(), "mipmap", ctx.getPackageName());
        if (iconId != 0) {
            return iconId;
        }

        // Try dummy image
        iconName = iconBaseName + "UNKNOWN";
        iconId = ctx.getResources().getIdentifier(iconName.toLowerCase(), "mipmap", ctx.getPackageName());
        if (iconId != 0) {
            return iconId;
        }

        return 0;
    }


    /* Identifiers for keyed archive (iOS only?)
    struct PropertyKey {
        static let idKey = "id"
        static let startDateKey = "startDate"
        static let endDateKey = "endDate"
        static let tripDescriptionKey = "description"
        static let codeKey = "code"
        static let nameKey = "name"
        static let typeKey = "type"
        static let elementsKey = "elements"
    }
    */

    // MARK: Factory
    public static Trip createFromDictionary( JSONObject elementData ) {
        //let tripType = elementData["type"] as? String ?? ""

        return new Trip(elementData);
    }

    /* Encode for keyed archive (iOS only?)
    // MARK: NSCoding
    func encodeWithCoder(aCoder: NSCoder) {
        aCoder.encodeInteger(id, forKey: PropertyKey.idKey)
        aCoder.encodeObject(startDate, forKey: PropertyKey.startDateKey)
        aCoder.encodeObject(endDate, forKey: PropertyKey.endDateKey)
        aCoder.encodeObject(tripDescription, forKey: PropertyKey.tripDescriptionKey)
        aCoder.encodeObject(code, forKey: PropertyKey.codeKey)
        aCoder.encodeObject(name, forKey: PropertyKey.nameKey)
        aCoder.encodeObject(type, forKey: PropertyKey.typeKey)
        aCoder.encodeObject(elements, forKey: PropertyKey.elementsKey)
    }
    */

    // MARK: Constructors
    /* Decode from keyed archive (iOS only?)

    required init?(coder aDecoder: NSCoder) {
    super.init()
    // NB: use conditional cast (as?) for any optional properties
    id  = aDecoder.decodeIntegerForKey(PropertyKey.idKey)
    startDate  = aDecoder.decodeObjectForKey(PropertyKey.startDateKey) as? NSDate
    endDate  = aDecoder.decodeObjectForKey(PropertyKey.endDateKey) as? NSDate
    tripDescription  = aDecoder.decodeObjectForKey(PropertyKey.tripDescriptionKey) as? String
    code  = aDecoder.decodeObjectForKey(PropertyKey.codeKey) as? String
    name  = aDecoder.decodeObjectForKey(PropertyKey.nameKey) as? String
    type  = aDecoder.decodeObjectForKey(PropertyKey.typeKey) as? String
    elements  = aDecoder.decodeObjectForKey(PropertyKey.elementsKey) as? [AnnotatedTripElement]

    setNotification()
    }
    */

    Trip(JSONObject elementData) {
        super();
        id = elementData.optInt(Constants.JSON.TRIP_ID, -1);
        startDate = ServerDate.convertServerDate(elementData.optString(Constants.JSON.TRIP_START_DATE), null);
        endDate = ServerDate.convertServerDate(elementData.optString(Constants.JSON.TRIP_END_DATE), null);
        tripDescription = elementData.optString(Constants.JSON.TRIP_DESCRIPTION);
        code = elementData.optString(Constants.JSON.TRIP_CODE);
        name = elementData.optString(Constants.JSON.TRIP_NAME);
        type = elementData.optString(Constants.JSON.TRIP_TYPE);
        JSONArray tripElements = elementData.optJSONArray(Constants.JSON.TRIP_ELEMENTS);
        if (tripElements != null) {
            elements = new ArrayList<>();
            for (int i = 0; i < tripElements.length(); i++) {
                JSONObject srvElement = tripElements.optJSONObject(i);
                TripElement tripElement = TripElement.createFromDictionary(srvElement);
                elements.add( new AnnotatedTripElement(tripElement));
            }
        }

        setNotification();
    }

    // MARK: Methods
    public boolean isEqual(Object otherObject) {
        //print("Comparing objects: self.class = \(object_getClassName(self)), object.class = \(object_getClassName(object!))")
        //print("Comparing objects: self.class = \(_stdlib_getDemangledTypeName(self)), object.class = \(_stdlib_getDemangledTypeName(object!))")
        if (this.getClass() != otherObject.getClass()) {
            return false;
        }
        try {
            Trip otherTrip = (Trip) otherObject;
            if (this.id              != otherTrip.id                       ) { return false; }
            if (!this.startDate.equals(otherTrip.startDate)                ) { return false; }
            if (!this.endDate.equals(otherTrip.endDate)                    ) { return false; }
            if (!this.tripDescription.equals(otherTrip.tripDescription)    ) { return false; }
            if (!this.code.equals(otherTrip.code)                          ) { return false; }
            if (!this.name.equals(otherTrip.name)                          ) { return false; }
            if (!this.type.equals(otherTrip.type)                          ) { return false; }
            if (elements != null) {
                for (int i = 0; i < elements.size(); i++) {
                    if (elements.get(i).modified == ChangeState.NEW || elements.get(i).modified == ChangeState.CHANGED) {
                        return false;
                    }
                }
            }
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public void compareTripElements(Trip otherTrip) {
        if (elements == null || otherTrip.elements == null) {
            return;
        }

        // Determine changes
        for (int i = 0; i < elements.size(); i++) {
            AnnotatedTripElement e1 = elements.get(i);
            AnnotatedTripElement e2 = null;
            for (int j = 0; j < otherTrip.elements.size(); j++) {
                if (otherTrip.elements.get(j).tripElement.id == e1.tripElement.id) {
                    e2 = otherTrip.elements.get(j);
                    break;
                }
            }
            if (e2 == null) {
                e1.modified = ChangeState.NEW;
            } else if (!e1.tripElement.isEqual(e2.tripElement)) {
                e1.modified = ChangeState.CHANGED;
            }
        }
    }

    public String startTime(int dateTimeStyle) {
        if (getStartTime() != null) {
            Formatter f = new Formatter(new StringBuilder(50), Locale.getDefault());
            long time = getStartTime().getTime();
            return android.text.format.DateUtils.formatDateRange(null, f, time, time, dateTimeStyle, getStartTimeZone()).toString();
        }
        return null;
    }


    public int elementCount() {
        if (elements == null)
            return 0;
        else
            return elements.size();
    }

    public AnnotatedTripElement elementById(int elementId) {
        for (int i = 0; i < elements.size(); i++) {
            if (elementId == elements.get(i).tripElement.id) {
                return elements.get(i);
            }
        }
        return null;
    }

    public AnnotatedTripElement elementByPosition(int position) {
        if (position >= 0 && position < elements.size())
            return elements.get(position);
        else
            return null;
    }


    public void setNotification() {
        // First delete any existing notifications for this trip
        // TO DO...
        return;
        /*
        for notification in UIApplication.sharedApplication().scheduledLocalNotifications! as [UILocalNotification] {
            if (notification.userInfo!["TripID"] as? Int == id) {
                UIApplication.sharedApplication().cancelLocalNotification(notification)
                // there should be a maximum of one match on TripID
                break
            }
        }

        // Set notification (if we have a start date)
        if let tripStart = startTime {
            if tense == .future {
                let defaults = NSUserDefaults.standardUserDefaults()
                let tripLeadtime = Int(defaults.floatForKey("trip_notification_leadtime"))
                let startTimeText = startTime(dateStyle: .ShortStyle, timeStyle: .ShortStyle)
                let now = NSDate()
                let dcf = NSDateComponentsFormatter()
                let genericAlertMessage = NSLocalizedString("SHiT trip '%@' starts in %@ (%@)", comment: "Some dummy comment")

                dcf.unitsStyle = .Short
                dcf.zeroFormattingBehavior = .DropAll

                var userInfo: [String:NSObject] = ["TripID": id]
                if let startTimeZone = startTimeZone {
                    userInfo["TimeZone"] = startTimeZone
                }

                if (tripLeadtime ?? -1) > 0 {
                    var alertTime = tripStart.addHours( -tripLeadtime )
                    // If we're already past the warning time, set a notification for right now instead
                    if alertTime.isLessThanDate(now) {
                        alertTime = now
                    }
                    let notification = UILocalNotification()

                    let actualLeadTime = tripStart.timeIntervalSinceDate(alertTime)
                    let leadTimeText = dcf.stringFromTimeInterval(actualLeadTime)
                    notification.alertBody = NSString.localizedStringWithFormat(genericAlertMessage, title!, leadTimeText!, startTimeText!) as String
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

    public void onRemoteCallComplete(JSONObject response) {
        Log.d("Trip", "Trip details retrieved");
        int count = response.optInt(Constants.JSON.QUERY_COUNT, -1);
        if (count != 1) {
            Log.e("Trip", "loadDetails returned " + Integer.toString(count) + " elements, expected 1.");
        } else {
            JSONArray results = response.optJSONArray(Constants.JSON.QUERY_RESULTS);
            JSONObject serverData = null;
            Trip newTrip = null;
            if (results != null && results.length() == 1) {
                serverData = results.optJSONObject(0);
            }
            if (serverData != null) {
                newTrip = Trip.createFromDictionary(serverData);
            }
            if (newTrip != null) {
                Log.d("Trip", "Comparing new trip details to existing");
                newTrip.compareTripElements(this);
                Log.d("Trip", "Updating trip");
                this.id              = newTrip.id;
                this.startDate       = newTrip.startDate;
                this.endDate         = newTrip.endDate;
                this.tripDescription = newTrip.tripDescription;
                this.code            = newTrip.code;
                this.name            = newTrip.name;
                this.type            = newTrip.type;
                this.elements        = newTrip.elements;
            }
        }

        Log.d("Trip", "Sending notification");
        Intent intent = new Intent("tripDetailsLoaded");
        //intent.putExtra("message", "SHiT trips loaded");
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }

    public void onRemoteCallFailed() {
        Log.d("Trip", "Server call failed");
        Intent intent = new Intent("tripDetailsLoaded");
        //intent.putExtra("message", "SHiT trips loaded");
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }


    public void loadDetails() {
        // TO DO...
        /*
        UserCredentials userCred = User.sharedUser.getCredentials();

        assert( userCred.name != nil );
        assert( userCred.password != nil );
        assert( userCred.urlsafePassword != nil );

        //Set the parameters for the RSTransaction object
        rsTransGetTripList.path = self.dynamicType.webServiceRootPath + code!
                rsTransGetTripList.parameters = [ "userName":userCred.name!,
                "password":userCred.urlsafePassword! ]
        */

        String url = URL_PART1 + code + URL_PART2;
        new ServerAPI(this).execute(url);
    }
}
