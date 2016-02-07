package no.shitt.myshit.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import no.shitt.myshit.R;
import no.shitt.myshit.helper.StringUtil;
import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.AnnotatedTripElement;
import no.shitt.myshit.model.Tense;

public class TripElementListAdapter extends BaseExpandableListAdapter /*BaseAdapter*/ {
    Context context;
    AnnotatedTrip annotatedTrip;

    List<ElementSectionInfo> sections;

    private class ElementSectionInfo {
        protected String   title;
        protected int      firstElement;
        protected boolean  expandedByDefault;
    }
    /*private view holder class*/
    private class ChildViewHolder {
        TextView  txtTripCode;
        TextView  txtElementId;
        ImageView imageView;
        ImageView overlayView;
        TextView  txtTitle;
        TextView  txtInfo;
        TextView  txtDetails;
    }
    private class GroupViewHolder {
        TextView  txtTitle;
        ImageView imageView;
    }


    public TripElementListAdapter(Context context, AnnotatedTrip annotatedTrip) {
        this.context = context;
        this.annotatedTrip = annotatedTrip;
        updateSections();
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder;

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null || !(convertView instanceof RelativeLayout)) {
            convertView = mInflater.inflate(R.layout.list_item_trip_element, null);
            holder = new ChildViewHolder();
            holder.txtTripCode  = (TextView) convertView.findViewById(R.id.element_trip_code);
            holder.txtElementId = (TextView) convertView.findViewById(R.id.element_id);
            holder.imageView    = (ImageView) convertView.findViewById(R.id.element_icon);
            holder.overlayView  = (ImageView) convertView.findViewById(R.id.element_icon_overlay);
            holder.txtTitle     = (TextView) convertView.findViewById(R.id.element_title);
            holder.txtInfo      = (TextView) convertView.findViewById(R.id.element_info);
            holder.txtDetails   = (TextView) convertView.findViewById(R.id.element_details);
            convertView.setTag(holder);
        }
        else {
            holder = (ChildViewHolder) convertView.getTag();
        }

        //TripElementItem rowItem = (TripElementItem) getItem(position);
        AnnotatedTripElement rowItem = (AnnotatedTripElement) getChild(groupPosition, childPosition);

        StringBuilder info = new StringBuilder();
        StringUtil.appendWithLeadingSeparator(info, rowItem.tripElement.getStartInfo(), "", false);
        StringUtil.appendWithLeadingSeparator(info, rowItem.tripElement.getEndInfo(), "\n", false);

        holder.txtTripCode.setText(rowItem.tripElement.tripCode);
        holder.txtElementId.setText(Integer.toString(rowItem.tripElement.id));
        holder.imageView.setImageResource(rowItem.tripElement.getIconId());
        holder.txtTitle.setText(rowItem.tripElement.getTitle());
        //holder.txtInfo.setText(rowItem.tripElement.getStartInfo() + "\n" + rowItem.tripElement.getEndInfo());
        holder.txtInfo.setText(info.toString());
        holder.txtDetails.setText(rowItem.tripElement.getDetailInfo());

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
        }

        return convertView;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder holder;

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_group_trip_elements, null);
            holder = new GroupViewHolder();
            holder.txtTitle   = (TextView) convertView.findViewById(R.id.element_group_title);
            holder.imageView  = (ImageView) convertView.findViewById(R.id.element_group_icon);
            convertView.setTag(holder);
        }
        else {
            holder = (GroupViewHolder) convertView.getTag();
        }

        //Log.d("TripElementListAdapter", "holder = " + holder);
        ElementSectionInfo section = (ElementSectionInfo) getGroup(groupPosition);
        //Log.d("TripElementListAdapter", "section = " + section + ", title = " + section.title);
        holder.txtTitle.setText(section.title);
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
            ElementSectionInfo section = sections.get(groupPosition);
            if (section != null) {
                if (groupPosition < sections.size() - 1) {
                    ElementSectionInfo nextSection = sections.get(groupPosition + 1);
                    return nextSection.firstElement - section.firstElement;
                } else {
                    return annotatedTrip.trip.elementCount() - section.firstElement;
                }
            }
        }
        return 0;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // TODO
        if (groupPosition < sections.size()) {
            ElementSectionInfo section = sections.get(groupPosition);
            int elementPosition = section.firstElement + childPosition;
            if (elementPosition < annotatedTrip.trip.elementCount()) {
                return annotatedTrip.trip.elementByPosition(childPosition);
            }
        }
        return null;
        //return rowItems.get(position);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return ((AnnotatedTripElement) getChild(groupPosition, childPosition)).tripElement.id;
        //return rowItems.indexOf(getItem(position));
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
    }
    
    private void updateSections() {
        sections = new ArrayList<>();
        String lastSectionTitle = "";
        Tense  lastElementTense = Tense.FUTURE;
        if (annotatedTrip.trip.elements == null) {
            return;
        }
        for (int i = 0; i < annotatedTrip.trip.elements.size(); i++) {
            AnnotatedTripElement element = annotatedTrip.trip.elementByPosition(i);
            String elementTitle = element.tripElement.startTime(DateFormat.MEDIUM, null);
            if (!StringUtil.equal(lastSectionTitle, elementTitle)) {
                // First check if previous section should be expanded by default
                // For active trips, past dates are collapsed by default; active and future dates are expanded
                // For past and future trips, all sections are expanded by default
                if (annotatedTrip.trip.getTense() == Tense.PRESENT && lastElementTense == Tense.PAST) {
                    sections.get(sections.size() - 1).expandedByDefault = false;
                }

                ElementSectionInfo section = new ElementSectionInfo();
                section.title = elementTitle;
                section.firstElement = i;
                section.expandedByDefault = true;
                sections.add(section);
                lastSectionTitle = elementTitle;
            }
            lastElementTense = element.tripElement.getTense();
        }

        /*
        for (int g = 0; g < sections.size(); g++) {
            Log.d("TripElementListAdapter", "Group " + g + ": " + sections.get(g).title + ", first item = " + sections.get(g).firstElement + ", expandByDefault = " + sections.get(g).expandedByDefault);
        }
        */
    }

    public void applyDefaultCollapse(ExpandableListView listView) {
        for (int g = 0; g < sections.size(); g++) {
            if ( sections.get(g).expandedByDefault ) {
                listView.expandGroup(g);
            } else {
                listView.collapseGroup(g);
            }
        }
    }
}
