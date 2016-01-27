package no.shitt.myshit.helper;

import org.json.JSONObject;

public interface ServerAPIListener {
    void onRemoteCallComplete(JSONObject jsonFromNet);
    void onRemoteCallFailed();
}
