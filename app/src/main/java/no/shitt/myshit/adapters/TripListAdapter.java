package no.shitt.myshit.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
//import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import no.shitt.myshit.Constants;
import no.shitt.myshit.R;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.Tense;
import no.shitt.myshit.model.TripList;

public class TripListAdapter extends BaseExpandableListAdapter {
    private final Context context;
    private List<TripSectionInfo> sections;

    private class TripSectionInfo {
        int      titleId;
        protected String   title;
        int      firstElement;
        boolean  expanded;
    }

    /*private view holder class*/
    private class TripViewHolder {
        ImageView imageView;
        ImageView overlayView;
        TextView txtName;
        TextView txtInfo;
        TextView txtDesc;
        TextView txtId;
        TextView txtCode;
    }
    private class GroupViewHolder {
        TextView  txtTitle;
        ImageView imageView;
    }

    // Constructor
    public TripListAdapter(Context context) {
        this.context = context;
        updateSections();
    }

    // public View getView(int position, View convertView, ViewGroup parent)
    @SuppressLint("SetTextI18n")
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TripViewHolder holder;

        LayoutInflater mInflater = (LayoutInflater)
                context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
//            convertView = mInflater.inflate(R.layout.list_item_trip, null);
            convertView = mInflater.inflate(R.layout.list_item_trip, parent, false);
            holder = new TripViewHolder();
            holder.txtId = convertView.findViewById(R.id.trip_id);
            holder.txtCode = convertView.findViewById(R.id.trip_code);
            holder.imageView = convertView.findViewById(R.id.trip_icon);
            holder.overlayView = convertView.findViewById(R.id.trip_icon_overlay);
            holder.txtName = convertView.findViewById(R.id.trip_name);
            holder.txtInfo = convertView.findViewById(R.id.trip_info);
            holder.txtDesc = convertView.findViewById(R.id.trip_description);
            convertView.setTag(holder);
        }
        else {
            holder = (TripViewHolder) convertView.getTag();
        }

        AnnotatedTrip rowItem = (AnnotatedTrip) getChild(groupPosition, childPosition);

        if (rowItem != null) {
            holder.txtId.setText(Integer.toString(rowItem.trip.id));
            holder.txtCode.setText(rowItem.trip.code);
            holder.imageView.setImageIcon(rowItem.trip.getIcon());
            holder.txtName.setText(rowItem.trip.name);
            holder.txtInfo.setText(rowItem.trip.getDateInfo());
            holder.txtDesc.setText(rowItem.trip.tripDescription);

            switch (rowItem.modified) {
                case CHANGED:
                    holder.overlayView.setVisibility(View.VISIBLE);
                    holder.overlayView.setImageResource(R.mipmap.icon_overlay_changed);
                    break;
                case NEW:
                    holder.overlayView.setVisibility(View.VISIBLE);
                    holder.overlayView.setImageResource(R.mipmap.icon_overlay_new);
                    break;
                default:
                    holder.overlayView.setVisibility(View.INVISIBLE);
                    break;
            }
        }
        return convertView;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder holder;

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_group_trip, parent, false);
            holder = new GroupViewHolder();
            holder.txtTitle   = convertView.findViewById(R.id.trip_group_title);
            holder.imageView  = convertView.findViewById(R.id.trip_group_icon);
            convertView.setTag(holder);
        }
        else {
            holder = (GroupViewHolder) convertView.getTag();
        }

        //Log.d("TripListAdapter", "holder = " + holder);
        TripSectionInfo section = (TripSectionInfo) getGroup(groupPosition);
        if (section != null) {
            // When loading list view data for the first time, the sections list is empty
            section.expanded = isExpanded;
            holder.txtTitle.setText(section.title);
        }

        if (isExpanded) {
            holder.imageView.setImageResource(R.mipmap.icon_group_expanded);
        } else {
            holder.imageView.setImageResource(R.mipmap.icon_group_collapsed);
        }

        return convertView;
    }

    @Override
    public int getGroupCount() {
        return sections.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        if (groupPosition < sections.size()) {
            return sections.get(groupPosition);
        }
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        if (groupPosition < sections.size()) {
            TripSectionInfo section = sections.get(groupPosition);
            if (section != null) {
                if (groupPosition < sections.size() - 1) {
                    TripSectionInfo nextSection = sections.get(groupPosition + 1);
                    return nextSection.firstElement - section.firstElement;
                } else {
                    return TripList.getSharedList().tripCount() - section.firstElement;
                }
            }
        }
        return 0;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (groupPosition < sections.size()) {
            TripSectionInfo section = sections.get(groupPosition);
            int elementPosition = section.firstElement + childPosition;
            if (elementPosition < TripList.getSharedList().tripCount()) {
                return TripList.getSharedList().tripByPosition(elementPosition);
            }
        }
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        AnnotatedTrip at = (AnnotatedTrip) getChild(groupPosition, childPosition);
        if (at != null) {
            return at.trip.id;
        }
        return 0;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public void notifyDataSetChanged() {
        updateSections();
        super.notifyDataSetChanged();
    }

    @Override
    public boolean areAllItemsEnabled () {
        return true;
    }


    private void updateSections() {
        List<TripSectionInfo> newSections = new ArrayList<>();
        int     lastSectionTitleId = R.string.group_title_historic;
        int     currentSectionTitleId;
        Context ctx = SHiTApplication.getContext();

//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences sharedPref = ctx.getSharedPreferences(
                ctx.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        String prefUpcoming = sharedPref.getString("pref_upcoming", "");

        for (int i = TripList.getSharedList().tripCount() - 1; i >= 0; i--) {
            AnnotatedTrip trip = TripList.getSharedList().tripByPosition(i);

            if (trip.trip.getTense() == Tense.PAST) {
                currentSectionTitleId = R.string.group_title_historic;
            } else if (trip.trip.getTense() == Tense.PRESENT) {
                currentSectionTitleId = R.string.group_title_current;
            } else {
                Calendar todayPlus7 = Calendar.getInstance();
                todayPlus7.add(Calendar.DAY_OF_MONTH, 7);
                Calendar todayPlus30 = Calendar.getInstance();
                todayPlus30.add(Calendar.DAY_OF_MONTH, 30);

                if (lastSectionTitleId == R.string.group_title_upcoming && Constants.UpcomingPreference.NEXT_ONLY.equals(prefUpcoming)) {
                    currentSectionTitleId = R.string.group_title_future;
                } else if (   (lastSectionTitleId == R.string.group_title_current
                               || lastSectionTitleId == R.string.group_title_historic)
                           && (Constants.UpcomingPreference.NEXT_ONLY.equals(prefUpcoming)
                                || Constants.UpcomingPreference.NEXT_AND_WITHIN_7_DAYS.equals(prefUpcoming)
                                || Constants.UpcomingPreference.NEXT_AND_WITHIN_30_DAYS.equals(prefUpcoming))) {
                    currentSectionTitleId = R.string.group_title_upcoming;
                } else if (   (   Constants.UpcomingPreference.NEXT_AND_WITHIN_7_DAYS.equals(prefUpcoming)
                               || Constants.UpcomingPreference.WITHIN_7_DAYS.equals(prefUpcoming))
                           && todayPlus7.after(trip.trip.getStartTime())) {
                    currentSectionTitleId = R.string.group_title_upcoming;
                } else if (   (   Constants.UpcomingPreference.NEXT_AND_WITHIN_30_DAYS.equals(prefUpcoming)
                        || Constants.UpcomingPreference.WITHIN_30_DAYS.equals(prefUpcoming))
                        && todayPlus30.after(trip.trip.getStartTime())) {
                    currentSectionTitleId = R.string.group_title_upcoming;
                } else {
                    currentSectionTitleId = R.string.group_title_future;
                }
            }

            if (newSections.size() ==  0 || lastSectionTitleId != currentSectionTitleId) {
                TripSectionInfo section = new TripSectionInfo();
                section.titleId = currentSectionTitleId;
                section.title = ctx.getString(currentSectionTitleId);
                section.expanded = true;       // Expand by default
                section.firstElement = i;
                newSections.add(0, section);
            } else {
                newSections.get(0).firstElement = i;
            }
            lastSectionTitleId = currentSectionTitleId;
        }

        if (sections != null) {
            for (int g = 0; g < newSections.size(); g++) {
                TripSectionInfo sectionInfo = newSections.get(g);
                for (TripSectionInfo oldSectionInfo : sections) {
                    if (oldSectionInfo.titleId == sectionInfo.titleId) {
                        sectionInfo.expanded = oldSectionInfo.expanded;
                    }
                }
                //Log.d("TripListAdapter", "Group " + g + ": " + sectionInfo.title + ", first item = " + sectionInfo.firstElement + ", expanded = " + sectionInfo.expanded);
            }
        }

        sections = newSections;
    }

    public void applyPreviousCollapse(ExpandableListView listView) {
        for (int g = 0; g < sections.size(); g++) {
            //Log.d("TripListAdapter", "Collapse/expand group " + g + ": " + sections.get(g).expanded);
            if ( sections.get(g).expanded ) {
                listView.expandGroup(g);
            } else {
                listView.collapseGroup(g);
            }
        }
    }
}