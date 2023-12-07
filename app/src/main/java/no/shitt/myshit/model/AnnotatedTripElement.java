package no.shitt.myshit.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import no.shitt.myshit.Constants;

public class AnnotatedTripElement {
    public final TripElement tripElement;
    public ChangeState modified;

    AnnotatedTripElement(int tripId, String tripCode, JSONObject jsonData) {
        super();

        if (jsonData.isNull(Constants.JSON.ANNELEMENT_ELEMENT)) {
            modified = ChangeState.UNCHANGED;
            tripElement = TripElement.createFromDictionary(tripId, tripCode, jsonData);
        } else {
            String modifiedRaw = jsonData.optString(Constants.JSON.ANNELEMENT_MODIFIED);
            modified = ChangeState.fromString(modifiedRaw);
            tripElement = TripElement.createFromDictionary(tripId, tripCode, Objects.requireNonNull(jsonData.optJSONObject(Constants.JSON.ANNELEMENT_ELEMENT)));
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.put(Constants.JSON.ANNELEMENT_MODIFIED, modified.getRawValue());
        jo.put(Constants.JSON.ANNELEMENT_ELEMENT, tripElement.toJSON());

        return jo;
    }
}
