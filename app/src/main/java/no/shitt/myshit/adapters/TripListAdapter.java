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
import no.shitt.myshit.R;
//import com.theopentutorials.android.beans.RowItem;
import no.shitt.myshit.beans.TripItem;

public class TripListAdapter extends BaseAdapter {
    Context context;

    List<TripItem> rowItems;

    public TripListAdapter(Context context, List<TripItem> items) {
        this.context = context;
        this.rowItems = items;
    }

    /*private view holder class*/
    private class ViewHolder {
        ImageView imageView;
        TextView txtName;
        TextView txtDesc;
        TextView txtId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        LayoutInflater mInflater = (LayoutInflater)
                context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_trip, null);
            holder = new ViewHolder();
            holder.txtId = (TextView) convertView.findViewById(R.id.trip_id);
            holder.imageView = (ImageView) convertView.findViewById(R.id.trip_icon);
            holder.txtName = (TextView) convertView.findViewById(R.id.trip_name);
            holder.txtDesc = (TextView) convertView.findViewById(R.id.trip_description);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        TripItem rowItem = (TripItem) getItem(position);

        holder.txtId.setText(Integer.toString(rowItem.getId()));
        holder.imageView.setImageResource(rowItem.getImageId());
        holder.txtName.setText(rowItem.getName());
        holder.txtDesc.setText(rowItem.getDesc());

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