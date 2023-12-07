package no.shitt.myshit.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import no.shitt.myshit.Constants;
import no.shitt.myshit.R;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.adapters.ChatListAdapter;
import no.shitt.myshit.helper.AlertDialogueManager;
import no.shitt.myshit.helper.ConnectionDetector;
import no.shitt.myshit.model.ChatMessage;
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

    Parcelable state;
    Trip trip;
    Activity mActivity;

    View mView;
    ListView listView;
    ConnectionDetector cd;
    final AlertDialogueManager alert = new AlertDialogueManager();
    private final ChatUpdateHandler chatUpdateHandler = new ChatUpdateHandler();
    private final CommErrorHandler  commErrorHandler = new CommErrorHandler();
    private final MessageTextWatcher messageTextWatcher = new MessageTextWatcher();

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
        ChatThreadFragment fragment = new ChatThreadFragment();
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
                    .build());
        }
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String sTripCode = getArguments().getString(ARG_TRIP_CODE);
            trip = TripList.getSharedList().tripByCode(sTripCode).trip;
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.d(LOG_TAG, "Creating view");
        mView = inflater.inflate(R.layout.fragment_chat_thread, container, false);
        listView = mView.findViewById(R.id.trip_chat_message_list);

        SwipeRefreshLayout swipeLayout = mView.findViewById(R.id.trip_chat_container);
        swipeLayout.setOnRefreshListener( () -> loadChat(false) );

        EditText msgEntryField = mView.findViewById(R.id.chatmsg_entry);
        msgEntryField.addTextChangedListener(messageTextWatcher);

        ImageButton sendButton = mView.findViewById(R.id.chat_send_button);
        sendButton.setOnClickListener(this::sendMessage);

        if (trip != null) {
            // Check if Internet present
            cd = new ConnectionDetector(SHiTApplication.getContext());
            if (!cd.isConnectedToInternet()) {
                alert.showAlertDialogue( getActivity(), getString(R.string.dlgtitle_network_connection_error),
                        getString(R.string.msg_connect_to_network), false);
            }
            if (trip.chatThread.count() == 0) {
                loadChat(false);
                //refreshChat();
            } else if (trip != null){
                trip.chatThread.refresh(ChatThread.RefreshMode.INCREMENTAL);
            }
        }
        return mView;
    }


    @Override
    public void onPause() {
        // Save ListView state @ onPause
        state = listView.onSaveInstanceState();

        Context ctx = SHiTApplication.getContext();
        LocalBroadcastManager.getInstance(ctx).unregisterReceiver(commErrorHandler);
        LocalBroadcastManager.getInstance(ctx).unregisterReceiver(chatUpdateHandler);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        Context ctx = SHiTApplication.getContext();
        LocalBroadcastManager.getInstance(ctx).registerReceiver(commErrorHandler, new IntentFilter(Constants.Notification.COMMUNICATION_FAILED));
        LocalBroadcastManager.getInstance(ctx).registerReceiver(chatUpdateHandler, new IntentFilter(Constants.Notification.CHAT_UPDATED));

        trip.chatThread.savePosition();
        controlSendButton();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
        mActivity = null;
    }

    @Override
    public void onDestroy() {
        //mListener = null;
        mActivity = null;
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
        void onChatFragmentInteraction(Uri uri);
    }


    void refreshChat() {
        if (trip != null) {
            trip.chatThread.refresh(ChatThread.RefreshMode.FULL);
        }
    }


    private class ChatUpdateHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SwipeRefreshLayout swipeLayout = mView.findViewById(R.id.trip_chat_container);
            swipeLayout.setRefreshing(false);

            TripList.getSharedList().saveToArchive();
            updateChatListView();
        }
    }

    private class CommErrorHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d(LOG_TAG, "Server REST call failed.");
            SwipeRefreshLayout swipeLayout = mView.findViewById(R.id.trip_chat_container);
            swipeLayout.setRefreshing(false);
        }
    }

    private class MessageTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void afterTextChanged(Editable editable) {
            controlSendButton();
        }
    }


    private void updateChatListView() {
        //Log.i(LOG_TAG, "Updating chat list");
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (listView != null) {
                    if (listView.getAdapter() == null) {
                        //Log.d(LOG_TAG, "Creating new adapter");
                        ChatListAdapter adapter = new ChatListAdapter(SHiTApplication.getContext(), trip.chatThread);
                        trip.chatThread.savePosition();
                        listView.setAdapter(adapter);
                        if (trip.chatThread.restorePosition()) {
                            restorePosition();
                        }
                    } else {
                        //Log.d(LOG_TAG, "Notify existing adapter to refresh data");
                        if (trip.chatThread.restorePosition()) {
                            restorePosition();
                        }
                        ((ChatListAdapter) listView.getAdapter()).notifyDataSetChanged();
                    }
                }
            });
        } else {
            Log.e(LOG_TAG, "Activity not set");
        }
    }


    private void loadChat(boolean refresh) {
        trip.chatThread.refresh( (refresh ? ChatThread.RefreshMode.INCREMENTAL : ChatThread.RefreshMode.FULL) );
    }


    private void restorePosition() {
        if(state != null) {
            //Log.d(LOG_TAG, "Trying to restore list view state");
            listView.onRestoreInstanceState(state);
        } else {
            final int pos = trip.chatThread.lastDisplayedItem();
            listView.post( () -> listView.setSelection(pos) );
        }
    }

    public void sendMessage(View v) {
        //Log.d(LOG_TAG, "Send message icon clicked");
        EditText textField = mActivity.findViewById(R.id.chatmsg_entry);
        String msgText = textField.getText().toString();
        ChatMessage msg = new ChatMessage(msgText);

        trip.chatThread.append(msg);
        textField.getText().clear();
        controlSendButton();

        // Hide keyboard
        View view = mActivity.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm = (InputMethodManager)mActivity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0 /*InputMethodManager.HIDE_IMPLICIT_ONLY*/);
        }

        ListView listView = mActivity.findViewById(R.id.trip_chat_message_list);
        listView.setSelection(trip.chatThread.count());
    }

    private void controlSendButton() {
        EditText textField = mActivity.findViewById(R.id.chatmsg_entry);
        ImageButton button = mActivity.findViewById(R.id.chat_send_button);
        if (textField != null && button != null) {
            boolean enabled = textField.getText().length() != 0;
            button.setEnabled(enabled);

            Drawable originalIcon = AppCompatResources.getDrawable(SHiTApplication.getContext(), R.mipmap.icon_chat);
            Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
            button.setImageDrawable(icon);
        }
    }

    private static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable res = drawable.mutate();
        //res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        res.setTint(Color.GRAY);
        return res;
    }
}
