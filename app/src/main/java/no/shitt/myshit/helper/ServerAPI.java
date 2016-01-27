package no.shitt.myshit.helper;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import no.shitt.myshit.Constants;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.model.TripList;

public class ServerAPI extends AsyncTask<String, String, String> {
    ServerAPIListener listener;
    JSONObject response;

    public ServerAPI(ServerAPIListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0]; // URL to call
        String resultToDisplay = "";

        InputStream in;

        Log.d("Server API", "Connecting");
        // HTTP Get
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());
        } catch (Exception e ) {
            System.out.println(e.getMessage());
            return e.getMessage();
        }

        Log.d("Server API", "Result retrieved");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            in.close();
            //resultToDisplay = sb.toString();
            //Log.d("Server API JSON Data", resultToDisplay);
            response = new JSONObject(sb.toString());
            //TripList.getSharedList().copyServerData(response.optJSONArray(Constants.JSON.QUERY_RESULTS));
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
            listener.onRemoteCallFailed();
        }

        return resultToDisplay;
    }

    protected void onPostExecute(String result) {
        Log.d("ServerAPI", "Calling listener");
        //Intent intent = new Intent("tripsLoaded");
        // You can also include some extra data.
        //intent.putExtra("message", "SHiT trips loaded");
        //LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
        listener.onRemoteCallComplete(response);
    }
}
