package no.shitt.myshit.model;

import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.HashSet;
import java.util.Set;

import no.shitt.myshit.Constants;
import no.shitt.myshit.FlightActivity;
import no.shitt.myshit.FlightPopupActivity;
import no.shitt.myshit.SHiTApplication;

public class Flight extends ScheduledTransport {
    private final static String LOG_TAG = Flight.class.getSimpleName();
    // MARK: Properties
    public String airlineCode;

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
            Set<String> excludeRefTypes = new HashSet<>();
            excludeRefTypes.add(REFTYPE_ELECTRONIC_TKT);
            return getReferences(", ", false, excludeRefTypes);
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
    }


    //
    // MARK: Methods
    //
    @Override
    boolean update(JSONObject elementData) {
        changed = super.update(elementData);

        airlineCode = updateField(airlineCode, elementData, Constants.JSON.ELEM_COMPANY_CODE);

        if (changed && ( this.getClass() == Flight.class) ) {
            setNotification();
        }
        return changed;
    }


    @Override
    public Intent getActivityIntent(ActivityType activityType) {
        Intent i;
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
