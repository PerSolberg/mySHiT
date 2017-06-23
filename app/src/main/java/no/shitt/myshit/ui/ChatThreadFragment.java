package no.shitt.myshit.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import no.shitt.myshit.Constants;
import no.shitt.myshit.R;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.TripDetailsActivity;
import no.shitt.myshit.adapters.ChatListAdapter;
import no.shitt.myshit.helper.AlertDialogueManager;
import no.shitt.myshit.helper.ConnectionDetector;
import no.shitt.myshit.model.ChatThread;
import no.shitt.myshit.model.Trip;
import no.shitt.myshit.model.TripList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatThreadFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatThreadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatThreadFragment extends Fragment {
    private static final String LOG_TAG = ChatThreadFragment.class.getSimpleName();

    // Parameter arguments
    private static final String ARG_TRIP_CODE = "ARG_TRIP_CODE";
    private static final String ARG_TRIP_ID = "ARG_TRIP_ID";

    //private String sTripCode;
    //private int nTripId;
    Parcelable state;
    Trip trip;

    View mView;
    //ExpandableListView listView;
    ListView listView;
    ConnectionDetector cd;
    private ProgressDialog pDialog;
    private OnFragmentInteractionListener mListener;
    final AlertDialogueManager alert = new AlertDialogueManager();

    public ChatThreadFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of this fragment using the provided parameters.
     *
     * @param tripId Unique ID of trip.
     * @param tripCode Unique code of trip.
     * @return A new instance of fragment chat_thread.
     */
    public static ChatThreadFragment newInstance(int tripId, String tripCode) {
        Log.d(LOG_TAG, "Creating new fragment object");
        ChatThreadFragment fragment = new ChatThreadFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TRIP_ID, tripId);
        args.putString(ARG_TRIP_CODE, tripCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Create fragment from saved state");
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
            //nTripId = getArguments().getInt(ARG_TRIP_ID);
            String sTripCode = getArguments().getString(ARG_TRIP_CODE);
            trip = TripList.getSharedList().tripByCode(sTripCode).trip;
        }
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()/*this*/).registerReceiver(new no.shitt.myshit.ui.ChatThreadFragment.HandleNotification(), new IntentFilter(Constants.Notification.COMMUNICATION_FAILED));
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()/*this*/).registerReceiver(new no.shitt.myshit.ui.ChatThreadFragment.HandleNotification(), new IntentFilter(Constants.Notification.CHAT_UPDATED));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Creating view");
        mView = inflater.inflate(R.layout.fragment_chat_thread, container, false);
        listView = (ListView) mView.findViewById(R.id.trip_chat_message_list);

        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.trip_chat_container);
        swipeLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        //Log.i("TripDetailsActivity", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        refreshChat();
                    }
                }
        );

        EditText msgEntryField = (EditText) mView.findViewById(R.id.chatmsg_entry);
        msgEntryField.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        controlSendButton();
                    }
                }

        );
        if (trip != null && trip.chatThread.count() == 0) {
            // Check if Internet present
            cd = new ConnectionDetector(SHiTApplication.getContext());
            if (!cd.isConnectingToInternet()) {
                // Internet Connection is not present
                alert.showAlertDialogue( getActivity(), "Internet Connection Error",
                        "Please connect to working Internet connection", false);
                // stop executing code by return
            } else {
                refreshChat();
            }
        } else if (trip != null){
            trip.chatThread.refresh(ChatThread.RefreshMode.INCREMENTAL);
        }

        //controlSendButton();
        //restorePosition();
        return mView;
    }

    @Override
    public void onPause() {
        // Save ListView state @ onPause
        state = listView.onSaveInstanceState();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        restorePosition();
        controlSendButton();
        updateChatListView();
    }

    /*
    // Not used in sample, seems not to be needed
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onChatFragmentInteraction(uri);
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
        super.onDetach();
        mListener = null;
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
        void onChatFragmentInteraction(Uri uri);
    }

    /*
     *
     */

    void refreshChat() {
        if (trip != null) {
            trip.chatThread.refresh(ChatThread.RefreshMode.FULL);
        }
    }




    private class HandleNotification extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.Notification.CHAT_UPDATED)) {
                serverCallComplete();
            } else if (intent.getAction().equals(Constants.Notification.COMMUNICATION_FAILED)) {
                serverCallFailed();
            }
        }
    }

    public void serverCallComplete() {
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.trip_chat_container);
        swipeLayout.setRefreshing(false);
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
        //Log.d("TripDetailsActivity", "Server call succeeded");
        TripList.getSharedList().saveToArchive();

        updateChatListView();
    }

    public void serverCallFailed() {
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.trip_chat_container);
        swipeLayout.setRefreshing(false);
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
        //Log.d("TripDetailsActivity", "Server REST call failed.");
    }

    private void updateChatListView() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                if (listView != null) {
                    if (listView.getAdapter() == null) {
                        ChatListAdapter adapter = new ChatListAdapter(SHiTApplication.getContext(), trip.chatThread);
                        listView.setAdapter(adapter);
                        //adapter.applyDefaultCollapse(listView);
                    } else {
                        Log.d(LOG_TAG, "Notify existing adapter to refresh data");
                        ((ChatListAdapter) listView.getAdapter()).notifyDataSetChanged();
                    }
                }
                }
            });
        }
    }

    private void loadChat(boolean refresh) {
        if ( ! refresh ) {
            pDialog = new ProgressDialog(SHiTApplication.getContext() /*no.shitt.myshit.ui.TripDetailsFragment.this*/);
            pDialog.setMessage("Loading chat messages ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        trip.chatThread.refresh( (refresh ? ChatThread.RefreshMode.INCREMENTAL : ChatThread.RefreshMode.FULL) );
    }

    private void controlSendButton() {
        if (getActivity() instanceof TripDetailsActivity) {
            ((TripDetailsActivity) getActivity()).controlSendButton();
        }
    }

    private void restorePosition() {
        if(state != null) {
            Log.d(LOG_TAG, "Trying to restore list view state");
            listView.onRestoreInstanceState(state);
        } else {
            final int pos = trip.chatThread.lastDisplayedItem();
            int msgCount = trip.chatThread.count();
            Log.d(LOG_TAG, "Trying to restore list view position " + pos + "/" + msgCount);
            //listView.setSelection(pos);
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.setSelection(pos);
                }
            });
        }
    }
}
