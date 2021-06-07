package com.app.shopifyuser.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.shopifyuser.R;
import com.app.shopifyuser.adapters.ResturantAdapter;
import com.app.shopifyuser.driver.DriverProfileActivity;
import com.app.shopifyuser.model.Resturant;
import com.app.shopifyuser.shared.LocalSave;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class UserActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener,
        NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    SweetAlertDialog sweetAlertDialog;
    RecyclerView rvRes;
    ArrayList<Resturant> resturants;
    FirebaseFirestore firebaseFirestore;
    CollectionReference collectionReference;
    ResturantAdapter resturantAdapter;

    private Toolbar mainToolbar;
    private DrawerLayout drawer_layout;
    private NavigationView navigationView;
    private Spinner categorySpinner;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<String> categories;

    private static final int RESTAURANTS_PAGE_LIMIT = 10;
    private ScrollListener scrollListener;
    private Query restaurantQuery;
    private DocumentSnapshot lastDocSnap;
    private boolean isLoadingItems;

    private String currentCategory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        initViews();
        initItems();
        initClicks();

    }

    private void initClicks() {
        sweetAlertDialog.show();

        mainToolbar.setNavigationOnClickListener(v -> showDrawer());
        mainToolbar.setOnMenuItemClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);

        swipeRefreshLayout.setOnRefreshListener(this);



        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (categories != null && !categories.isEmpty()) {
                    currentCategory = categories.get(position);
                    onRefresh();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void showDrawer() {
        drawer_layout.openDrawer(GravityCompat.START);
    }

    private void initItems() {

        sweetAlertDialog = new SweetAlertDialog(UserActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setCancelable(false);

        firebaseFirestore = FirebaseFirestore.getInstance();

        getFilterSpinner();


        restaurantQuery = firebaseFirestore.collection("resturants")
                .limit(RESTAURANTS_PAGE_LIMIT);

        resturants = new ArrayList<>();
        resturantAdapter = new ResturantAdapter(UserActivity.this, resturants);
        rvRes.setLayoutManager(new LinearLayoutManager(UserActivity.this));
        rvRes.setAdapter(resturantAdapter);


        getMoreRestaurants(true, null);

    }

    private void initViews() {
        mainToolbar = findViewById(R.id.mainToolbar);
        rvRes = findViewById(R.id.rvRes);

        drawer_layout = findViewById(R.id.drawer_layout);
        categorySpinner = findViewById(R.id.categorySpinner);
        navigationView = findViewById(R.id.navigationView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

    }

    private void getFilterSpinner() {

        categories = new ArrayList<>();

        final List<String> spinnerArray = new ArrayList<>();
        categories.add(null);
        spinnerArray.add("All");

        final ArrayAdapter<String> ad = new ArrayAdapter<>(this,
                R.layout.spinner_item_layout, spinnerArray);

        ad.setDropDownViewResource(R.layout.spinner_item_layout);

        categorySpinner.setAdapter(ad);
        firebaseFirestore.collection("Categories")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot snapshot : snapshots) {
                    categories.add(snapshot.getId());
                    spinnerArray.add(snapshot.getId());
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                ad.notifyDataSetChanged();

            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        if (item.getItemId() == R.id.showCart) {
            startActivity(new Intent(UserActivity.this, CartActivity.class));
        } else if (item.getItemId() == R.id.activeOrders) {
            startActivity(new Intent(UserActivity.this, OrdersActivity.class));
        }

        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.signOut) {
            LocalSave.getInstance(this).clear();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else if (item.getItemId() == R.id.editDriverProfile) {
            startActivity(new Intent(this, DriverProfileActivity.class));

        } else if (item.getItemId() == R.id.reportProblem) {
            startActivity(new Intent(this, ReportProblemsActivity.class));
        }

        return true;
    }

    @Override
    public void onBackPressed() {

        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }











    private void getMoreRestaurants(boolean isInitial, String category) {

        isLoadingItems = true;

        swipeRefreshLayout.setRefreshing(true);

        Query updatedQuery = restaurantQuery;

        if (category != null) {
            updatedQuery = restaurantQuery.whereEqualTo("category", category);
        }


        if (lastDocSnap != null) {
            updatedQuery = updatedQuery.startAfter(lastDocSnap);
        }

        updatedQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {

                lastDocSnap = queryDocumentSnapshots.getDocuments().get(
                        queryDocumentSnapshots.size() - 1
                );

                if (isInitial) {
                    resturants.addAll(queryDocumentSnapshots.toObjects(Resturant.class));
                } else {
                    resturants.addAll(resturants.size(),
                            queryDocumentSnapshots.toObjects(Resturant.class));
                }
            }
        }).addOnCompleteListener(task -> {
            if (isInitial) {

                resturantAdapter.notifyDataSetChanged();


                if (task.getResult().size() == RESTAURANTS_PAGE_LIMIT && scrollListener == null) {
                    rvRes.addOnScrollListener(scrollListener = new ScrollListener());
                }

            } else {

                final int resultSize = task.getResult().size();

                resturantAdapter.notifyItemRangeInserted(
                        resturants.size() - resultSize, resultSize);
                if (resultSize < RESTAURANTS_PAGE_LIMIT && scrollListener != null) {
                    rvRes.removeOnScrollListener(scrollListener);
                }
            }

            sweetAlertDialog.dismiss();
            swipeRefreshLayout.setRefreshing(false);

            isLoadingItems = false;
        });


    }


    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingItems &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                getMoreRestaurants(false, currentCategory);

            }
        }
    }


    @Override
    public void onRefresh() {

        resturants.clear();
        resturantAdapter.notifyDataSetChanged();
        lastDocSnap = null;
        getMoreRestaurants(true, currentCategory);

    }
}