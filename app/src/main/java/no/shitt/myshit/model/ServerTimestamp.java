package no.shitt.myshit.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import no.shitt.myshit.helper.JSONable;

import static no.shitt.myshit.Constants.JSON.SRVTS_FORMATTED;
import static no.shitt.myshit.Constants.JSON.SRVTS_EPOCH;
import static no.shitt.myshit.Constants.JSON.SRVTS_EPOCH_SEC;
import static no.shitt.myshit.Constants.JSON.SRVTS_EPOCH_MICROSEC;

public class ServerTimestamp implements JSONable {
    private static final String LOG_TAG = ServerTimestamp.class.getSimpleName();

    private final String formattedTS;
    private int epochSeconds;
    private int epochMicroSec;


    ServerTimestamp(JSONObject elementData) {
        formattedTS = elementData.optString(SRVTS_FORMATTED);
        JSONObject epoch = elementData.optJSONObject(SRVTS_EPOCH);

        //noinspection ConstantValue
        if (formattedTS == null || epoch == null || epoch.isNull(SRVTS_EPOCH_SEC) || epoch.isNull(SRVTS_EPOCH_MICROSEC)) {
            Log.e(LOG_TAG, "Unable to parse server timestamp: " + elementData);
            return;
        }
        epochSeconds = epoch.optInt(SRVTS_EPOCH_SEC, -1);
        epochMicroSec = epoch.optInt(SRVTS_EPOCH_MICROSEC, -1);
    }


    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        JSONObject epoch = new JSONObject();

        epoch.put(SRVTS_EPOCH_SEC, epochSeconds);
        epoch.put(SRVTS_EPOCH_MICROSEC, epochMicroSec);
        jo.put(SRVTS_FORMATTED, formattedTS);
        jo.put(SRVTS_EPOCH, epoch);

        return jo;
    }


    //
    // MARK: Comparators
    //
    @SuppressWarnings("unused")
    boolean equals(ServerTimestamp other) {
        return (other != null) && (epochSeconds == other.epochSeconds && epochMicroSec == other.epochMicroSec);
    }

    @SuppressWarnings("unused")
    boolean before(ServerTimestamp other) {
        return (other != null) && (epochSeconds < other.epochSeconds || (epochSeconds == other.epochSeconds && epochMicroSec < other.epochMicroSec));
    }

    boolean after(ServerTimestamp other) {
        return (other != null) && (epochSeconds > other.epochSeconds || (epochSeconds == other.epochSeconds && epochMicroSec > other.epochMicroSec) );
    }
}