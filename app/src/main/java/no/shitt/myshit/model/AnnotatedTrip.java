package no.shitt.myshit.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import no.shitt.myshit.Constants;
import no.shitt.myshit.helper.ServerDate;

public class AnnotatedTrip /* NSObject, NSCoding */ {
    public TripListSection section;
    public Trip trip;
    public ChangeState modified;

    AnnotatedTrip(JSONObject jsonData) {
        super();
        String modifiedRaw = jsonData.optString(Constants.JSON.ANNTRIP_MODIFIED);
        modified = ChangeState.fromString(modifiedRaw);
        trip = new Trip(jsonData.optJSONObject(Constants.JSON.ANNTRIP_TRIP), false);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.put(Constants.JSON.ANNTRIP_MODIFIED, modified.getRawValue());
        jo.put(Constants.JSON.ANNTRIP_TRIP, trip.toJSON());

        return jo;
    }

    AnnotatedTrip(TripListSection section, Trip trip, ChangeState modified) {
        super();
        // Initialize stored properties.
        this.modified = modified;
        this.section = section;
        this.trip = trip;
    }
}
