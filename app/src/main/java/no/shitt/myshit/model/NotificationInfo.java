package no.shitt.myshit.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import no.shitt.myshit.Constants;
import no.shitt.myshit.helper.ServerDate;

/**
 * Created by persolberg on 2017-05-31.
 */

public class NotificationInfo {
    private static final String TIMEZONE         = "UTC";
    private static final int    LEADTIME_UNKNOWN = -1;

    private Date baseDate;
    private Date notificationDate;
    private int leadTime;

    // MARK: Constructors
    NotificationInfo(JSONObject elementData) {
        String baseDateText = elementData.isNull(Constants.JSON.NTFINFO_BASE_DATE) ? null : elementData.optString(Constants.JSON.NTFINFO_BASE_DATE);
        if (baseDateText != null) {
            baseDate = ServerDate.convertServerDate(baseDateText, TIMEZONE);
        }
        String notificationDateText = elementData.isNull(Constants.JSON.NTFINFO_NOTIFICATION_DATE) ? null : elementData.optString(Constants.JSON.NTFINFO_NOTIFICATION_DATE);
        if (notificationDateText != null) {
            notificationDate = ServerDate.convertServerDate(notificationDateText, TIMEZONE);
        }
        leadTime = elementData.optInt(Constants.JSON.NTFINFO_LEAD_TIME, LEADTIME_UNKNOWN);
    }

    NotificationInfo(Date baseDate, int leadTime) {
        super();

        Calendar now = Calendar.getInstance();
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTime(baseDate);
        alarmTime.add(Calendar.MINUTE, -leadTime);

        this.baseDate = baseDate;
        if (alarmTime.before(now)) {
            now.add(Calendar.SECOND, 5);
            this.notificationDate = now.getTime();
        } else {
            this.notificationDate = alarmTime.getTime();
        }
        this.leadTime = leadTime;
    }


    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject(); // super.toJSON();

        jo.putOpt(Constants.JSON.NTFINFO_BASE_DATE, baseDate);
        jo.putOpt(Constants.JSON.NTFINFO_NOTIFICATION_DATE, ServerDate.convertServerDate(notificationDate, TIMEZONE));
        jo.putOpt(Constants.JSON.NTFINFO_LEAD_TIME, leadTime);

        return jo;
    }

    // MARK: Methods
    boolean needsRefresh(Date baseDate, Date notificationDate, int leadTime) {
        Date now = new Date();

        Calendar newNotificationTime = Calendar.getInstance();
        newNotificationTime.setTime(baseDate);
        newNotificationTime.add(Calendar.MINUTE, -leadTime);
        if (this.notificationDate.after(now)) {
            // Not notfied yet, we may just refresh
            return true;
        } else if (newNotificationTime.getTime().after(now)) {
            // New notification is in the future, probably because event time or lead time changed - refresh
            return true;
        } else if (this.baseDate.compareTo(baseDate) != 0) {
            // Event date changed, notify user about change
            return true;
        }

        return false;
    }

    boolean needsRefresh(NotificationInfo newNotification)  {
        return needsRefresh(newNotification.baseDate, newNotification.notificationDate, newNotification.leadTime);
    }

    Date getNotificationDate() {
        return notificationDate;
    }

    void combine(NotificationInfo with) {
        this.notificationDate = with.notificationDate;
    }
}
