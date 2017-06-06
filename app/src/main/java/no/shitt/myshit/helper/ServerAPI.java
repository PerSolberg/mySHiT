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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
//import java.util.Iterator;
import java.util.Map;
//import java.util.regex.Pattern;

import no.shitt.myshit.Constants;
//import no.shitt.myshit.SHiTApplication;
//import no.shitt.myshit.model.TripList;

public class ServerAPI extends AsyncTask<ServerAPIParams, String, String> {
    // Interface constants
    public enum Method {
        PUT    ("put"),
        POST   ("post"),
        GET    ("get");

        private final String rawValue;
        Method(String rawValue) {
            this.rawValue = rawValue;
        }

        /*
        public String getRawValue() {
            return rawValue;
        }

        public static Method fromString(String text) {
            if (text != null) {
                for (Method m : Method.values()) {
                    if (text.equalsIgnoreCase(m.rawValue)) {
                        return m;
                    }
                }
            }
            return null;
        }
        */
    }

    public final static String URL_USER_VERIFY  = "https://www.shitt.no/mySHiT/user";
    public final static String URL_TRIP_INFO    = "http://www.shitt.no/mySHiT/trip";
    public final static String URL_BASE         = "http://www.shitt.no/mySHiT";

    public final static String RESOURCE_TRIP    = "trip";
    public final static String RESOURCE_CHAT    = "thread";

    public final static String VERB_MSG_READ    = "read";

    public final static String PARAM_USER_NAME     = "userName";
    public final static String PARAM_PASSWORD      = "password";
    public final static String PARAM_DETAILS_TYPE  = "details";
    public final static String PARAM_SECTIONED     = "sectioned";
    public final static String PARAM_PLATFORM      = "platform";
    public final static String PARAM_LANGUAGE      = "language";

    // Instance properties
    private ServerAPIListener listener;
    private JSONObject response;
    private Method method;

    public ServerAPI(Method method, ServerAPIListener listener) {
        this.method   = method;
        this.listener = listener;
    }

    public ServerAPI(ServerAPIListener listener) {
        this.method   = Method.GET;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(ServerAPIParams... params) {
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
        if (method == Method.GET) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
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
                //Log.d("Server API JSON Data", resultToDisplay);
                response = new JSONObject(sb.toString());
                if (response == null) {
                    //Log.e("ServerAPI", "Empty JSON: " + resultToDisplay);
                    throw new IOException("JSON empty");
                }
            } catch (Exception e) {
                //Log.e("Buffer Error", "Error converting result " + e.getMessage());
                listener.onRemoteCallFailed(e);
                return e.getMessage();
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }
        } else if (method == Method.POST) {
            // TODO:
            Log.i("ServerAPI", "POST");
        } else if (method == Method.PUT) {
            // TODO:
            Log.i("ServerAPI", "PUT");
        }

        return resultToDisplay;
    }

    protected void onPostExecute(String result) {
        //Log.d("ServerAPI", "Calling listener");
        if (response != null) {
            listener.onRemoteCallComplete(response);
        }
    }
}
