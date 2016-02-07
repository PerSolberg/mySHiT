package no.shitt.myshit.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import no.shitt.myshit.Constants;

public class AnnotatedTripElement /* NSObject, NSCoding */ {
    public TripElement tripElement;
    public ChangeState modified;

    AnnotatedTripElement(int tripId, String tripCode, JSONObject jsonData) {
        super();
        String modifiedRaw = jsonData.optString(Constants.JSON.ANNELEMENT_MODIFIED);
        modified = ChangeState.fromString(modifiedRaw);
        tripElement = TripElement.createFromDictionary(tripId, tripCode, jsonData.optJSONObject(Constants.JSON.ANNELEMENT_ELEMENT));
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.put(Constants.JSON.ANNELEMENT_MODIFIED, modified.getRawValue());
        jo.put(Constants.JSON.ANNELEMENT_ELEMENT, tripElement.toJSON());

        return jo;
    }

    AnnotatedTripElement(TripElement tripElement) {
        super();
        // Initialize stored properties.
        this.modified = ChangeState.UNCHANGED;
        this.tripElement = tripElement;
    }

    AnnotatedTripElement(TripElement tripElement, ChangeState modified) {
        super();
        // Initialize stored properties.
        this.modified = modified;
        this.tripElement = tripElement;
    }
}
