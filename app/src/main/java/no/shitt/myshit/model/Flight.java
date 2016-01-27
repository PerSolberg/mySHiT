package no.shitt.myshit.model;

import android.content.Context;
import android.text.format.DateUtils;

import org.json.JSONObject;

import java.util.Map;

import no.shitt.myshit.Constants;

public class Flight extends GenericTransport {
    // MARK: Properties
    String airlineCode;

    /* Identifiers for keyed archive (iOS only?)
    struct PropertyKey {
        static let airlineCodeKey = "airlineCode"
    }
    */

    @Override
    public String getTitle(Context ctx) {
        return (airlineCode != null ? airlineCode : "XX" ) + " " + (routeNo != null ? routeNo : "***") + ": "
                + (departureLocation != null ? departureLocation : "<Departure>") + " - "
                + (arrivalLocation != null ? arrivalLocation : "<Arrival>");
    }

    @Override
    public String getStartInfo(Context ctx) {
        String timeInfo = startTime(DateUtils.FORMAT_SHOW_TIME);
        String airportName = (departureStop != null ? departureStop : "<Departure Airport>");
        String terminalInfo = (departureTerminalCode != null && !departureTerminalCode.isEmpty() ? " [" + departureTerminalCode + "]" : "");
        return (timeInfo != null ? timeInfo + ": " : "") + airportName + terminalInfo;
    }

    @Override
    public String getEndInfo(Context ctx) {
        String timeInfo = endTime(DateUtils.FORMAT_SHOW_TIME);
        String airportName = (arrivalStop != null ? arrivalStop : "<Arrival Airport>");
        String terminalInfo = (arrivalTerminalCode != null && !arrivalTerminalCode.isEmpty() ? " [" + arrivalTerminalCode + "]" : "");
        return (timeInfo != null ? timeInfo + ": " : "") + airportName + terminalInfo;
    }

    @Override
    public String getDetailInfo(Context ctx) {
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

    /* Encode for keyed archive (iOS only?)
    // MARK: NSCoding
    override func encodeWithCoder(aCoder: NSCoder) {
        super.encodeWithCoder(aCoder)
        aCoder.encodeObject(airlineCode, forKey: PropertyKey.airlineCodeKey)
    }
    */

    // MARK: Constructors
    /* Decode from keyed archive (iOS only?)
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        // NB: use conditional cast (as?) for any optional properties
        airlineCode = aDecoder.decodeObjectForKey(PropertyKey.airlineCodeKey) as? String
        setNotification()
    }
    */

    Flight(JSONObject elementData) {
        super(elementData);
        airlineCode = elementData.optString(Constants.JSON.ELEM_COMPANY_CODE);
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
