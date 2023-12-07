package no.shitt.myshit.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import no.shitt.myshit.ui.ChatThreadFragment;
import no.shitt.myshit.ui.TripDetailsFragment;

/*
 *  TripPagerAdapter
 *  ---------------------------------------------------------------------------
 *  Handles sideways paging (tabbing) on trip details screen.
 *
 *  Created by Per Solberg on 2017-06-10.
 */

public class TripPagerAdapter extends FragmentStateAdapter /*FragmentPagerAdapter*/ {
    private static final int PAGE_COUNT = 2;
    public static final int TAB_TRIP_DETAILS = 0;
    public static final int TAB_MESSAGES = 1;

    private final String mTripCode;
    private final int mTripId;

    public TripPagerAdapter(FragmentActivity fa, int tripId, String tripCode) {
        super(fa);
        this.mTripId = tripId;
        this.mTripCode = tripCode;
    }

    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }

    @Override
    @NonNull
    public Fragment createFragment(int position) {
        switch (position) {
            case TAB_TRIP_DETAILS:
                return TripDetailsFragment.newInstance(mTripId, mTripCode);

            case TAB_MESSAGES:
                return ChatThreadFragment.newInstance(mTripId, mTripCode);
        }
        throw new IllegalArgumentException();
    }
}
