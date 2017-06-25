package no.shitt.myshit.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.List;

import no.shitt.myshit.R;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.model.ChatMessage;
import no.shitt.myshit.model.ChatThread;
import no.shitt.myshit.model.User;

/*
 *  ChatListAdapter
 *  ---------------------------------------------------------------------------
 *  List adapter to populate chat list screen.
 *
 *  Created by Per Solberg on 2017-06-16.
 */

public class ChatListAdapter extends BaseAdapter /* ListAdapter /*BaseExpandableListAdapter */ {
    private static final String LOG_TAG = ChatListAdapter.class.getSimpleName();

    private enum ViewTypes {
        OTHERS_PLAIN(0),
        OTHERS_SEEN_INFO(1),
        OWN_PLAIN(2),
        OWN_SEEN_INFO(3),
        OWN_UNSAVED(4)
        ;

        private final int rawValue;
        ViewTypes(int rawValue) {
            this.rawValue = rawValue;
        }
        static int valueCount() { return size; }

        private static final int size = ViewTypes.values().length;
    }

    private final Context context;
    private final ChatThread chatThread;

    /* Private view holder class */
    private class MessageViewHolder {
        TextView  txtUserInitials;
        TextView  txtMessage;
        TextView  txtSeenInfo;
    }

    public ChatListAdapter(Context context, ChatThread chatThread) {
        this.context = context;
        this.chatThread = chatThread;
        chatThread.registerObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                SHiTApplication.runOnUiThread(new Runnable() {
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int itemPosition) {  // Was getChildId
        return chatThread.get(itemPosition).getId();
    }

    @Override
    public boolean isEmpty() {
        return (chatThread == null) || (chatThread.count() == 0);
    }

    @Override
    public boolean isEnabled(int itemPosition) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() { return true; }

    @Override
    public int getCount() {
        return (chatThread == null) ? 0 : chatThread.count();
    }

    @Override
    public int getViewTypeCount() {
        return ViewTypes.valueCount();
    }

    @Override
    public Object getItem(int itemPosition) {
        return chatThread.get(itemPosition);
    }


    @Override
    public int getItemViewType(int itemPosition) {
        ChatMessage msg = chatThread.get(itemPosition);

        if ( !msg.isStored() ) {
            return ViewTypes.OWN_UNSAVED.rawValue;
        } else if ( msg.getUserId() == User.sharedUser.getId() && msg.getLastSeenBy() == null) {
            return ViewTypes.OWN_PLAIN.rawValue;
        } else if ( msg.getUserId() == User.sharedUser.getId() ) {
            return ViewTypes.OWN_SEEN_INFO.rawValue;
        } else if ( msg.getLastSeenBy() == null) {
            return ViewTypes.OTHERS_PLAIN.rawValue;
        } else {
            return ViewTypes.OTHERS_SEEN_INFO.rawValue;
        }
    }

    @Override
    public View getView(int itemPosition, View convertView, ViewGroup parent) {
        MessageViewHolder holder;

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        int viewType = getItemViewType(itemPosition);
        if (convertView == null || !(convertView instanceof RelativeLayout)) {
            holder = new MessageViewHolder();
            if (viewType == ViewTypes.OTHERS_PLAIN.rawValue) {
                convertView = mInflater.inflate(R.layout.chatmsg_other_plain, parent, false);
                holder.txtUserInitials  = (TextView) convertView.findViewById(R.id.chatmsg_who);
                holder.txtMessage       = (TextView) convertView.findViewById(R.id.chatmsg_text);
                holder.txtSeenInfo      = null;
            } else if (viewType == ViewTypes.OTHERS_SEEN_INFO.rawValue) {
                convertView = mInflater.inflate(R.layout.chatmsg_other_seeninfo, parent, false);
                holder.txtUserInitials = (TextView) convertView.findViewById(R.id.chatmsg_who);
                holder.txtMessage = (TextView) convertView.findViewById(R.id.chatmsg_text);
                holder.txtSeenInfo = (TextView) convertView.findViewById(R.id.chatmsg_seen_info);
            } else if (viewType == ViewTypes.OWN_UNSAVED.rawValue) {
                convertView = mInflater.inflate(R.layout.chatmsg_own_unsaved, parent, false);
                holder.txtUserInitials  = null;
                holder.txtMessage       = (TextView) convertView.findViewById(R.id.chatmsg_text);
                holder.txtSeenInfo      = null;
            } else if (viewType == ViewTypes.OWN_PLAIN.rawValue) {
                convertView = mInflater.inflate(R.layout.chatmsg_own_plain, parent, false);
                holder.txtUserInitials  = null;
                holder.txtMessage       = (TextView) convertView.findViewById(R.id.chatmsg_text);
                holder.txtSeenInfo      = null;
            } else if (viewType == ViewTypes.OWN_SEEN_INFO.rawValue) {
                convertView = mInflater.inflate(R.layout.chatmsg_own_seeninfo, parent, false);
                holder.txtUserInitials  = null;
                holder.txtMessage       = (TextView) convertView.findViewById(R.id.chatmsg_text);
                holder.txtSeenInfo      = (TextView) convertView.findViewById(R.id.chatmsg_seen_info);
            } else {
                Log.e("ChatListAdapter", "Unknown view type " + viewType);
            }
            convertView.setTag(holder);
        }
        else {
            holder = (MessageViewHolder) convertView.getTag();
        }

        ChatMessage msg = (ChatMessage) getItem(itemPosition);

        holder.txtMessage.setText(msg.getMessageText());
        if (holder.txtUserInitials != null) {
            holder.txtUserInitials.setText(msg.getUserInitials());
        }
        if (holder.txtSeenInfo != null) {
            List<String> seenByUsers = msg.getLastSeenBy();
            String seenInfo = "";

            if (seenByUsers.size() > 1) {
                String seenByFirst = TextUtils.join(", ", seenByUsers.subList(0, seenByUsers.size() - 1));
                String seenByLast  = seenByUsers.get(seenByUsers.size() - 1);
                seenInfo = SHiTApplication.getContext().getString(R.string.chat_seen_by_many, seenByFirst, seenByLast);
            } else if (seenByUsers.get(0).equals(ChatThread.LAST_SEEN_BY_EVERYONE)) {
                seenInfo = SHiTApplication.getContext().getString(R.string.chat_seen_by_all);
            } else {
                seenInfo = SHiTApplication.getContext().getString(R.string.chat_seen_by_one, seenByUsers.get(0));
            }
            holder.txtSeenInfo.setText(seenInfo);
        }

        return convertView;
    }
}
