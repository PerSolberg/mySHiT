package no.shitt.myshit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import no.shitt.myshit.helper.AlertDialogueManager;
import no.shitt.myshit.helper.ConnectionDetector;
import no.shitt.myshit.helper.JSONParser;

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
import android.widget.Toast;

public class TripDetailsActivity extends ListActivity {
    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogueManager alert = new AlertDialogueManager();

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jsonParser = new JSONParser();

    ArrayList<HashMap<String, String>> elementsList;

    // tracks JSONArray
    //JSONArray albums = null;
    JSONArray elements = null;

    // Album id
    String trip_id, trip_name;

    // tracks JSON url
    // id - should be posted as GET params to get track list (ex: id = 5)
    //private static final String URL_ALBUMS = "http://api.androidhive.info/songs/album_tracks.php";
    private static final String URL_TRIP_DETAILS = "https://www.shitt.no/mySHiT/trip/code/MICA2016?userName=persolberg@hotmail.com&password=Vertex70&sectioned=0&details=non-historic";

    // ALL JSON node names
    private static final String JSON_QUERY_RESULTS          = "results";
    private static final String JSON_TRIP_ID                = "id";
    private static final String JSON_TRIP_NAME              = "name";
    private static final String JSON_TRIP_ELEMENTS          = "elements";

    //private static final String JSON_ELEM_TYPE              = "type";
    //private static final String JSON_ELEM_SUBTYPE           = "subType";
    private static final String JSON_ELEM_ID                = "id";
    //private static final String JSON_ELEM_REF               = "references";
    //private static final String JSON_ELEM_LEG_NO            = "legNo";
    //private static final String JSON_ELEM_SEG_ID            = "segmentId";
    //private static final String JSON_ELEM_SEG_CODE          = "segmentCode";
    private static final String JSON_ELEM_DEP_TIME          = "departureTime";
    //private static final String JSON_ELEM_DEP_TZ            = "departureTimezone";
    private static final String JSON_ELEM_DEP_LOCATION      = "departureLocation";
    private static final String JSON_ELEM_DEP_STOP          = "departureStop";
    //private static final String JSON_ELEM_DEP_ADDR          = "departureAddress";
    //private static final String JSON_ELEM_DEP_TERMINAL_CODE = "departureTerminalCode";
    //private static final String JSON_ELEM_DEP_TERMINAL_NAME = "departureTerminalName";
    //private static final String JSON_ELEM_DEP_COORDINATES   = "departureCoordinates";
    private static final String JSON_ELEM_ARR_TIME          = "arrivalTime";
    //private static final String JSON_ELEM_ARR_TZ            = "arrivalTimezone";
    private static final String JSON_ELEM_ARR_LOCATION      = "arrivalLocation";
    private static final String JSON_ELEM_ARR_STOP          = "arrivalStop";
    //private static final String JSON_ELEM_ARR_ADDR          = "arrivalAddress";
    //private static final String JSON_ELEM_ARR_TERMINAL_CODE = "arrivalTerminalCode";
    //private static final String JSON_ELEM_ARR_TERMINAL_NAME = "arrivalTerminalName";
    //private static final String JSON_ELEM_ARR_COORDINATES   = "arrivalCoordinates";
    //private static final String JSON_ELEM_ROUTE_NO          = "routeNo";
    //private static final String JSON_ELEM_COMPANY           = "company";
    //private static final String JSON_ELEM_COMPANY_CODE      = "companyCode";
    //private static final String JSON_ELEM_COMPANY_PHONE     = "companyPhone";




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        cd = new ConnectionDetector(getApplicationContext());

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialogue(TripDetailsActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        // Get album id
        Intent i = getIntent();
        trip_id = i.getStringExtra("trip_id");

        // Hashmap for ListView
        elementsList = new ArrayList<HashMap<String, String>>();

        // Loading tracks in Background Thread
        new LoadTracks().execute();

        // get listview
        ListView lv = getListView();

        /**
         * Listview on item click listener
         * SingleTrackActivity will be lauched by passing album id, song id
         * */
        lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // On selecting single track get song information
                Intent i = new Intent(getApplicationContext(), FlightActivity.class);

                // to get song information
                // both album id and song is needed
                String trip_id = ((TextView) view.findViewById(R.id.trip_id)).getText().toString();
                String element_id = ((TextView) view.findViewById(R.id.element_id)).getText().toString();

                Toast.makeText(getApplicationContext(), "Trip Id: " + trip_id  + ", Element Id: " + element_id, Toast.LENGTH_SHORT).show();

                i.putExtra("trip_id", trip_id);
                i.putExtra("element_id", element_id);

                startActivity(i);
            }
        });

    }

    /**
     * Background Async Task to Load all tracks under one album
     * */
    class LoadTracks extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(TripDetailsActivity.this);
            pDialog.setMessage("Loading songs ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting tracks json and parsing
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            // post album id as GET parameter
            //params.add(new BasicNameValuePair(TAG_TRIP_ID, album_id));

            // getting JSON string from URL
            String json = jsonParser.makeHttpRequest(URL_TRIP_DETAILS, "GET",
                    params);

            // Check your log cat for JSON reponse
            Log.d("Trip Details JSON: ", json);

            try {
                JSONObject jObj = new JSONObject(json);
                if (jObj != null) {
                    JSONArray tripListData = jObj.getJSONArray(JSON_QUERY_RESULTS);
                    if (tripListData != null) {
                        JSONObject tripDetailsData = tripListData.getJSONObject(0);
                        if (tripDetailsData != null) {
                            String trip_id = jObj.getString(JSON_TRIP_ID);
                            trip_name = jObj.getString(JSON_TRIP_NAME);
                            elements = jObj.getJSONArray(JSON_TRIP_ELEMENTS);

                            if (elements != null) {
                                // looping through All songs
                                for (int i = 0; i < elements.length(); i++) {
                                    JSONObject e = elements.getJSONObject(i);

                                    // Storing each json item in variable
                                    String element_id = e.getString(JSON_ELEM_ID);
                                    String start_loc  = e.getString(JSON_ELEM_DEP_LOCATION);
                                    String start_stop = e.getString(JSON_ELEM_DEP_STOP);
                                    String start_time = e.getString(JSON_ELEM_DEP_TIME);
                                    String end_loc    = e.getString(JSON_ELEM_ARR_LOCATION);
                                    String end_stop   = e.getString(JSON_ELEM_ARR_STOP);
                                    String end_time   = e.getString(JSON_ELEM_ARR_TIME);

                                    String element_title = start_loc + " - " + end_loc;
                                    String element_info  = start_time + ": " + start_stop + "\n" + end_time + ": " + end_stop;
                                    String element_details = "references go here";

                                    // creating new HashMap
                                    HashMap<String, String> map = new HashMap<String, String>();

                                    // adding each child node to HashMap key => value
                                    map.put(JSON_TRIP_ID, trip_id);
                                    map.put(JSON_ELEM_ID, element_id);
                                    map.put("title", element_title);
                                    map.put("info", element_info);
                                    map.put("details", element_details);

                                    // adding HashList to ArrayList
                                    elementsList.add(map);
                                }
                            } else {
                                Log.d("Trip Details: ", "null");
                            }
                        }
                    }
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
            // dismiss the dialog after getting all tracks
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter( TripDetailsActivity.this
                            , elementsList
                            , R.layout.list_item_trip_element
                            , new String[] { JSON_TRIP_ID, JSON_ELEM_ID, null, "title", "info", "details" }
                            , new int[] { R.id.trip_id, R.id.element_id, R.id.element_icon, R.id.element_title, R.id.element_info, R.id.element_details }
                            );
                    // updating listview
                    setListAdapter(adapter);

                    // Change Activity Title with Album name
                    setTitle(trip_name);
                }
            });

        }

    }
}

/*
android:id="@+id/trip_id"
android:id="@+id/element_id"
android:id="@+id/element_icon"
android:id="@+id/element_title"
android:id="@+id/element_info"
android:id="@+id/element_details"
*/
