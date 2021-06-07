package com.app.shopifyuser.user;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.shopifyuser.R;
import com.app.shopifyuser.adapters.TabAdapter;
import com.google.android.material.tabs.TabLayout;

public class OrdersActivity extends AppCompatActivity {


    private Toolbar ordersToolbar;
    private TabLayout tabLayout;
    private ViewPager ordersViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        initViews();
        initItems();
        initClicks();

    }

    private void initViews() {

        ordersToolbar = findViewById(R.id.ordersToolbar);
        tabLayout = findViewById(R.id.tabLayout);
        ordersViewPager = findViewById(R.id.ordersViewPager);


    }


    private void initClicks() {
        ordersToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initItems() {


        final Fragment[] tabFragments = {new ActiveOrdersFragment(), new ReceiptsFragment()};
        final String[] tabTitles = {"Active Orders", "Receipts"};

        final TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, tabFragments, tabTitles);


        ordersViewPager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(ordersViewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_active_orders);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_receipt);


    }

}