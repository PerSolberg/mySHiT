package no.shitt.myshit;

//import no.shitt.myshit.TripDetailsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

import no.shitt.myshit.adapters.TripPagerAdapter;
import no.shitt.myshit.model.TripList;

/**
 * Created by persolberg on 2018-02-02.
 */

public class TripDetailsPopupActivity extends TripDetailsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TripDetailsPopup", "Launching TripDetails as pop-up");
        super.onCreate(savedInstanceState);
        cancelAlert();

        loadTripDetails(true);

        // Switch to messages tab if user clicked on a message notification
        Intent i = getIntent();
        String intentAction = i.getAction();
        if (intentAction != null) {
            if (intentAction.equals(Constants.PushNotificationActions.CHATMSG_CLICK)) {
                Bundle extras = i.getExtras();
                String changeType = extras.getString(Constants.PushNotificationKeys.CHANGE_TYPE);
                if (changeType.equals(Constants.PushNotificationData.TYPE_CHAT_MESSAGE)) {
                    TabLayout tabLayout = (TabLayout) findViewById(R.id.trip_details_tabbar);
                    TabLayout.Tab tab = tabLayout.getTabAt(TripPagerAdapter.TAB_MESSAGES);
                    tab.select();
                }
            }
        } else {
            Log.e("TripDetailsPopup", "Could not get intent action");
        }
    }

    /*
    @Override
    public void onBackPressed() {
        Log.d("TripDetailsPopup", "Back pressed");
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
