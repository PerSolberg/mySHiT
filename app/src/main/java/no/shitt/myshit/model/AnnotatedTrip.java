package no.shitt.myshit.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import no.shitt.myshit.Constants;

public class AnnotatedTrip {
    public final Trip trip;
    public ChangeState modified;

    AnnotatedTrip(JSONObject jsonData) {
        super();

        if (jsonData.isNull(Constants.JSON.ANNTRIP_TRIP)) {
            trip = new Trip(jsonData/*, true*/);
            modified = ChangeState.UNCHANGED;
        } else {
            String modifiedRaw = jsonData.optString(Constants.JSON.ANNTRIP_MODIFIED);
            modified = ChangeState.fromString(modifiedRaw);
            trip = new Trip(Objects.requireNonNull(jsonData.optJSONObject(Constants.JSON.ANNTRIP_TRIP)));
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.put(Constants.JSON.ANNTRIP_MODIFIED, modified.getRawValue());
        jo.put(Constants.JSON.ANNTRIP_TRIP, trip.toJSON());

        return jo;
    }
}
