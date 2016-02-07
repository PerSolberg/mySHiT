package no.shitt.myshit.model;

import org.json.JSONException;
import org.json.JSONObject;

public enum ChangeState {
    NEW("N"),
    CHANGED("C"),
    DELETED("D"),
    UNCHANGED("U");

    private final String rawValue;

    ChangeState(String rawValue) {
        this.rawValue = rawValue;
    }

    public String getRawValue() {
        return rawValue;
    }

    public static ChangeState fromString(String text) {
        if (text != null) {
            for (ChangeState cs : ChangeState.values()) {
                if (text.equalsIgnoreCase(cs.rawValue)) {
                    return cs;
                }
            }
        }
        return null;
    }
}
