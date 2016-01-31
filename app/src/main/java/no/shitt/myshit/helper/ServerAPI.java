package no.shitt.myshit.helper;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
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
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import no.shitt.myshit.Constants;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.model.TripList;

public class ServerAPI extends AsyncTask<ServerAPIParams, String, String> {
    ServerAPIListener listener;
    JSONObject response;

    public final static String URL_USER_VERIFY  = "https://www.shitt.no/mySHiT/user";
    public final static String URL_TRIP_INFO    = "http://www.shitt.no/mySHiT/trip";

    public final static String PARAM_USER_NAME     = "userName";
    public final static String PARAM_PASSWORD      = "password";
    public final static String PARAM_DETAILS_TYPE  = "details";
    public final static String PARAM_SECTIONING    = "sectioned";
    public final static String PARAM_PLATFORM      = "platform";
    public final static String PARAM_LANGUAGE      = "language";

    public ServerAPI(ServerAPIListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(ServerAPIParams... params) {
        StringBuilder urlBuilder = new StringBuilder(params[0].baseUrl);

        // Build URL
        if (params[0].verb != null) {
            urlBuilder.append("/");
            urlBuilder.append(params[0].verb);
            urlBuilder.append("/");
            urlBuilder.append(params[0].verbArgument);
        }
        Log.d("ServerAPI", "Base URL = " + urlBuilder.toString());

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
        String resultToDisplay;

        // Invoke REST API
        InputStream in;

        Log.d("Server API", "Connecting to " + urlString);
        // HTTP Get
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());

            Log.d("Server API", "Result retrieved");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            reader.close();
            in.close();
            urlConnection.disconnect();

            resultToDisplay = sb.toString();
            //Log.d("Server API JSON Data", resultToDisplay);
            response = new JSONObject(sb.toString());
            if (response == null) {
                Log.e("ServerAPI", "Empty JSON: " + resultToDisplay);
                throw new IOException("JSON empty");
            }
        } catch (Exception e ) {
            Log.e("Buffer Error", "Error converting result " + e.getMessage());
            listener.onRemoteCallFailed();
            return e.getMessage();
        }

        return resultToDisplay;
    }

    protected void onPostExecute(String result) {
        Log.d("ServerAPI", "Calling listener");
        listener.onRemoteCallComplete(response);
    }
}
