package no.shitt.myshit.model;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import no.shitt.myshit.Constants;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.JSONable;
import no.shitt.myshit.helper.ServerAPI;

public class TripList implements ServerAPI.Listener, JSONable, Iterable<AnnotatedTrip> {
    private static final String LOG_TAG = TripList.class.getSimpleName();

    private static final TripList sharedList = new TripList();

    private List<AnnotatedTrip> trips = new ArrayList<>();
    private ServerTimestamp lastUpdateTS;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);

    // Prevent other classes from instantiating - TripList is singleton!
    private TripList() {}

    // Encode to JSON for saving to file
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        int count = 0;

        JSONArray jat = new JSONArray();
        for (AnnotatedTrip at: trips) {
            count++;
            jat.put(at.toJSON());
        }
        jo.put(ServerAPI.ResultItem.COUNT, count);
        jo.put(ServerAPI.ResultItem.API_VERSION, ServerAPI.VERSION_CURRENT);
        jo.put(ServerAPI.ResultItem.TIMESTAMP, lastUpdateTS.toJSON());
        jo.put(ServerAPI.ResultItem.CONTENT, ServerAPI.ResultItemValue.CONTENT_LIST);
        jo.put(ServerAPI.ResultItem.TRIP_LIST, jat);

        return jo;
    }

    // Functions
    public static TripList getSharedList() {
        return sharedList;
    }

    public void getFromServer() {
        //Send request
        ServerAPI.Params params = new ServerAPI.Params(ServerAPI.URL_TRIP_INFO);
        params.addParameter(ServerAPI.Param.USER_NAME, User.sharedUser.getUserName());
        params.addParameter(ServerAPI.Param.PASSWORD, User.sharedUser.getPassword());
        //params.addParameter(ServerAPI.PARAM_DETAILS_TYPE, "non-historic");
        params.addParameter(ServerAPI.Param.LANGUAGE, Locale.getDefault().getLanguage());

        new ServerAPI(this).execute(params);
    }

    //
    // Iterable
    //
    @NonNull
    public Iterator<AnnotatedTrip> iterator() {
        return trips.iterator();
    }


    //
    // Copy data received from server to memory structure
    //
    public void update(JSONObject json, boolean fromArchive) {
        rwl.writeLock().lock();
        performUpdate(json, fromArchive);
        rwl.writeLock().unlock();
    }


    private void performUpdate(JSONObject json, boolean fromArchive) {
        int apiVersion = json.optInt(ServerAPI.ResultItem.API_VERSION);
        boolean legacy = apiVersion < ServerAPI.VERSION_CURRENT;
        String tripsTag = legacy ? ServerAPI.ResultItem.RESULTS_V1 : ServerAPI.ResultItem.TRIP_LIST;
        JSONArray newTrips = json.optJSONArray(tripsTag);
        if (newTrips == null) {
            Log.e(LOG_TAG, "Response does not contain '" + tripsTag + "' element");
            return;
        }
        String contentType = legacy ? ServerAPI.ResultItemValue.CONTENT_LIST : json.optString(ServerAPI.ResultItem.CONTENT);
        if (contentType.equals("")) {
            Log.e(LOG_TAG, "Response does not contain '" + ServerAPI.ResultItem.CONTENT + "' element");
            return;
        }
        JSONObject jsonTS = json.optJSONObject(ServerAPI.ResultItem.TIMESTAMP);
        ServerTimestamp serverTS = null;
        if (jsonTS != null) {
            serverTS = new ServerTimestamp(jsonTS);
        }
        if ( serverTS == null && !legacy ) {
            Log.e(LOG_TAG, "Response does not contain valid '" + ServerAPI.ResultItem.TIMESTAMP + "' element");
            return;
        }

        boolean initialLoad = trips.isEmpty(); // ( lastUpdateTS == null );
        if (contentType.equals(ServerAPI.ResultItemValue.CONTENT_LIST)) {
            if ( lastUpdateTS != null && lastUpdateTS.after(serverTS) ) {
                Log.d(LOG_TAG, "Ignoring old update");
                return;  // Ignore old update
            }
            lastUpdateTS = serverTS;
        }

        // Add or update trips received from server
        boolean changed = false;
        boolean added = false;
        List<Integer> tripIDs = new ArrayList<>();
        for (int i = 0; i < newTrips.length(); i++) {
            JSONObject jsonTrip = newTrips.optJSONObject(i);
            assert jsonTrip != null : "Trip data not in dictionary";
            AnnotatedTrip receivedATrip = new AnnotatedTrip(jsonTrip);
            int tripId = receivedATrip.trip.id;
            tripIDs.add(tripId);
            AnnotatedTrip existingATrip = tripById(tripId);
            if (existingATrip != null) {
                Log.d(LOG_TAG, "Updating existing trip");
                boolean detailsAlreadyLoaded = existingATrip.trip.elementsLoaded();
                boolean tripChanged = existingATrip.trip.update(jsonTrip, serverTS);
                if (tripChanged) {
                    changed = true;
                    existingATrip.modified = ChangeState.CHANGED;
                } else if ( detailsAlreadyLoaded != existingATrip.trip.elementsLoaded() ) {
                    changed = true;
                }
            } else {
                Log.d(LOG_TAG, "Adding new trip");
                if ( ! fromArchive ) {
                    receivedATrip.modified = initialLoad ? ChangeState.UNCHANGED : ChangeState.NEW;
                }
                trips.add(receivedATrip);
                added = true;
            }
        }

        if (contentType.equals(ServerAPI.ResultItemValue.CONTENT_LIST)) {
            // Remove trips no longer in list - but only if we received complete list
            for (int i = trips.size() - 1; i >= 0; i-- ) {
                Trip trip = trips.get(i).trip;
                if ( ! tripIDs.contains(trip.id) ) {
                    trip.deregisterFromPushNotifications();
                    changed = true;
                    trips.remove(i);
                }
            }
        }

        // If new trips were added, sort the list
        if (added) {
//            trips.sort((at1, at2) -> tripIDs.indexOf(at1.trip.id) - tripIDs.indexOf(at2.trip.id) );
            trips.sort(Comparator.comparingInt(at -> tripIDs.indexOf(at.trip.id) ) );

            changed = true;
        }

        if (changed) {
            saveToArchive();
        }
    }


    public void onRemoteCallComplete(JSONObject response) {
        Log.d(LOG_TAG, "Successfully received response from server");
        update(response, false);

        Intent intent = new Intent(Constants.Notification.TRIPS_LOADED);
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }


    public void onRemoteCallFailed() {
        Log.e(LOG_TAG, "Server call failed");
        Intent intent = new Intent(Constants.Notification.COMMUNICATION_FAILED);
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }


    public void onRemoteCallFailed(Exception e) {
        Log.e(LOG_TAG, "Server call failed with exception: " + e.getLocalizedMessage());
        Intent intent = new Intent(Constants.Notification.COMMUNICATION_FAILED);
        intent.putExtra("message", e.getMessage());
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }


    // Load from JSON archive
    public void loadFromArchive() {
        String jsonString = "";

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
            }

            JSONObject jo = new JSONObject(jsonString);
            update(jo, true);
        }
        catch (FileNotFoundException e) {
            //Log.d(LOG_TAG, "File not found: " + e.toString());
        }
        catch (IOException ioe) {
            //Log.e(LOG_TAG, "Failed to load trips due to IO error...");
        }
        catch (JSONException je) {
            //Log.e(LOG_TAG, "Failed to load trips due to JSON error...");
        }
    }


    public void saveToArchive() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute( () -> {
            try {
                rwl.readLock().lock();
                JSONObject jo = this.toJSON();
                String jsonString = jo.toString();
                FileOutputStream fos = SHiTApplication.getContext().openFileOutput(Constants.LOCAL_ARCHIVE_FILE, Context.MODE_PRIVATE);
                fos.write(jsonString.getBytes());
                fos.close();

                Log.d(LOG_TAG, "Trips saved to JSON file");
            }
            catch (JSONException je) {
                //Log.e(LOG_TAG, "Failed to save trips due to JSON error...");
            }
            catch (IOException ioe) {
                //Log.e(LOG_TAG, "Failed to save trips due to IO error...");
            }
            finally {
                rwl.readLock().unlock();
            }
        });
    }


    public int tripCount() {
        if (trips == null)
            return 0;
        else
            return trips.size();
    }


    public AnnotatedTrip tripById(int tripId) {
        rwl.readLock().lock();
        for (int i = 0; i < trips.size(); i++) {
            if (tripId == trips.get(i).trip.id) {
                rwl.readLock().unlock();
                return trips.get(i);
            }
        }
        rwl.readLock().unlock();
        return null;
    }


    public AnnotatedTrip tripByPosition(int position) {
        if (position >= 0 && position < trips.size())
            return trips.get(position);
        else
            return null;
    }


    public AnnotatedTrip tripByCode(String tripCode) {
        rwl.readLock().lock();
        for (int i = 0; i < trips.size(); i++) {
            if (tripCode.equals(trips.get(i).trip.code)) {
                rwl.readLock().unlock();
                return trips.get(i);
            }
        }
        rwl.readLock().unlock();
        return null;
    }


    public void clear() {
        trips = new ArrayList<>();
        saveToArchive();
    }


    public void refreshNotifications() {
        for (int i = 0; i < trips.size(); i++) {
            trips.get(i).trip.refreshNotifications();
        }
    }
}
