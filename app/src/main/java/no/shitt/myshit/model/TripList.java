package no.shitt.myshit.model;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import no.shitt.myshit.Constants;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.JSONable;
import no.shitt.myshit.helper.ServerAPI;
import no.shitt.myshit.helper.ServerAPIListener;
import no.shitt.myshit.helper.ServerAPIParams;

public class TripList implements ServerAPIListener, JSONable {
    private static TripList sharedList = new TripList();

    private List<AnnotatedTrip> trips = new ArrayList<>();

    // Prevent other classes from instantiating - User is singleton!
    private TripList() {}

    // Encode to JSON for saving to file
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        int count = 0;

        JSONArray jat = new JSONArray();
        Iterator i = trips.iterator();
        while (i.hasNext()) {
            count++;
            AnnotatedTrip at = (AnnotatedTrip) i.next();
            jat.put(at.trip.toJSON());
        }
        jo.put(Constants.JSON.QUERY_COUNT, count);
        jo.put(Constants.JSON.QUERY_RESULTS, jat);

        return jo;
    }

    // Functions
    public static TripList getSharedList() {
        return sharedList;
    }

    public void getFromServer(/*ServerAPIListener responseHandler*/) {
        //Send request
        ServerAPIParams params = new ServerAPIParams(ServerAPI.URL_TRIP_INFO);
        params.addParameter(ServerAPI.PARAM_USER_NAME, User.sharedUser.getUserName());
        params.addParameter(ServerAPI.PARAM_PASSWORD, User.sharedUser.getPassword());
        params.addParameter(ServerAPI.PARAM_DETAILS_TYPE, "non-historic");

        new ServerAPI(this).execute(params);
    }

    // Copy data received from server to memory structure
    public void copyServerData(JSONArray serverData) {
        // Clear current data and repopulate from server data
        List<AnnotatedTrip> newTripList = new ArrayList<>();

        Log.d("TripList copyServerData", "Copy elements");
        for (int i = 0; i < serverData.length(); i++) {
            Trip newTrip = new Trip(serverData.optJSONObject(i));
            //Log.d("TripList copyServerData", "Element #" + String.valueOf(i));
            newTripList.add(new AnnotatedTrip(TripListSection.HISTORIC, newTrip, ChangeState.UNCHANGED));
        }

        // Determine changes
        Log.d("TripList copyServerData", "Determine changes");
        if (!trips.isEmpty()) {
            for (int i = 0; i < newTripList.size(); i++) {
                AnnotatedTrip newTrip = newTripList.get(i);
                AnnotatedTrip oldTrip = null;
                for (int j = 0; j < trips.size(); j++) {
                    if (trips.get(i).trip.id == newTrip.trip.id) {
                        oldTrip = trips.get(j);
                        break;
                    }
                }
                if (oldTrip == null) {
                    newTrip.modified = ChangeState.NEW;
                } else {
                    newTrip.trip.compareTripElements(oldTrip.trip);
                    if (!newTrip.trip.isEqual(oldTrip.trip)) {
                        newTrip.modified = ChangeState.CHANGED;
                    }
                }
            }
        }

        trips =  newTripList;
    }

    public void onRemoteCallComplete(JSONObject response) {
        Log.d("TripList", "Trip list retrieved - building model");
        copyServerData(response.optJSONArray(Constants.JSON.QUERY_RESULTS));

        Intent intent = new Intent(Constants.Notification.TRIPS_LOADED);
        //intent.putExtra("message", "SHiT trips loaded");
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }

    public void onRemoteCallFailed() {
        //
    }

    // Load from JSON archive
    public void loadFromArchive() {
        String jsonString = "";

        Log.d("TripList", "Loading from local file");
        try {
            InputStream inputStream = SHiTApplication.getContext().openFileInput(Constants.LOCAL_ARCHIVE_FILE);

            if ( inputStream != null ) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                jsonString = stringBuilder.toString();
                Log.d("TripList", "File loaded");
            }

            /*
            for (int i = 0; i < jsonString.length(); i += 1000) {
                Log.d("TripList JSON", jsonString.substring(i, Math.min(i + 1000, jsonString.length())));
            }
            */
            JSONObject jo = new JSONObject(jsonString);
            copyServerData(jo.optJSONArray(Constants.JSON.QUERY_RESULTS));
        }
        catch (FileNotFoundException e) {
            Log.d("TripList", "File not found: " + e.toString());
        }
        catch (IOException ioe) {
            Log.e("TripList", "Failed to load trips due to IO error...");
        }
        catch (JSONException je) {
            Log.e("TripList", "Failed to load trips due to JSON error...");
        }
    }

    public void saveToArchive() {
        try {
            JSONObject jo = this.toJSON();
            String jsonString = jo.toString();
            FileOutputStream fos = SHiTApplication.getContext().openFileOutput(Constants.LOCAL_ARCHIVE_FILE, Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            fos.close();

            Log.d("Trip", "Trips saved to JSON file");
        }
        catch (JSONException je) {
            Log.e("Trip", "Failed to save trips due to JSON error...");
        }
        catch (IOException ioe) {
            Log.e("Trip", "Failed to save trips due to IO error...");
        }
    }

    public int tripCount() {
        if (trips == null)
            return 0;
        else
            return trips.size();
    }

    public AnnotatedTrip tripById(int tripId) {
        for (int i = 0; i < trips.size(); i++) {
            if (tripId == trips.get(i).trip.id) {
                return trips.get(i);
            }
        }
        return null;
    }

    public AnnotatedTrip tripByPosition(int position) {
        if (position >= 0 && position < trips.size())
            return trips.get(position);
        else
            return null;
    }

    public AnnotatedTrip tripByCode(String tripCode) {
        for (int i = 0; i < trips.size(); i++) {
            if (tripCode.equals(trips.get(i).trip.code)) {
                return trips.get(i);
            }
        }
        return null;
    }

    public void clear() {
        trips = new ArrayList<>();
        saveToArchive();
    }
}
