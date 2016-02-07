package no.shitt.myshit.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import no.shitt.myshit.R;
import no.shitt.myshit.model.TripElement;


public class ReferenceListAdapter extends BaseAdapter {
    Context context;

    public List<Map<String,String>> referenceList;


    public ReferenceListAdapter(Context context, List<Map<String,String>> referenceList) {
        this.context = context;
        this.referenceList = referenceList;
    }

    /*private view holder class*/
    private class ViewHolder {
        TextView  txtRefLabel;
        TextView  txtRefNo;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_reference, null);
            holder = new ViewHolder();
            holder.txtRefLabel  = (TextView) convertView.findViewById(R.id.ref_type);
            holder.txtRefNo     = (TextView) convertView.findViewById(R.id.ref_no);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String, String> reference = (Map<String, String>) getItem(position);

        String refNo = reference.get(TripElement.REFTAG_REF_NO);
        String url = reference.get(TripElement.REFTAG_LOOKUP_URL);
        StringBuilder displayRefNo = new StringBuilder();
        if (url != null) {
            displayRefNo.append("<a href=\"");
            displayRefNo.append(url);
            displayRefNo.append("\">");
            displayRefNo.append(refNo);
            displayRefNo.append("</a>");
        } else {
            displayRefNo.append(refNo);
        }


        holder.txtRefLabel.setText(reference.get(TripElement.REFTAG_TYPE));
        holder.txtRefNo.setText(Html.fromHtml(displayRefNo.toString()));
        holder.txtRefNo.setMovementMethod(LinkMovementMethod.getInstance());

        return convertView;
    }

    @Override
    public int getCount() {
        return referenceList.size();
    }

    @Override
    public Object getItem(int position) {
        return referenceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
