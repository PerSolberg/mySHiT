package no.shitt.myshit.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Map;

import no.shitt.myshit.Constants;
import no.shitt.myshit.R;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.StringUtil;

public class ScheduledTransport extends GenericTransport {
    // MARK: Properties
    // No additional properties needed

    // MARK: Constructors
    ScheduledTransport(int tripId, String tripCode, JSONObject elementData) {
        super(tripId, tripCode, elementData);
    }


    @Override
    public String getTitle() {
        return StringUtil.stringWithDefault(companyName, "XX") + " "
                + StringUtil.stringWithDefault(routeNo, "***") + ": "
                + StringUtil.stringWithDefault(departureLocation, "<Departure>") + " - "
                + StringUtil.stringWithDefault(arrivalLocation, "<Arrival>");
    }

    @Override
    public String getStartInfo() {
        StringBuilder sb = new StringBuilder();
        StringUtil.appendWithLeadingSeparator(sb, startTime(null, DateFormat.SHORT), "", false);
        StringUtil.appendWithLeadingSeparator(sb, departureStop, ": ", false);
        StringUtil.appendWithLeadingSeparator(sb, departureLocation, ", ", false);
        return sb.toString();
    }

    @Override
    public String getEndInfo() {
        StringBuilder sb = new StringBuilder();
        StringUtil.appendWithLeadingSeparator(sb, endTime(null, DateFormat.SHORT), "", false);
        StringUtil.appendWithLeadingSeparator(sb, arrivalStop, ": ", false);
        StringUtil.appendWithLeadingSeparator(sb, arrivalLocation, ", ", false);
        return sb.toString();
    }

    @Override
    public String getDetailInfo() {
        if (references != null) {
            String refList = "";
            for (int i = 0; i < references.size(); i++) {
                Map<String,String> ref = references.get(i);
                if (!"ETKT".equals(ref.get("type"))) {
                    refList = refList + (refList.equals("") ? "" : ", ") + references.get(i).get("refNo");
                }
            }
            return refList;
        }
        return null;
    }


    // MARK: Methods
    public void setNotification() {
        // First delete any existing notifications for this trip element (either one or two)
        //cancelNotifications();

        // Set notification(s) (if we have a start date)
        if (getTense() == Tense.FUTURE) {
            Context ctx = SHiTApplication.getContext();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);

            int leadTimeDepartureMinutes = SHiTApplication.getPreferenceInt(Constants.Setting.ALERT_LEAD_TIME_DEPARTURE, LEAD_TIME_MISSING);
            /* try {
                String leadTimeDeparture = sharedPref.getString(Constants.Setting.ALERT_LEAD_TIME_DEPARTURE, "");
                leadTimeDepartureMinutes = Integer.valueOf(leadTimeDeparture);
            }
            catch (Exception e) {
                leadTimeDepartureMinutes = -1;
            } */

            int leadTimeConnectionMinutes = SHiTApplication.getPreferenceInt(Constants.Setting.ALERT_LEAD_TIME_CONNECTION, LEAD_TIME_MISSING);
            /* try {
                String leadtimeConnection = sharedPref.getString(Constants.Setting.ALERT_LEAD_TIME_CONNECTION, "");
                leadTimeConnectionMinutes = Integer.valueOf(leadtimeConnection);
            }
            catch (Exception e) {
                leadTimeConnectionMinutes = -1;
            } */

            if (leadTimeDepartureMinutes != LEAD_TIME_MISSING && legNo == 1) {
                setNotification(Constants.Setting.ALERT_LEAD_TIME_DEPARTURE, leadTimeDepartureMinutes, R.string.alert_msg_travel, null);
            }
            if (leadTimeConnectionMinutes != LEAD_TIME_MISSING) {
                setNotification(Constants.Setting.ALERT_LEAD_TIME_CONNECTION, leadTimeConnectionMinutes, R.string.alert_msg_travel, null);
            }
        }
    }

}


