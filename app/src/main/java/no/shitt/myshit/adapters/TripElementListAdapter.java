package no.shitt.myshit.adapters;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
//import com.theopentutorials.android.activities.R;
import org.w3c.dom.Text;

import no.shitt.myshit.R;
//import com.theopentutorials.android.beans.RowItem;
import no.shitt.myshit.beans.TripElementItem;

public class TripElementListAdapter extends BaseAdapter {
    Context context;

    List<TripElementItem> rowItems;

    public TripElementListAdapter(Context context, List<TripElementItem> items) {
        this.context = context;
        this.rowItems = items;
    }

    /*private view holder class*/
    private class ViewHolder {
        TextView  txtTripId;
        TextView  txtElementId;
        ImageView imageView;
        TextView  txtTitle;
        TextView  txtInfo;
        TextView  txtDetails;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        LayoutInflater mInflater = (LayoutInflater)
                context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_trip_element, null);
            holder = new ViewHolder();
            holder.txtTripId    = (TextView) convertView.findViewById(R.id.trip_id);
            holder.txtElementId = (TextView) convertView.findViewById(R.id.element_id);
            holder.imageView    = (ImageView) convertView.findViewById(R.id.element_icon);
            holder.txtTitle     = (TextView) convertView.findViewById(R.id.element_title);
            holder.txtInfo      = (TextView) convertView.findViewById(R.id.element_info);
            holder.txtDetails   = (TextView) convertView.findViewById(R.id.element_details);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        TripElementItem rowItem = (TripElementItem) getItem(position);

        holder.txtTripId.setText(Integer.toString(rowItem.getTripId()));
        holder.txtElementId.setText(Integer.toString(rowItem.getId()));
        holder.imageView.setImageResource(rowItem.getImageId());
        holder.txtTitle.setText(rowItem.getName());
        holder.txtInfo.setText(rowItem.getStart() + "\n" + rowItem.getEnd());
        holder.txtDetails.setText(rowItem.getDesc());

        return convertView;
    }

    @Override
    public int getCount() {
        return rowItems.size();
    }

    @Override
    public Object getItem(int position) {
        return rowItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return rowItems.indexOf(getItem(position));
    }
}
