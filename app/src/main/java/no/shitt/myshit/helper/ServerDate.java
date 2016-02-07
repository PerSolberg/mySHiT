package no.shitt.myshit.helper;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import no.shitt.myshit.R;
import no.shitt.myshit.SHiTApplication;

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

    public static String formatInterval(long intervalInMillis) {
        long days = intervalInMillis / DateUtils.DAY_IN_MILLIS;
        long hours = (intervalInMillis % DateUtils.DAY_IN_MILLIS) / DateUtils.HOUR_IN_MILLIS;
        long minutes = (intervalInMillis % DateUtils.HOUR_IN_MILLIS) / DateUtils.MINUTE_IN_MILLIS;

        Context ctx = SHiTApplication.getContext();
        Resources res = ctx.getResources();

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(res.getQuantityString(R.plurals.days, (int) days, (int) days));
        }
        if (hours > 0) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(res.getQuantityString(R.plurals.hours, (int) hours, (int) hours));
        }
        if (minutes > 0 || sb.length() == 0) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(res.getQuantityString(R.plurals.minutes, (int) minutes, (int) minutes));
        }

        return sb.toString();
    }

    public static boolean equal(Date str1, Date str2) {
        return (str1 == null ? str2 == null : str1.equals(str2));
    }
}
