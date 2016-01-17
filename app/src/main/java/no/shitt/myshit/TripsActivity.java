package no.shitt.myshit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import no.shitt.myshit.adapters.TripListAdapter;
import no.shitt.myshit.helper.AlertDialogueManager;
import no.shitt.myshit.helper.ConnectionDetector;
import no.shitt.myshit.helper.JSONParser;

import no.shitt.myshit.beans.TripItem;

public class TripsActivity extends ListActivity {
    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogueManager alert = new AlertDialogueManager();

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jsonParser = new JSONParser();

    //ArrayList<HashMap<String, String>> tripList;
    List<TripItem> tripList;

    // albums JSONArray
    //JSONArray albums = null;
    JSONObject tripData = null;

    // albums JSON url
    //private static final String URL_ALBUMS = "http://api.androidhive.info/songs/albums.php";
    private static final String URL_TRIPS = "http://www.shitt.no/mySHiT/trip?userName=persolberg@hotmail.com&password=Vertex70&sectioned=0&details=non-historic";

    // ALL JSON node names
    private static final String JSON_QUERY_RESULTS = "results";
    private static final String JSON_TRIP_ID = "id";
    //private static final String JSON_TRIP_START_DATE = "startDate";
    //private static final String JSON_TRIP_END_DATE = "endDate";
    private static final String JSON_TRIP_DESC = "description";
    //private static final String JSON_TRIP_CODE = "code";
    private static final String JSON_TRIP_NAME = "name";
    //private static final String JSON_TRIP_SECTION = "section";
    private static final String JSON_TRIP_TYPE = "type";
    //private static final String JSON_TRIP_ELEMENTS = "elements";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        cd = new ConnectionDetector(getApplicationContext());

        // Check for internet connection
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialogue(TripsActivity.this, "Internet Connection Error",
                    "Please, please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        // Hashmap for ListView
        //tripList = new ArrayList<HashMap<String, String>>();
        tripList = new ArrayList<>();

        // Loading Albums JSON in Background Thread
        new LoadTrips().execute();

        // get listview
        ListView lv = getListView();

        /**
         * Listview item click listener
         * TrackListActivity will be lauched by passing album id
         * */
        lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // on selecting a single album
                // TrackListActivity will be launched to show tracks inside the album
                Intent i = new Intent(getApplicationContext(), TripDetailsActivity.class);

                // send album id to tracklist activity to get list of songs under that album
                String album_id = ((TextView) view.findViewById(R.id.trip_id)).getText().toString();
                i.putExtra("album_id", album_id);

                startActivity(i);
            }
        });
    }

    /**
     * Background Async Task to Load all Albums by making http request
     * */
    class LoadTrips extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(TripsActivity.this);
            pDialog.setMessage("Loading SHiT Trips ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Albums JSON
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            // getting JSON string from URL
            String json = jsonParser.makeHttpRequest(URL_TRIPS, "GET",
                    params);

            // Check your log cat for JSON reponse
            Log.d("TripData JSON: ", "> " + json);

            try {
                //albums = new JSONArray(json);
                tripData = new JSONObject(json);

                if (tripData != null) {
                    JSONArray tripListData = tripData.getJSONArray(JSON_QUERY_RESULTS);
                    if (tripListData != null) {
                        // looping through All albums
                        for (int i = 0; i < tripListData.length(); i++) {
                            JSONObject c = tripListData.getJSONObject(i);

                            // Storing each json item values in variable
                            String id = c.getString(JSON_TRIP_ID);
                            String name = c.getString(JSON_TRIP_NAME);
                            String desc = c.getString(JSON_TRIP_DESC);
                            String type = c.getString(JSON_TRIP_TYPE);

                            String icon_name = "icon_trip_" + type;
                            int icon_id = getResources().getIdentifier(icon_name.toLowerCase(), "mipmap", getPackageName());
                            //Log.d("Icon ID:", Integer.toString(icon_id));
                            TripItem trip = new TripItem(Integer.valueOf(id), icon_id, name, desc);
                            tripList.add(trip);
                        }
                    }
                }else{
                    Log.d("TripData: ", "null");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all albums
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new TripListAdapter(TripsActivity.this, tripList);
                    /*
                    ListAdapter adapter = new SimpleAdapter( TripsActivity.this
                                                           , tripList
                                                           , R.layout.list_item_trip
                                                           , new String[] { JSON_TRIP_ID, null, JSON_TRIP_NAME, JSON_TRIP_DESC }
                                                           , new int[] { R.id.trip_id, R.id.trip_icon, R.id.trip_name, R.id.trip_description }
                                                           );
                    */

                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }
}