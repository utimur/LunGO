package android.example.mas;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class PagerSliderAdapter extends FragmentStatePagerAdapter {

    private int count;

    public PagerSliderAdapter(FragmentManager fm, int count) {
        super(fm);
        this.count = count;
    }

    @Override
    public Fragment getItem(int i) {

        switch (i) {
            case 0:
                return new ChatFragment();
            case 1:
                return new ContactsFragment();
                default:
                    return null;
        }
    }

    @Override
    public int getCount() {
        return count;
    }
}
