package no.shitt.myshit.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import no.shitt.myshit.R;
import no.shitt.myshit.model.AnnotatedTrip;
import no.shitt.myshit.model.TripList;

public class TripListAdapter extends BaseAdapter {
    Context context;

    public TripListAdapter(Context context) {
        this.context = context;
    }

    /*private view holder class*/
    private class ViewHolder {
        ImageView imageView;
        TextView txtName;
        TextView txtInfo;
        TextView txtDesc;
        TextView txtId;
        TextView txtCode;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        LayoutInflater mInflater = (LayoutInflater)
                context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_trip, null);
            holder = new ViewHolder();
            holder.txtId = (TextView) convertView.findViewById(R.id.trip_id);
            holder.txtCode = (TextView) convertView.findViewById(R.id.trip_code);
            holder.imageView = (ImageView) convertView.findViewById(R.id.trip_icon);
            holder.txtName = (TextView) convertView.findViewById(R.id.trip_name);
            holder.txtInfo = (TextView) convertView.findViewById(R.id.trip_info);
            holder.txtDesc = (TextView) convertView.findViewById(R.id.trip_description);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        AnnotatedTrip rowItem = (AnnotatedTrip) getItem(position);

        holder.txtId.setText(Integer.toString(rowItem.trip.id));
        holder.txtCode.setText(rowItem.trip.code);
        holder.imageView.setImageResource(rowItem.trip.getIconId());
        holder.txtName.setText(rowItem.trip.name);
        holder.txtInfo.setText(rowItem.trip.getDateInfo());
        holder.txtDesc.setText(rowItem.trip.tripDescription);

        return convertView;
    }

    @Override
    public int getCount() {
        return TripList.getSharedList().tripCount();
    }

    @Override
    public Object getItem(int position) {
        return TripList.getSharedList().tripByPosition(position);
    }

    @Override
    public long getItemId(int position) {
        return ((AnnotatedTrip) getItem(position)).trip.id;
    }
}