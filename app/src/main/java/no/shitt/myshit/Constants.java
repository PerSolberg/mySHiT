package no.shitt.myshit;

import java.util.regex.Pattern;

public class Constants {
    public static final boolean DEVELOPER_MODE = false;  // Change to false for release version!

    public static final String CRED_U_FILE = "SHiT-U.dat";
    public static final String CRED_P_FILE = "SHiT-P.dat";
    public static final String LOCAL_ARCHIVE_FILE = "SHiT-trips.json";

    public static final String URL_ENCODE_CHARSET = "utf-8";
    public static final String DEVICE_TYPE        = "Android";

    public static final Pattern selectAll = Pattern.compile(".*", Pattern.DOTALL);
    public static final Pattern selectAllButFirstLine = Pattern.compile("(?<=[\\r\\n]).*", Pattern.DOTALL | Pattern.MULTILINE);

    public class NotificationChannel {
        public static final String CHAT      = "CHAT";
        public static final String ALARM     = "ALARM";
        public static final String UPDATE    = "UPDATE";
    }

    public class NotificationTag {
        public static final String CHAT           = "Chat";
        public static final String TRIP           = "Trip";
        public static final String TRIP_ELEMENT   = "TripElement";
    }

    public class Notification {
        public static final String LOGON_FAILED           = "logonFailed";
        public static final String LOGON_UNAUTHORISED     = "logonUnauthorised";
        public static final String LOGON_SUCCEEDED        = "logonSuccessful";

        public static final String TRIPS_LOADED           = "tripsLoaded";
        public static final String TRIP_DETAILS_LOADED    = "tripDetailsLoaded";
        public static final String CHAT_UPDATED           = "chatUpdated";
        public static final String COMMUNICATION_FAILED   = "communicationError";
    }

    public class NotificationUserInfo {
        public static final String TRIP_ID = "TripID";
        public static final String TRIP_ELEMENT_ID = "TripElementID";
        public static final String TIMEZONE = "TimeZone";
        public static final String LEAD_TIME_TYPE = "leadTimeType";
    }

    public class PushNotification {
        //private static final String TOPIC_ROOT             = "/topics";
        public static final String TOPIC_ROOT_USER        = "U-";
        public static final String TOPIC_ROOT_ITINERARY   = "I-";
        public static final String TOPIC_ROOT_TRIP        = "T-";
        public static final String TOPIC_GLOBAL           = "GLOBAL";
    }

    public class PushNotificationKeys {
        public static final String CHANGE_TYPE            = "changeType";
        public static final String CHANGE_OPERATION       = "changeOperation";
        public static final String TRIP_ID                = "tripId";
        public static final String TRIP_NAME              = "tripName";
        public static final String LAST_SEEN_INFO         = "lastSeenInfo";
        public static final String LOC_ARGS               = "loc-args";
        public static final String LOC_KEY                = "loc-key";
        public static final String TITLE_LOC_ARGS         = "title-loc-args";
        public static final String TITLE_LOC_KEY          = "title-loc-key";
        public static final String FROM_USER_ID           = "fromUserId";
        public static final String MESSAGE_ID             = "id";
    }

    public class PushNotificationData {
        public static final String TYPE_CHAT_MESSAGE      = "CHATMESSAGE";
        public static final String OP_INSERT              = "INSERT";
        public static final String OP_DELETE              = "DELETE";
        public static final String OP_UPDATE              = "UPDATE";
    }

    public class PushNotificationActions {
        public static final String EVENT_CLICK                = "no.shitt.myshit.EVENT";
        public static final String TRIP_CLICK                 = "no.shitt.myshit.TRIP";
        public static final String HOTEL_CLICK                = "no.shitt.myshit.ACCOM.HOTEL";
        public static final String FLIGHT_CLICK               = "no.shitt.myshit.TRANSPORT.FLIGHT";
        public static final String PRIVATE_TRANSPORT_CLICK    = "no.shitt.myshit.TRANSPORT.PRIVATE";
        public static final String SCHEDULED_TRANSPORT_CLICK  = "no.shitt.myshit.TRANSPORT.SCHEDULED";

        /* Server-based click events */
        public static final String CHATMSG_CLICK              = "NTF.INSERT.CHATMESSAGE";
        public static final String CHATMSG_REPLY              = "NTF.ACTION.REPLY";
        public static final String CHATMSG_IGNORE             = "NTF.ACTION.IGNORE";
    }

    public class Setting {
        public static final String ALERT_LEAD_TIME_DEPARTURE     = "pref_alertLeadTime_departure";
        public static final String ALERT_LEAD_TIME_CONNECTION    = "pref_alertLeadTime_connection";
        public static final String ALERT_LEAD_TIME_EVENT         = "pref_alertLeadTime_event";
        public static final String ALERT_LEAD_TIME_TRIP          = "pref_alertLeadTime_trip";
    }

    public class Intent {
        public static final String CHATMSG_REPLY        = "no.shitt.myshit.CHATMSG_REPLY";
        public static final String CHATMSG_IGNORE       = "no.shitt.myshit.CHATMSG_IGNORE";
    }

    public class IntentExtra {
        public static final String ELEMENT_ID           = "element_id";
        public static final String MESSAGE              = "message";
        public static final String MESSAGE_ID           = "message_id";
        public static final String NOTIFICATION_TAG     = "ntf_tag";
        public static final String TITLE                = "title";
        public static final String TRIP_CODE            = "trip_code";
        public static final String TRIP_ID              = "trip_id";
//        public static final String KEY_TRIP_CODE  = "tripCode";
//        public static final String KEY_ELEMENT_ID = "tripElement";
//        public static final String KEY_TITLE      = "title";
//        public static final String KEY_MESSAGE    = "msg";
    }

    public class UpcomingPreference {
        public static final String NEXT_ONLY               = "N";
        public static final String WITHIN_7_DAYS           = "7";
        public static final String WITHIN_30_DAYS          = "30";
        public static final String NEXT_AND_WITHIN_7_DAYS  = "N7";
        public static final String NEXT_AND_WITHIN_30_DAYS = "N30";
    }

    public class JSON {
        public static final String QUERY_COUNT             = "count";
        public static final String QUERY_DETAILS           = "details";
        public static final String QUERY_RESULTS           = "results";

        public static final String ANNTRIP_TRIP            = "trip";
        public static final String ANNTRIP_MODIFIED        = "modified";
        public static final String ANNELEMENT_ELEMENT      = "element";
        public static final String ANNELEMENT_MODIFIED     = "modified";

        public static final String TRIP_ID                 = "id";
        public static final String TRIP_ITINERARY_ID       = "itineraryId";
        public static final String TRIP_START_DATE         = "startDate";
        public static final String TRIP_END_DATE           = "endDate";
        public static final String TRIP_DESCRIPTION        = "description";
        public static final String TRIP_CODE               = "code";
        public static final String TRIP_NAME               = "name";
        public static final String TRIP_SECTION            = "section";
        public static final String TRIP_TYPE               = "type";
        public static final String TRIP_ELEMENT_COUNT      = "elementCount";
        public static final String TRIP_ELEMENTS           = "elements";
        public static final String TRIP_NOTIFICATIONS      = "notifications";
        public static final String TRIP_CHAT               = "chatThread";

        public static final String ELEM_TYPE               = "type";
        public static final String ELEM_SUB_TYPE           = "subType";
        public static final String ELEM_ID                 = "id";
        public static final String ELEM_REFERENCES         = "references";
        public static final String ELEM_LEG_NO             = "legNo";
        public static final String ELEM_SEGMENT_ID         = "segmentId";
        public static final String ELEM_SEGMENT_CODE       = "segmentCode";
        public static final String ELEM_DEP_TIME           = "departureTime";
        public static final String ELEM_DEP_TZ             = "departureTimezone";
        public static final String ELEM_DEP_LOCATION       = "departureLocation";
        public static final String ELEM_DEP_STOP           = "departureStop";
        public static final String ELEM_DEP_ADDR           = "departureAddress";
        public static final String ELEM_DEP_TERMINAL_CODE  = "departureTerminalCode";
        public static final String ELEM_DEP_TERMINAL_NAME  = "departureTerminalName";
        public static final String ELEM_DEP_COORDINATES    = "departureCoordinates";
        public static final String ELEM_ARR_TIME           = "arrivalTime";
        public static final String ELEM_ARR_TZ             = "arrivalTimezone";
        public static final String ELEM_ARR_LOCATION       = "arrivalLocation";
        public static final String ELEM_ARR_STOP           = "arrivalStop";
        public static final String ELEM_ARR_ADDR           = "arrivalAddress";
        public static final String ELEM_ARR_TERMINAL_CODE  = "arrivalTerminalCode";
        public static final String ELEM_ARR_TERMINAL_NAME  = "arrivalTerminalName";
        public static final String ELEM_ARR_COORDINATES    = "arrivalCoordinates";
        public static final String ELEM_ROUTE_NO           = "routeNo";
        public static final String ELEM_COMPANY            = "company";
        public static final String ELEM_COMPANY_CODE       = "companyCode";
        public static final String ELEM_PHONE              = "companyPhone";

        public static final String ELEM_HOTEL_NAME          = "hotelName";
        public static final String ELEM_HOTEL_CHECK_IN      = "checkIn";
        public static final String ELEM_HOTEL_CHECK_OUT     = "checkOut";
        public static final String ELEM_HOTEL_ADDR          = "address";
        public static final String ELEM_HOTEL_POST_CODE     = "postCode";
        public static final String ELEM_HOTEL_CITY          = "city";
        public static final String ELEM_HOTEL_PHONE         = "phone";
        public static final String ELEM_HOTEL_TRANSFER_INFO = "transferInfo";

        public static final String ELEM_EVENT_VENUE_NAME       = "venueName";
        public static final String ELEM_EVENT_START_TIME       = "startTime";
        public static final String ELEM_EVENT_TIMEZONE         = "timezone";
        public static final String ELEM_EVENT_TRAVEL_TIME      = "travelTime";
        public static final String ELEM_EVENT_VENUE_ADDR       = "venueAddress";
        public static final String ELEM_EVENT_VENUE_CITY       = "venueCity";
        public static final String ELEM_EVENT_VENUE_POST_CODE  = "venuePostCode";
        public static final String ELEM_EVENT_VENUE_PHONE      = "venuePhone";
        public static final String ELEM_EVENT_ACCESS_INFO      = "accessInfo";

        public static final String CHAT_MESSAGES               = "messages";

        public static final String CHATMSG_ID                  = "id";
        public static final String CHATMSG_TRIP_ID             = "tripId";
        public static final String CHATMSG_USER_ID             = "userId";
        public static final String CHATMSG_USER_NAME           = "userName";
        public static final String CHATMSG_USER_INIT           = "userInitials";
        public static final String CHATMSG_DEVICE_TYPE         = "deviceType";
        public static final String CHATMSG_DEVICE_ID           = "deviceId";
        public static final String CHATMSG_LOCAL_ID            = "localId";
        public static final String CHATMSG_MESSAGE_TEXT        = "message";
        public static final String CHATMSG_STORED_TS           = "storedTS";
        public static final String CHATMSG_CREATED_TS          = "createdTS";

        public static final String CHATTHREAD_TRIP_ID              = "tripId";
        public static final String CHATTHREAD_LAST_DISPLAYED_ID    = "lastDisplayedId";
        public static final String CHATTHREAD_LAST_SEEN_USER_LOCAL = "lastSeenByUserLocal";
        public static final String CHATTHREAD_LAST_SEEN_USER_SRV   = "lastSeenByUserServer";
        public static final String CHATTHREAD_LAST_SEEN_OTHERS     = "lastSeenByOthers";
        public static final String CHATTHREAD_MSG_VERSION          = "messageVersion";
        //public static final String CHATTHREAD_LAST_DISPLAYED_POS   = "lastDisplayedPosition";
        public static final String CHATTHREAD_LAST_SEEN_VERSION    = "lastSeenVersion";
        public static final String CHATTHREAD_MESSAGES             = "messages";
        public static final String CHATTHREAD_LAST_SEEN_BY_ME      = "lastSeenByMe";

        public static final String NTFINFO_BASE_DATE           = "baseDate";
        public static final String NTFINFO_NOTIFICATION_DATE   = "notificationDate";
        public static final String NTFINFO_LEAD_TIME           = "leadTime";

    }
}
