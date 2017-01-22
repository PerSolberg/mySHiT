package no.shitt.myshit;

public class Constants {
    public static final boolean DEVELOPER_MODE = false;  // Change to false for release version!

    public static final String CRED_U_FILE = "SHiT-U.dat";
    public static final String CRED_P_FILE = "SHiT-P.dat";
    public static final String LOCAL_ARCHIVE_FILE = "SHiT-trips.json";

    public static final String URL_ENCODE_CHARSET = "utf-8";

    public class Notification {
        public static final String LOGON_FAILED           = "logonFailed";
        public static final String LOGON_UNAUTHORISED     = "logonUnauthorised";
        public static final String LOGON_SUCCEEDED        = "logonSuccessful";

        public static final String TRIPS_LOADED           = "tripsLoaded";
        public static final String TRIP_DETAILS_LOADED    = "tripDetailsLoaded";
        public static final String COMMUNICATION_FAILED   = "communicationError";
    }

    public class PushNotification {
        //private static final String TOPIC_ROOT             = "/topics";
        public  static final String TOPIC_ROOT_USER        = "U-";
        public  static final String TOPIC_ROOT_ITINERARY   = "I-";
        public  static final String TOPIC_ROOT_TRIP        = "T-";
        public  static final String TOPIC_GLOBAL           = "GLOBAL";
    }

    public class IntentExtra {
        public static final String TRIP_CODE            = "trip_code";
        public static final String ELEMENT_ID           = "element_id";
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
        public static final String TRIP_ELEMENTS           = "elements";

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

    }
}
