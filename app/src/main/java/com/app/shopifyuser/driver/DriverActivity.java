package com.app.shopifyuser.driver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.shopifyuser.R;
import com.app.shopifyuser.adapters.DeliveryAdapter;
import com.app.shopifyuser.model.DeliveryOrder;
import com.app.shopifyuser.shared.LocalSave;
import com.app.shopifyuser.user.LoginActivity;
import com.app.shopifyuser.user.ReportProblemsActivity;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DriverActivity extends AppCompatActivity implements LocationListener,
        Toolbar.OnMenuItemClickListener,
        NavigationView.OnNavigationItemSelectedListener,
        DeliveryAdapter.DeliveryStatusListener,
        SwipeRefreshLayout.OnRefreshListener {

    private int currentUserId;

    private static final int
            REQUEST_CHECK_SETTINGS = 100,
            REQUEST_LOCATION_PERMISSION = 10,
            MIN_UPDATE_DISTANCE = 10,
            ORDERS_PAGE_LIMIT = 10;



    private ArrayList<DeliveryOrder> deliveryOrders;
    private DeliveryAdapter deliveryAdapter;
    private ScrollListener scrollListener;


    private DocumentReference userRef;
    private Location currentLocation;


    private SweetAlertDialog sweetAlertDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar mainToolbar;
    private DrawerLayout drawer_layout;
    private NavigationView navigationView;
    private RecyclerView deliveryRv;



    private Query deliveryQuery;
    private DocumentSnapshot lastDocSnap;
    private boolean isLoadingDeliveryItems;
    private CollectionReference deliveriesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);


        initViews();
        initItem();
        initClicks();

        final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(permissions, REQUEST_LOCATION_PERMISSION);

        } else {
            checkLocationSettings();
        }

    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationSettings();
            }
        }

    }


    @SuppressLint("MissingPermission")
    private void checkLocationSettings() {

        final LocationRequest locationRequest = LocationRequest.create().
                setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(2000).setFastestInterval(2000);

        final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build())
                .addOnSuccessListener(locationSettingsResponse -> {
                    Log.d("ttt", "location is enabled");

                    LocationManager locationManager = (LocationManager)
                            getSystemService(Context.LOCATION_SERVICE);

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            2000,
                            10, this);


                }).addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                Log.d("ttt", "location is not enabled");
                try {
                    final ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);

                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });

    }


    private void initClicks() {




        mainToolbar.setNavigationOnClickListener(v -> showDrawer());
        mainToolbar.setOnMenuItemClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        swipeRefreshLayout.setOnRefreshListener(this);

    }


    private void initItem() {

        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();


        currentUserId = LocalSave.getInstance(this).getCurrentUser().getId();


        deliveriesRef = FirebaseFirestore.getInstance().collection("Deliveries");
        userRef = FirebaseFirestore.getInstance().collection("users")
                .document(String.valueOf(currentUserId));

        deliveryOrders = new ArrayList<>();
        deliveryAdapter = new DeliveryAdapter(this, deliveryOrders, this);
        deliveryRv.setAdapter(deliveryAdapter);


        final List<Integer> allowedStates = new ArrayList<>(2);
        allowedStates.add(DeliveryOrder.STATUS_PENDING);
        allowedStates.add(DeliveryOrder.STATUS_PICKUP);

        deliveryQuery = deliveriesRef.whereEqualTo("byUser", String.valueOf(currentUserId))
                .whereIn("status", allowedStates)
                .limit(ORDERS_PAGE_LIMIT);

        getMoreDeliveries(true);



    }

    private void getMoreDeliveries(boolean isInitial) {

        isLoadingDeliveryItems = true;

        swipeRefreshLayout.setRefreshing(true);

        Query updatedQuery = deliveryQuery;

        if (lastDocSnap != null) {
            updatedQuery = deliveryQuery.startAfter(lastDocSnap);
        }

        updatedQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {

                lastDocSnap = queryDocumentSnapshots.getDocuments().get(
                        queryDocumentSnapshots.size() - 1);

                if (isInitial) {
                    deliveryOrders.addAll(queryDocumentSnapshots.toObjects(DeliveryOrder.class));
                } else {
                    deliveryOrders.addAll(deliveryOrders.size(),
                            queryDocumentSnapshots.toObjects(DeliveryOrder.class));
                }
            }
        }).addOnCompleteListener(task -> {
            if (isInitial) {

                deliveryAdapter.notifyDataSetChanged();

                if (task.getResult().size() == ORDERS_PAGE_LIMIT && scrollListener == null) {
                    deliveryRv.addOnScrollListener(scrollListener = new ScrollListener());
                }

                if (deliveryOrders.isEmpty()) {
                    swipeRefreshLayout.setRefreshing(false);
                    sweetAlertDialog.dismiss();
                }

            } else {

                final int resultSize = task.getResult().size();

                deliveryAdapter.notifyItemRangeInserted(
                        deliveryOrders.size() - resultSize, resultSize);
                if (resultSize < ORDERS_PAGE_LIMIT && scrollListener != null) {
                    deliveryRv.removeOnScrollListener(scrollListener);
                }
            }

            sweetAlertDialog.dismiss();
            swipeRefreshLayout.setRefreshing(false);

            isLoadingDeliveryItems = false;
        });


    }

    @Override
    public void setStatusPickedUp(int position) {

        if (position < deliveryOrders.size()) {

            final String id = deliveryOrders.get(position).getId();

            if (id == null || id.isEmpty())
                return;

            deliveriesRef.document(id)
                    .update("status", DeliveryOrder.STATUS_PICKUP).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Toast.makeText(DriverActivity.this, "Delivery was picked up!",
                            Toast.LENGTH_SHORT).show();

                }
            });

        }

    }

    @Override
    public void setStatusDelivered(int position) {


        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setTitle("Marking as delivered!");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();


        deliveriesRef.document(deliveryOrders.get(position).getId())
                .update("status", DeliveryOrder.STATUS_DELIVERED,
                        "deliveredAt", System.currentTimeMillis())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        deliveryOrders.remove(position);
                        deliveryAdapter.notifyItemRemoved(position);

                        sweetAlertDialog.dismiss();

                        Toast.makeText(DriverActivity.this,
                                "Delivery receipt created!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sweetAlertDialog.dismiss();
            }
        });

    }

    @Override
    public void onRefresh() {

        deliveryOrders.clear();
        deliveryAdapter.notifyDataSetChanged();
        lastDocSnap = null;
        getMoreDeliveries(true);

    }


    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingDeliveryItems &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                getMoreDeliveries(false);

            }
        }
    }


    public void showDrawer() {
        drawer_layout.openDrawer(GravityCompat.START);
    }


    private void initViews() {

        mainToolbar = findViewById(R.id.mainToolbar);
        deliveryRv = findViewById(R.id.deliveryRv);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        drawer_layout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);

    }




    @Override
    public void onLocationChanged(@NonNull Location location) {

        if (currentLocation != null &&
                currentLocation.distanceTo(location) < MIN_UPDATE_DISTANCE) {
            return;
        }

        Log.d("ttt", "new location is: " +
                location.getLatitude() + "-" + location.getLongitude());

        String hash = GeoFireUtils.getGeoHashForLocation(
                new GeoLocation(location.getLatitude(), location.getLongitude()));
        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put("geohash", hash);
        locationMap.put("lat", location.getLatitude());
        locationMap.put("lng", location.getLongitude());

        userRef.update(locationMap);
        currentLocation = location;

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        ((LocationManager)
                getSystemService(Context.LOCATION_SERVICE)).removeUpdates(this);


    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        if (item.getItemId() == R.id.showReceipts) {
            startActivity(new Intent(this, DeliveriesReceiptsActivity.class));
        }

        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.signOut) {

            LocalSave.getInstance(this).clear();
            startActivity(new Intent(this, LoginActivity.class));

            ((LocationManager)
                    getSystemService(Context.LOCATION_SERVICE)).removeUpdates(this);


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
}