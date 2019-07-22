package no.shitt.myshit.helper;

//import android.content.Context;
//import android.content.Intent;
import android.accounts.AuthenticatorException;
import android.os.AsyncTask;
//import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
//import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
//import java.util.regex.Pattern;

import no.shitt.myshit.Constants;
//import no.shitt.myshit.SHiTApplication;
//import no.shitt.myshit.model.TripList;

public class ServerAPI extends AsyncTask<ServerAPI.Params, String, String> {
    // Interface constants
    public enum Method {
        PUT    ("PUT"),
        POST   ("POST"),
        GET    ("GET");

        private final String rawValue;
        Method(String rawValue) {
            this.rawValue = rawValue;
        }
    }

    public final static String URL_USER_VERIFY  = "https://www.shitt.no/mySHiT/user";
    public final static String URL_TRIP_INFO    = "http://www.shitt.no/mySHiT/trip";
    public final static String URL_BASE         = "http://www.shitt.no/mySHiT";

    public final static String RESOURCE_TRIP    = "trip";
    public final static String RESOURCE_CHAT    = "thread";

    public final static String VERB_MSG_READ    = "read";

    public final static String PARAM_USER_NAME        = "userName";
    public final static String PARAM_PASSWORD         = "password";
    public final static String PARAM_DETAILS_TYPE     = "details";
    public final static String PARAM_PLATFORM         = "platform";
    public final static String PARAM_LANGUAGE         = "language";
    public final static String PARAM_LAST_MESSAGE_ID  = "lastMessageId";

    // Instance properties
    private final Listener listener;
    private JSONObject response;
    private final Method method;

    public ServerAPI(Method method, Listener listener) {
        this.method   = method;
        this.listener = listener;
    }

    public ServerAPI(Listener listener) {
        this.method   = Method.GET;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Params... params) {
        StringBuilder urlBuilder = new StringBuilder(params[0].baseUrl);

        // Build URL
        if (params[0].resource != null) {
            urlBuilder.append("/");
            urlBuilder.append(params[0].resource);
            if (params[0].resourceId != null) {
                urlBuilder.append("/");
                urlBuilder.append(params[0].resourceId);
            }
        }

        if (params[0].verb != null) {
            urlBuilder.append("/");
            urlBuilder.append(params[0].verb);
            urlBuilder.append("/");
            urlBuilder.append(params[0].verbArgument);
        }
        //Log.d("ServerAPI", "Base URL = " + urlBuilder.toString());

        try {
            if (params[0].parameters != null) {
                boolean firstParam = true;
                for (Map.Entry<String,String> entry : params[0].parameters.entrySet()) {
                    urlBuilder.append(firstParam ? "?" : "&");
                    urlBuilder.append(entry.getKey());
                    urlBuilder.append("=");
                    urlBuilder.append(URLEncoder.encode(entry.getValue(), Constants.URL_ENCODE_CHARSET));
                    firstParam = false;
                }

            }
        }
        catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee.getMessage());
        }

        String urlString = urlBuilder.toString();
        String resultToDisplay = "";

        // Invoke REST API
        InputStream in;

        //Log.d("Server API", "Connecting to " + urlString);
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            Log.i("ServerAPI", "Connecting to " + urlString + " using " + method.rawValue);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput( method == Method.POST || method == Method.PUT);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setRequestMethod(method.rawValue);

            if ( (method == Method.POST || method == Method.PUT) && params[0].getPayload() != null ) {
                OutputStream out = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(out, "UTF-8"));
                writer.write(params[0].getPayload().toString());
                writer.flush();
                writer.close();
                out.close();
            }

            // getResponseCode() connects to server - all settings and data must go before this
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new AuthenticatorException();
            }
            in = new BufferedInputStream(urlConnection.getInputStream());

            //Log.d("Server API", "Result retrieved");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            reader.close();
            in.close();

            resultToDisplay = sb.toString();
            if (sb.length() == 0) {
                throw new IOException("Empty response, expected JSON");
            }
            //Log.d("Server API JSON Data", resultToDisplay);
            response = new JSONObject(sb.toString());
        } catch (Exception e) {
            //Log.e("Buffer Error", "Error converting result " + e.getMessage());
            listener.onRemoteCallFailed(e);
            return e.getMessage();
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }

        return resultToDisplay;
    }

    protected void onPostExecute(String result) {
        //Log.d("ServerAPI", "Calling listener");
        if (response != null) {
            listener.onRemoteCallComplete(response);
        }
    }

    public static class Params {
        public final String baseUrl;
        public String resource;
        public String resourceId;
        public String verb;
        public String verbArgument;
        public Map<String, String> parameters;
        private JSONObject payload;

        public Params(String url) {
            baseUrl = url;
        }

        public Params(String baseUrl, String resource, String resourceId, String verb, String verbArgument) {
            this.baseUrl = baseUrl;
            this.resource = resource;
            this.resourceId = resourceId;
            this.verb = verb;
            this.verbArgument = verbArgument;
        }

        /*
        public Params(String baseUrl, String verb, String verbArgument) {
            this.baseUrl = baseUrl;
            this.verb = verb;
            this.verbArgument = verbArgument;
        }
        */

        public void addParameter(String name, String value) {
            if (parameters == null) {
                parameters = new HashMap<>();
            }
            parameters.put(name, value);
        }

        public void setPayload(JSONObject payload) {
            this.payload = payload;
        }

        JSONObject getPayload() {
            return payload;
        }
    }

    public interface Listener {
        void onRemoteCallComplete(JSONObject jsonFromNet);
        void onRemoteCallFailed();
        void onRemoteCallFailed(Exception e);
    }
}
