package no.shitt.myshit.helper;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ServerAPIParams {
    public String baseUrl;
    public String resource;
    public String resourceId;
    public String verb;
    public String verbArgument;
    public Map<String, String> parameters;
    private JSONObject payload;

    public ServerAPIParams(String url) {
        baseUrl = url;
    }

    public ServerAPIParams(String baseUrl, String resource, String resourceId, String verb, String verbArgument) {
        this.baseUrl = baseUrl;
        this.resource = resource;
        this.resourceId = resourceId;
        this.verb = verb;
        this.verbArgument = verbArgument;
    }

    public ServerAPIParams(String baseUrl, String verb, String verbArgument) {
        this.baseUrl = baseUrl;
        this.verb = verb;
        this.verbArgument = verbArgument;
    }

    public void addParameter(String name, String value) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        parameters.put(name, value);
    }

    public void setPayload(JSONObject payload) {
        this.payload = payload;
    }

    public JSONObject getPayload() {
        return payload;
    }
}
