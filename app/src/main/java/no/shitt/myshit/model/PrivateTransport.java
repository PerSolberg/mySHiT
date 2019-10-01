package no.shitt.myshit.model;

import android.content.Intent;

import org.json.JSONObject;

import no.shitt.myshit.Constants;
import no.shitt.myshit.PrivateTransportPopupActivity;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.PrivateTransportActivity;

/**
 * Created by Per Solberg on 2018-01-31.
 */

public class PrivateTransport extends GenericTransport {
    // MARK: Properties
    // No additional properties needed

    // MARK: Constructors
    PrivateTransport(int tripId, String tripCode, JSONObject elementData) {
        super(tripId, tripCode, elementData);
    }


    // MARK: Methods

    @Override
    public Intent getActivityIntent(ActivityType activityType) {
        Intent i;
        switch (activityType) {
            case REGULAR:
                i = new Intent(SHiTApplication.getContext(), PrivateTransportActivity.class);
                break;

            case POPUP:
                i = new Intent(SHiTApplication.getContext(), PrivateTransportPopupActivity.class);
                break;

            default:
                return null;
        }

        i.putExtra(Constants.IntentExtra.TRIP_CODE, tripCode);
        i.putExtra(Constants.IntentExtra.ELEMENT_ID, String.valueOf(id));

        return i;
    }


    @Override
    protected String getNotificationClickAction() {
        return Constants.PushNotificationActions.PRIVATE_TRANSPORT_CLICK;
    }

}


