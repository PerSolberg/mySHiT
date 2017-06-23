package no.shitt.myshit.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import no.shitt.myshit.R;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.ui.ChatThreadFragment;
import no.shitt.myshit.ui.TripDetailsFragment;

/*
 *  TripPagerAdapter
 *  ---------------------------------------------------------------------------
 *  Handles sideways paging (tabbing) on trip details screen.
 *
 *  Created by Per Solberg on 2017-06-10.
 */

public class TripPagerAdapter extends FragmentPagerAdapter {
    private static final int PAGE_COUNT = 2;
    private final String mTripCode;
    private final int mTripId;

    private static final int tabTitles[] = new int[] { R.string.trip_page_itinerary , R.string.trip_page_messages };
    //private Context context;

    public TripPagerAdapter(FragmentManager fm, Context context, int tripId, String tripCode) {
        super(fm);
        this.mTripId = tripId;
        this.mTripCode = tripCode;
        //this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment f = null;
        switch (position) {
            case 0:
                f = TripDetailsFragment.newInstance(mTripId, mTripCode);
                break;

            case 1:
                f = ChatThreadFragment.newInstance(mTripId, mTripCode);
                break;
        }
        //return PageFragment.newInstance(position + 1);
        return f;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return SHiTApplication.getContext().getString(tabTitles[position]);
    }
}
