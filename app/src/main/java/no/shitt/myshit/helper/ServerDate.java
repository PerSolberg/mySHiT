package no.shitt.myshit.helper;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class ServerDate {
    //iOS feature
    //typealias DayHourMinute = (days:Int, hours:Int, minutes:Int)

    static final Map<String,String> dateFormats;
    static final SimpleDateFormat dateFormatter = new SimpleDateFormat();

    static {
        Map<String, String> initMap = new HashMap<>();
        initMap.put("^[0-9]{4}-[0-9]{2}-[0-9]{2}$", "yyyy-MM-dd");
        initMap.put("^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$", "yyyy-MM-dd HH:mm:ss");
        initMap.put("^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}$", "yyyy-MM-dd'T'HH:mm:ss");
        dateFormats = Collections.unmodifiableMap(initMap);
    }

    static String findFormatString (String serverDateString) {
        for (Map.Entry<String,String> entry : dateFormats.entrySet()) {
            if (Pattern.matches(entry.getKey(), serverDateString)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static Date convertServerDate (String serverDateString, String timeZoneName) {
        String formatString = findFormatString(serverDateString);
        if (formatString != null) {
            dateFormatter.applyPattern(formatString);
            if (timeZoneName != null) {
                TimeZone timezone = TimeZone.getTimeZone(timeZoneName);
                dateFormatter.setTimeZone(timezone);
            }
            //let locale = NSLocale(localeIdentifier: "en_US_POSIX");
            //dateFormatter.dateFormat = formatString;
            //dateFormatter.locale = locale;
            try {
                return dateFormatter.parse(serverDateString);
            }
            catch (java.text.ParseException pe) {
                return null;
            }
        } else {
            return null;
        }
    }
}
