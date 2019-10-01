package no.shitt.myshit.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;
import android.widget.TextView;

import no.shitt.myshit.Constants;
import no.shitt.myshit.R;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.adapters.TripElementListAdapter;
import no.shitt.myshit.helper.AlertDialogueManager;
import no.shitt.myshit.helper.ConnectionDetector;
import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.AnnotatedTripElement;
import no.shitt.myshit.model.ChangeState;
import no.shitt.myshit.model.TripElement;
import no.shitt.myshit.model.TripList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TripDetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TripDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripDetailsFragment extends Fragment {
    private final static String LOG_TAG = TripDetailsFragment.class.getSimpleName();
    // Fragment initialization parameters
    public static final String ARG_TRIP_CODE = "ARG_TRIP_CODE";
    public static final String ARG_TRIP_ID   = "ARG_TRIP_ID";

    //private int mTripId;
    private String sTripCode;

    View mView;
    ConnectionDetector cd;
    final AlertDialogueManager alert = new AlertDialogueManager();
    ExpandableListView listView;
    private ProgressDialog pDialog;
    private AnnotatedTrip annotatedTrip;

    private OnFragmentInteractionListener mListener;
    private final TripsUpdateHandler tripsUpdateHandler = new TripsUpdateHandler();
    private final TripDetailsUpdateHandler tripDetailsUpdateHandler = new TripDetailsUpdateHandler();
    private final CommErrorHandler  commErrorHandler = new CommErrorHandler();

    public TripDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param tripCode Parameter 1.
     * @return A new instance of fragment trip_details.
     */
    public static TripDetailsFragment newInstance(int tripId, String tripCode) {
        TripDetailsFragment fragment = new TripDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TRIP_ID, tripId);
        args.putString(ARG_TRIP_CODE, tripCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Constants.DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .penaltyFlashScreen()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    //.penaltyDeath()
                    .build());
        }

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sTripCode = getArguments().getString(ARG_TRIP_CODE);
        }

        //setContentView(R.layout.activity_trip_details);
        annotatedTrip = TripList.getSharedList().tripByCode(sTripCode);

        //Context ctx = SHiTApplication.getContext();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(tripDetailsUpdateHandler, new IntentFilter(Constants.Notification.TRIP_DETAILS_LOADED));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(tripsUpdateHandler, new IntentFilter(Constants.Notification.TRIPS_LOADED));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(commErrorHandler, new IntentFilter(Constants.Notification.COMMUNICATION_FAILED));
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_trip_details, container, false);

        listView = (ExpandableListView) mView.findViewById(R.id.trip_details_list);
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id)
            {
                String trip_code = ((TextView) view.findViewById(R.id.element_trip_code)).getText().toString();
                String element_id = ((TextView) view.findViewById(R.id.element_id)).getText().toString();
                AnnotatedTripElement annotatedElement = TripList.getSharedList().tripByCode(trip_code).trip.elementById(Integer.valueOf(element_id));
                TripElement element = annotatedElement.tripElement;

                // Reset modification flag when user views data
                if (annotatedElement.modified != ChangeState.UNCHANGED) {
                    annotatedElement.modified = ChangeState.UNCHANGED;
                    TripList.getSharedList().saveToArchive();
                    updateListView();
                }

                //Intent i;
                Intent i2 = element.getActivityIntent(TripElement.ActivityType.REGULAR);

                // Pass trip id and element id to details view
                i2.putExtra(Constants.IntentExtra.TRIP_CODE, trip_code);
                i2.putExtra(Constants.IntentExtra.ELEMENT_ID, element_id);

                startActivity(i2);
                return true;
            }
        });

        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.trip_details_list_container);
        swipeLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        //Log.i("TripDetailsActivity", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        loadTripDetails(true);
                    }
                }
        );

        if (annotatedTrip == null) {
            Log.e(LOG_TAG, "Invalid trip!");
        } else if (annotatedTrip.trip.elementCount() == 0) {
            // Check if Internet present
            cd = new ConnectionDetector(SHiTApplication.getContext());
            if (!cd.isConnectedToInternet()) {
                // Internet Connection is not present
                alert.showAlertDialogue(getActivity(), getResources().getString(R.string.dlgtitle_network_connection_error),
                        getString(R.string.msg_connect_to_network), false);
                // stop executing code by return
            } else {
                loadTripDetails(false);
            }
        } else {
            updateListView();
        }

        return mView;
    }

    /*
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    */

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(tripDetailsUpdateHandler);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(tripsUpdateHandler);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(commErrorHandler);

        super.onDestroy();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class CommErrorHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            serverCallFailed();
        }
    }

    private class TripsUpdateHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            annotatedTrip = TripList.getSharedList().tripByCode(sTripCode);
            serverCallComplete();
        }
    }

    private class TripDetailsUpdateHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            serverCallComplete();
        }
    }

    /*
    private class HandleNotification extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.Notification.TRIP_DETAILS_LOADED)) {
                serverCallComplete();
            } else if (intent.getAction().equals(Constants.Notification.TRIPS_LOADED)) {
                annotatedTrip = TripList.getSharedList().tripByCode(sTripCode);
                serverCallComplete();
            } else if (intent.getAction().equals(Constants.Notification.COMMUNICATION_FAILED)) {
                serverCallFailed();
            }
        }
    }
    */

    private void loadTripDetails(boolean refresh) {
        if ( ! refresh ) {
            pDialog = new ProgressDialog( getActivity()  /*no.shitt.myshit.ui.TripDetailsFragment.this*/);
            pDialog.setMessage(getString(R.string.msg_loading_trip_details));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        annotatedTrip.trip.loadDetails();
    }

    private void updateListView() {
        if (getActivity() != null) {
            if (annotatedTrip.trip.hasElements() && !annotatedTrip.trip.elementsLoaded()) {
                // Probably reloaded entire trip list, but details for this trip weren't included
                annotatedTrip.trip.loadDetails();
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (listView != null) {
                            TripElementListAdapter adapter = new TripElementListAdapter(SHiTApplication.getContext() /*no.shitt.myshit.ui.TripDetailsFragment.this*/, annotatedTrip);
                            //setListAdapter(adapter);
                            listView.setAdapter(adapter);
                            adapter.applyDefaultCollapse(listView);
                        }
                    }
                });
            }
        } else {
            Log.i("TripDetailsFragment", "No activity found when updating trip details");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d("TripDetailsActivity", "onResume Starting, action = " + getIntent().getAction() + ", Selector = " + getIntent().getSelector());
        // Or maybe not...
        //TripList.getSharedList().getFromServer();
        // Hide keyboard

        //View view = getActivity().getCurrentFocus();
        View view = getView().getRootView();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else {
            Log.d("TripDetailsFragment", "Unable to close keyboard");
        }

        updateListView();
    }

    public void serverCallComplete() {
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.trip_details_list_container);
        swipeLayout.setRefreshing(false);
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
        //Log.d("TripDetailsActivity", "Server call succeeded");
        TripList.getSharedList().saveToArchive();

        updateListView();
    }

    public void serverCallFailed() {
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.trip_details_list_container);
        swipeLayout.setRefreshing(false);
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
        //Log.d("TripDetailsActivity", "Server REST call failed.");
    }
}
