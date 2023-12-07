package no.shitt.myshit.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
//import android.net.Uri;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
 * { @link TripDetailsFragment.OnFragmentInteractionListener} interface
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
    private AnnotatedTrip annotatedTrip;

    //private OnFragmentInteractionListener mListener;
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
        Activity a = requireActivity();
        LocalBroadcastManager.getInstance(a).registerReceiver(tripDetailsUpdateHandler, new IntentFilter(Constants.Notification.TRIP_DETAILS_LOADED));
        LocalBroadcastManager.getInstance(a).registerReceiver(tripsUpdateHandler, new IntentFilter(Constants.Notification.TRIPS_LOADED));
        LocalBroadcastManager.getInstance(a).registerReceiver(commErrorHandler, new IntentFilter(Constants.Notification.COMMUNICATION_FAILED));
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_trip_details, container, false);

        listView = mView.findViewById(R.id.trip_details_list);
        listView.setOnChildClickListener((ExpandableListView parent, View view, int groupPosition, int childPosition, long id) -> {
            String trip_code = ((TextView) view.findViewById(R.id.element_trip_code)).getText().toString();
            String element_id = ((TextView) view.findViewById(R.id.element_id)).getText().toString();
            AnnotatedTripElement annotatedElement = TripList.getSharedList().tripByCode(trip_code).trip.elementById(Integer.parseInt(element_id));
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
        });

        SwipeRefreshLayout swipeLayout = mView.findViewById(R.id.trip_details_list_container);
        swipeLayout.setOnRefreshListener( () -> loadTripDetails(true) );

        if (annotatedTrip == null) {
            Log.e(LOG_TAG, "Invalid trip!");
        } else if (annotatedTrip.trip.elementCount() == 0) {
            cd = new ConnectionDetector(SHiTApplication.getContext());
            if (!cd.isConnectedToInternet()) {
                alert.showAlertDialogue(getActivity(), getResources().getString(R.string.dlgtitle_network_connection_error),
                        getString(R.string.msg_connect_to_network), false);
            } else {
                loadTripDetails(false);
            }
        } else {
            updateListView();
        }

        return mView;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }


    @Override
    public void onDestroy() {
        Activity a = requireActivity();
        LocalBroadcastManager.getInstance(a).unregisterReceiver(tripDetailsUpdateHandler);
        LocalBroadcastManager.getInstance(a).unregisterReceiver(tripsUpdateHandler);
        LocalBroadcastManager.getInstance(a).unregisterReceiver(commErrorHandler);
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


    private void loadTripDetails(boolean refresh) {
        if ( ! refresh ) {
            SwipeRefreshLayout swipeLayout = mView.findViewById(R.id.trip_details_list_container);
            swipeLayout.setRefreshing(true);
        }

        annotatedTrip.trip.loadDetails();
    }

    private void updateListView() {
        if (getActivity() != null) {
            if (annotatedTrip.trip.hasElements() && !annotatedTrip.trip.elementsLoaded()) {
                // Probably reloaded entire trip list, but details for this trip weren't included
                annotatedTrip.trip.loadDetails();
            } else {
                getActivity().runOnUiThread( () -> {
                    if (listView != null) {
                        TripElementListAdapter adapter = new TripElementListAdapter(SHiTApplication.getContext() /*no.shitt.myshit.ui.TripDetailsFragment.this*/, annotatedTrip);
                        listView.setAdapter(adapter);
                        adapter.applyDefaultCollapse(listView);
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

        View view = requireView().getRootView();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else {
            Log.d("TripDetailsFragment", "Unable to close keyboard");
        }

        updateListView();
    }

    public void serverCallComplete() {
        SwipeRefreshLayout swipeLayout = mView.findViewById(R.id.trip_details_list_container);
        swipeLayout.setRefreshing(false);

        TripList.getSharedList().saveToArchive();
        updateListView();
    }

    public void serverCallFailed() {
        SwipeRefreshLayout swipeLayout = mView.findViewById(R.id.trip_details_list_container);
        swipeLayout.setRefreshing(false);
    }
}
