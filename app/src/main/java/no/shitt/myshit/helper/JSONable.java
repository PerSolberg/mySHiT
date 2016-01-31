package no.shitt.myshit.helper;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONable {
    JSONObject toJSON() throws JSONException;
}
