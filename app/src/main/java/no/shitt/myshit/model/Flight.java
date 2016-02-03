package no.shitt.myshit.model;

import android.content.Context;
import android.text.format.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Map;

import no.shitt.myshit.Constants;
import no.shitt.myshit.SHiTApplication;

public class Flight extends GenericTransport {
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
        Context ctx = SHiTApplication.getContext();
        DateFormat dateFormatter = android.text.format.DateFormat.getTimeFormat(ctx);
        //String timeInfo = startTime(DateUtils.FORMAT_SHOW_TIME);
        String timeInfo = dateFormatter.format(departureTime);
        String airportName = (departureStop != null ? departureStop : "<Departure Airport>");
        String terminalInfo = (departureTerminalCode != null && !departureTerminalCode.isEmpty() ? " [" + departureTerminalCode + "]" : "");
        return (timeInfo != null ? timeInfo + ": " : "") + airportName + terminalInfo;
    }

    @Override
    public String getEndInfo() {
        Context ctx = SHiTApplication.getContext();
        DateFormat dateFormatter = android.text.format.DateFormat.getTimeFormat(ctx);
        //String timeInfo = endTime(DateUtils.FORMAT_SHOW_TIME);
        String timeInfo = dateFormatter.format(arrivalTime);
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

    Flight(JSONObject elementData) {
        super(elementData);
        airlineCode = elementData.isNull(Constants.JSON.ELEM_COMPANY_CODE) ? null : elementData.optString(Constants.JSON.ELEM_COMPANY_CODE);
        setNotification();
    }

    // MARK: Methods
    @Override
    public boolean isEqual(Object otherObject) {
        if (this.getClass() != otherObject.getClass()) {
            return false;
        }
        try {
            Flight otherFlight = (Flight) otherObject;
            if (this.airlineCode.equals(otherFlight.airlineCode))      { return false; }

            return super.isEqual(otherObject);
        } catch (Exception e) {
            return false;
        }
    }
}
