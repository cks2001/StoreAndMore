package com.exampe.storeandmore.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.exampe.storeandmore.activity.MainActivity.active;
import static com.exampe.storeandmore.activity.MainActivity.bottomNavigationView;
import static com.exampe.storeandmore.activity.MainActivity.fm;
import static com.exampe.storeandmore.activity.MainActivity.homeClicked;
import static com.exampe.storeandmore.activity.MainActivity.homeFragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.activity.LoginActivity;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;


public class TrackOrderFragment extends Fragment {
    View root;
    LinearLayout lytEmpty, lytDate;
    Session session;
    String[] tabs;
    TabLayout tabLayout;
    ViewPager viewPager;
    TrackOrderFragment.ViewPagerAdapter adapter;
    Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_track_order, container, false);

        activity = getActivity();

        session = new Session(activity);

        if (session.getData(Constant.local_pickup).equals("1")) {
            tabs = new String[]{getString(R.string.all), getString(R.string.pickup), getString(R.string.received), getString(R.string.processed), getString(R.string.shipped1), getString(R.string.delivered1), getString(R.string.cancelled1), getString(R.string.returned1)};
        } else {
            tabs = new String[]{getString(R.string.all), getString(R.string.received), getString(R.string.processed), getString(R.string.shipped1), getString(R.string.delivered1), getString(R.string.cancelled1), getString(R.string.returned1)};
        }

        lytEmpty = root.findViewById(R.id.lytEmpty);
        lytDate = root.findViewById(R.id.lytDate);
        viewPager = root.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(5);
        tabLayout = root.findViewById(R.id.lytTab);
        tabLayout.setupWithViewPager(viewPager);


        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            if (ApiConfig.isConnected(activity)) {
                ApiConfig.getWalletBalance(activity, new Session(activity));
                setupViewPager(viewPager);
            }
        } else {
            startActivity(new Intent(activity, LoginActivity.class).putExtra(Constant.FROM, "tracker"));
        }

        root.findViewById(R.id.btnBorder).setOnClickListener(view -> {
            fm.beginTransaction().show(homeFragment).hide(active).commit();
            bottomNavigationView.setSelectedItemId(0);
            homeClicked = true;
        });

        return root;
    }

    void setupViewPager(ViewPager viewPager) {
        adapter = new TrackOrderFragment.ViewPagerAdapter(fm);
        adapter.addFrag(new OrderListAllFragment(), tabs[0]);

        if (session.getData(Constant.local_pickup).equals("1")) {
            adapter.addFrag(new OrderListPickupFragment(), tabs[1]);
            adapter.addFrag(new OrderListReceivedFragment(), tabs[2]);
            adapter.addFrag(new OrderListProcessedFragment(), tabs[3]);
            adapter.addFrag(new OrderListShippedFragment(), tabs[4]);
            adapter.addFrag(new OrderListDeliveredFragment(), tabs[5]);
            adapter.addFrag(new OrderListCancelledFragment(), tabs[6]);
            adapter.addFrag(new OrderListReturnedFragment(), tabs[7]);
        } else {
            adapter.addFrag(new OrderListReceivedFragment(), tabs[1]);
            adapter.addFrag(new OrderListProcessedFragment(), tabs[2]);
            adapter.addFrag(new OrderListShippedFragment(), tabs[3]);
            adapter.addFrag(new OrderListDeliveredFragment(), tabs[4]);
            adapter.addFrag(new OrderListCancelledFragment(), tabs[5]);
            adapter.addFrag(new OrderListReturnedFragment(), tabs[6]);
        }
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string._title_order_track);
        activity.invalidateOptionsMenu();
        hideKeyboard();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.toolbar_layout).setVisible(false);
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_cart).setVisible(true);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(true);
    }

    @SuppressWarnings("deprecation")
    public class ViewPagerAdapter extends FragmentStatePagerAdapter {
        final List<Fragment> mFragmentList = new ArrayList<>();
        final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            Bundle data = new Bundle();
            data.putInt("pos", position);
            Fragment fragment = null;
            if (position == 0) {
                fragment = new OrderListAllFragment();
            } else {
                if (session.getData(Constant.local_pickup).equals("1")) {
                    if (position == 1) {
                        fragment = new OrderListPickupFragment();
                    } else if (position == 2) {
                        fragment = new OrderListReceivedFragment();
                    } else if (position == 3) {
                        fragment = new OrderListProcessedFragment();
                    } else if (position == 4) {
                        fragment = new OrderListShippedFragment();
                    } else if (position == 5) {
                        fragment = new OrderListDeliveredFragment();
                    } else if (position == 6) {
                        fragment = new OrderListCancelledFragment();
                    } else if (position == 7) {
                        fragment = new OrderListReturnedFragment();
                    }
                } else {
                    if (position == 1) {
                        fragment = new OrderListReceivedFragment();
                    } else if (position == 2) {
                        fragment = new OrderListProcessedFragment();
                    } else if (position == 3) {
                        fragment = new OrderListShippedFragment();
                    } else if (position == 4) {
                        fragment = new OrderListDeliveredFragment();
                    } else if (position == 5) {
                        fragment = new OrderListCancelledFragment();
                    } else if (position == 6) {
                        fragment = new OrderListReturnedFragment();
                    }
                }
            }

            assert fragment != null;
            fragment.setArguments(data);
            return fragment;
        }

        @Override
        public int getCount() {

            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public int getItemPosition(@NotNull Object object) {
            return POSITION_NONE;
        }
    }

}