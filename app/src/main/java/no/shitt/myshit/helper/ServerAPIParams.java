package no.shitt.myshit.helper;

import java.util.HashMap;
import java.util.Map;

public class ServerAPIParams {
    public String baseUrl;
    public String verb;
    public String verbArgument;
    public Map<String, String> parameters;

    public ServerAPIParams(String url) {
        baseUrl = url;
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
}
