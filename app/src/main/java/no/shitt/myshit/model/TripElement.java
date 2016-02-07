package no.shitt.myshit.model;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import no.shitt.myshit.Constants;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.JSONable;
import no.shitt.myshit.helper.StringUtil;

public class TripElement implements JSONable {
    public String type;
    public String subType;
    public int id;
    public List<Map<String,String>> references;

    public String tripCode;
    public int    tripId;
    JSONObject serverData;

    public static final String REFTAG_REF_NO       = "refNo";
    public static final String REFTAG_TYPE         = "type";
    public static final String REFTAG_LOOKUP_URL   = "urlLookup";

    private static final String iconBaseName = "icon_tripelement_";

    public Date getStartTime() {
        return null;
    }
    public String getStartTimeZone() {
        return null;
    }
    public Date getEndTime() {
        return null;
    }
    public String getEndTimeZone() {
        return null;
    }
    public String getTitle() {
        return null;
    }
    public String getStartInfo() {
        return null;
    }
    public String getEndInfo() {
        return null;
    }
    public String getDetailInfo() {
        return null;
    }
    public Tense getTense() {
        Date startTime = getStartTime();
        if (startTime != null) {
            Date today = new Date();

            // If end time isn't set, assume duration of 1 day
            Date endTime = getEndTime();
            if (endTime == null) {
                Calendar cal = GregorianCalendar.getInstance();
                cal.setTime(startTime);
                cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
                endTime = cal.getTime();
            }

            if (today.after(endTime)) {
                return Tense.PAST;
            } else if (today.before(startTime)) {
                return Tense.FUTURE;
            } else {
                return Tense.PRESENT;
            }
        } else {
            return Tense.FUTURE;
        }
    }

    public int getIconId() {
        Context ctx = SHiTApplication.getContext();
        String iconType = "default";
        String iconName;
        int    iconId;

        switch (getTense()) {
            case PAST:
                iconType = "historic";
                break;
            case PRESENT:
                iconType = "active";
                break;
            default:
                break;
        }
        iconName = iconBaseName + type + "_" + subType + "_" + iconType;

        // First try exact match
        iconId = ctx.getResources().getIdentifier(iconName.toLowerCase(), "mipmap", ctx.getPackageName());
        if (iconId != 0) {
            return iconId;
        }

        // Try ignoring subtype
        iconName = iconBaseName + type + "_" + iconType;
        iconId = ctx.getResources().getIdentifier(iconName.toLowerCase(), "mipmap", ctx.getPackageName());
        if (iconId != 0) {
            return iconId;
        }

        // Try dummy image
        iconName = iconBaseName + iconType;
        iconId = ctx.getResources().getIdentifier(iconName.toLowerCase(), "mipmap", ctx.getPackageName());
        if (iconId != 0) {
            return iconId;
        }

        return 0;
    }

    // MARK: Factory
    static TripElement createFromDictionary( int tripId, String tripCode, JSONObject elementData ) {
        String elemType = elementData.optString(Constants.JSON.ELEM_TYPE);
        String elemSubType = elementData.optString(Constants.JSON.ELEM_SUB_TYPE);

        TripElement elem;
        if (elemType.equals("TRA") && elemSubType.equals("AIR")) {
            elem = new Flight(tripId, tripCode, elementData);
        } else if (elemType.equals("TRA")) {
            elem = new GenericTransport(tripId, tripCode, elementData);
        } else if (elemType.equals("ACM")) {
            elem = new Hotel(tripId, tripCode, elementData);
        } else {
            elem = null;
        }

        return elem;
    }

    // Encode to JSON for saving to file
    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.putOpt(Constants.JSON.ELEM_TYPE, type);
        jo.putOpt(Constants.JSON.ELEM_SUB_TYPE, subType);
        jo.putOpt(Constants.JSON.ELEM_ID, id);

        JSONArray jar = new JSONArray();
        Iterator i = references.iterator();
        while (i.hasNext()) {
            Map ref = (Map) i.next();
            jar.put(new JSONObject(ref));
        }
        jo.putOpt(Constants.JSON.ELEM_REFERENCES, jar);

        return jo;
    }

    TripElement(int id, String type, String subType, List<Map<String,String>> references) {
        // Initialize stored properties.
        //self.visible = visible
        super();
        if (id <= 0 || type == null || subType == null) {
            throw new IllegalArgumentException("id, type or subtype is invalid");
        }

        this.id = id;
        this.type = type;
        this.subType = subType;
        this.references = references;
    }

    TripElement(int tripId, String tripCode, JSONObject elementData) {
        id = elementData.optInt("id", -1);
        type = elementData.isNull("type") ? null : elementData.optString("type");
        subType = elementData.isNull("subType") ? null : elementData.optString("subType");

        this.tripId = tripId;
        this.tripCode = tripCode;

        JSONArray serverRefs = elementData.optJSONArray("references");
        if (serverRefs == null) {
            references = null;
        } else {
            references = new ArrayList<>();
            for (int i = 0; i < serverRefs.length(); i++) {
                JSONObject ref = serverRefs.optJSONObject(i);
                Iterator keys = ref.keys();
                Map<String,String> refData = new HashMap<>();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    String value = ref.optString(key);
                    refData.put(key, value);
                }
                references.add(refData);
            }
        }

        serverData = elementData;
    }


    // MARK: Methods
    public boolean isEqual(Object otherObject) {
        if (this.getClass() != otherObject.getClass()) {
            Log.d("GenericTransport", "Changed class! " + this.getClass().getCanonicalName() + " != " + otherObject.getClass().getCanonicalName());
            return false;
        }
        try {
            TripElement otherTripElement = (TripElement) otherObject;

            if (this.id != otherTripElement.id)                             { return false; }
            if (!StringUtil.equal(this.type, otherTripElement.type)      )  { return false; }
            if (!StringUtil.equal(this.subType, otherTripElement.subType))  { return false; }

            if (this.references != null && otherTripElement.references != null) {
                if (this.references.size() != otherTripElement.references.size()) {
                    //Log.d("TripElement", "Changed reference count");
                    return false;
                }

                boolean match = this.references.containsAll(otherTripElement.references) && otherTripElement.references.containsAll(this.references);
                //Log.d("TripElement", "Reference match = " + match);
                return match;
            } else if (this.references != null || otherTripElement.references != null) {
                //Log.d("TripElement", "Changed references");
                return false;
            }

            return true;
        } catch (Exception e) {
            Log.e("TripElement", "Comparison failed with exception");
            return false;
        }
    }


    public String startTime(Integer dateStyle, Integer timeStyle) {
        return null;
    }

    public String endTime(Integer dateStyle, Integer timeStyle) {
        return null;
    }

    public void setNotification() {
        // Generic trip element can't have notifications (start date/time not known)
        // Subclasses that support notifications must override this method
    }

}
