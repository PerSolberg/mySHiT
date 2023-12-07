package no.shitt.myshit;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import android.util.Log;
import android.view.MenuItem;

import java.util.Objects;

import no.shitt.myshit.adapters.TripPagerAdapter;

/**
 * Created by Per Solberg on 2018-02-02.
 */

public class TripDetailsPopupActivity extends TripDetailsActivity {
    private static final String LOG_TAG = TripDetailsPopupActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cancelAlert();

        loadTripDetails(true);

        // Switch to messages tab if user clicked on a message notification
        Intent i = getIntent();
        String intentAction = i.getAction();
        if (intentAction != null) {
            if (intentAction.equals(Constants.PushNotificationActions.CHATMSG_CLICK)) {
                Bundle extras = Objects.requireNonNull( i.getExtras() );
                String changeType = Objects.requireNonNull( extras.getString(Constants.PushNotificationKeys.CHANGE_TYPE) );
                if (changeType.equals(Constants.PushNotificationData.TYPE_CHAT_MESSAGE)) {
                    TabLayout tabLayout = findViewById(R.id.trip_details_tabbar);
                    TabLayout.Tab tab = tabLayout.getTabAt(TripPagerAdapter.TAB_MESSAGES);
                    Objects.requireNonNull(tab).select();
                }
            }
        } else {
            Log.e(LOG_TAG, "Could not get intent action");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
