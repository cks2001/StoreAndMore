package com.exampe.storeandmore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.adapter.SliderPagerAdapter;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;



public class WelcomeActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TextView tvNext;
    private SliderPagerAdapter adapter;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // bind views
        viewPager = findViewById(R.id.pagerIntroSlider);
        TabLayout tabLayout = findViewById(R.id.tabs);
        TextView tvSkip = findViewById(R.id.tvSkip);
        tvNext = findViewById(R.id.tvNext);

        // init slider pager adapter
        adapter = new SliderPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        // set adapter
        viewPager.setAdapter(adapter);

        // set dot indicators
        tabLayout.setupWithViewPager(viewPager);

        tvNext.setOnClickListener(view -> {
            if ((viewPager.getCurrentItem() + 1) < adapter.getCount()) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                new Session(WelcomeActivity.this).setBoolean("is_first_time", true);
                new Session(WelcomeActivity.this).setBoolean("isCartFirstTime", true);
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class).putExtra(Constant.FROM, ""));
                finish();
            }
        });

        tvSkip.setOnClickListener(view -> {
            new Session(WelcomeActivity.this).setBoolean("is_first_time", true);
            new Session(WelcomeActivity.this).setBoolean("isCartFirstTime", true);
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class).putExtra(Constant.FROM, ""));
            finish();
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == adapter.getCount() - 1) {
                    tvNext.setText(R.string.get_started);
                } else {
                    tvNext.setText(R.string.next);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}