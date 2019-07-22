package no.shitt.myshit.model;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Map;

import no.shitt.myshit.Constants;
import no.shitt.myshit.FlightActivity;
import no.shitt.myshit.FlightPopupActivity;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.StringUtil;

public class Flight extends ScheduledTransport {
    // MARK: Properties
    public final String airlineCode;

    @Override
    public String getTitle() {
        return (airlineCode != null ? airlineCode : "XX" ) + " " + (routeNo != null ? routeNo : "***") + ": "
                + (departureLocation != null ? departureLocation : "<Departure>") + " - "
                + (arrivalLocation != null ? arrivalLocation : "<Arrival>");
    }

    @Override
    public String getStartInfo() {
        String timeInfo = startTime(null, DateFormat.SHORT);
        String airportName = (departureStop != null ? departureStop : "<Departure Airport>");
        String terminalInfo = (departureTerminalCode != null && !departureTerminalCode.isEmpty() ? " [" + departureTerminalCode + "]" : "");
        return (timeInfo != null ? timeInfo + ": " : "") + airportName + terminalInfo;
    }

    @Override
    public String getEndInfo() {
        String timeInfo = endTime(null, DateFormat.SHORT);
        String airportName = (arrivalStop != null ? arrivalStop : "<Arrival-Airport>");
        String terminalInfo = (arrivalTerminalCode != null && !arrivalTerminalCode.isEmpty() ? " [" + arrivalTerminalCode + "]" : "");
        return (timeInfo != null ? timeInfo + ": " : "") + airportName + terminalInfo;
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

    // Encode to JSON for saving to file
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jo = super.toJSON();

        jo.putOpt(Constants.JSON.ELEM_COMPANY_CODE, airlineCode);

        return jo;
    }

    Flight(int tripId, String tripCode, JSONObject elementData) {
        super(tripId, tripCode, elementData);
        airlineCode = elementData.isNull(Constants.JSON.ELEM_COMPANY_CODE) ? null : elementData.optString(Constants.JSON.ELEM_COMPANY_CODE);
        //setNotification();
    }

    // MARK: Methods
    @Override
    public boolean isEqual(Object otherObject) {
        if (this.getClass() != otherObject.getClass()) {
            return false;
        }
        try {
            Flight otherFlight = (Flight) otherObject;
            if (!StringUtil.equal(this.airlineCode, otherFlight.airlineCode))      { return false; }

            return super.isEqual(otherObject);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Intent getActivityIntent(ActivityType activityType) {
        Intent i = null;
        switch (activityType) {
            case REGULAR:
                i = new Intent(SHiTApplication.getContext(), FlightActivity.class);
                break;

            case POPUP:
                i = new Intent(SHiTApplication.getContext(), FlightPopupActivity.class);
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
        return Constants.PushNotificationActions.FLIGHT_CLICK;
    }
}
