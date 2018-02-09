package no.shitt.myshit.model;

import android.content.Intent;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Map;

import no.shitt.myshit.Constants;
import no.shitt.myshit.R;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.ScheduledTransportActivity;
import no.shitt.myshit.ScheduledTransportPopupActivity;
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
            int leadTimeDepartureMinutes = SHiTApplication.getPreferenceInt(Constants.Setting.ALERT_LEAD_TIME_DEPARTURE, LEAD_TIME_MISSING);
            int leadTimeConnectionMinutes = SHiTApplication.getPreferenceInt(Constants.Setting.ALERT_LEAD_TIME_CONNECTION, LEAD_TIME_MISSING);

            if (leadTimeDepartureMinutes != LEAD_TIME_MISSING && legNo == 1) {
                setNotification(Constants.Setting.ALERT_LEAD_TIME_DEPARTURE
                        , leadTimeDepartureMinutes
                        , R.string.alert_msg_travel
                        , getNotificationClickAction()
                        , null);
            }
            if (leadTimeConnectionMinutes != LEAD_TIME_MISSING) {
                setNotification(Constants.Setting.ALERT_LEAD_TIME_CONNECTION
                        , leadTimeConnectionMinutes
                        , R.string.alert_msg_travel
                        , getNotificationClickAction()
                        , null);
            }
        }
    }


    @Override
    public Intent getActivityIntent(ActivityType activityType) {
        Intent i = null;
        switch (activityType) {
            case REGULAR:
                i = new Intent(SHiTApplication.getContext(), ScheduledTransportActivity.class);
                break;

            case POPUP:
                i = new Intent(SHiTApplication.getContext(), ScheduledTransportPopupActivity.class);
                break;

            default:
                return null;
        }

        i.putExtra(Constants.IntentExtra.TRIP_CODE, tripCode);
        i.putExtra(Constants.IntentExtra.ELEMENT_ID, String.valueOf(id));

        return i;
    }

    @Override
    protected String getNotificationClickAction() {
        return Constants.PushNotificationActions.SCHEDULED_TRANSPORT_CLICK;
    }
}


