package com.app.shopifyuser.user;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.app.shopifyuser.R;
import com.app.shopifyuser.Utils.LocationRequester;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener,
        View.OnClickListener {


    public static final int MAP_TYPE_CURRENT_LOCATION = 1, MAP_TYPE_MARK_LOCATION = 2;
    private static final int
            REQUEST_CHECK_SETTINGS = 100,
            REQUEST_LOCATION_PERMISSION = 10;

    private LocationRequester locationRequester;

    private SweetAlertDialog sweetAlertDialog;

    private GoogleMap mMap;
    private Marker currentMapMarker;
    private Button confirmLocationBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);



        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);


        confirmLocationBtn = findViewById(R.id.confirmLocationBtn);

        switch (getIntent().getIntExtra("mapType", 0)) {

            case MAP_TYPE_CURRENT_LOCATION:
                confirmLocationBtn.setOnClickListener(this);
                break;

            case MAP_TYPE_MARK_LOCATION:
                confirmLocationBtn.setVisibility(View.GONE);
                break;

        }

    }


    @Override
    public void onMapReady(@NotNull GoogleMap googleMap) {
        mMap = googleMap;


        switch (getIntent().getIntExtra("mapType", 0)) {

            case MAP_TYPE_CURRENT_LOCATION:

                markCurrentPosition();
                mMap.setOnMapClickListener(this);
                break;

            case MAP_TYPE_MARK_LOCATION:

                final LatLng deliveryLatLng = getIntent().getParcelableExtra("deliveryLatLng");

                LatLng currentLatLng = new LatLng(deliveryLatLng.latitude, deliveryLatLng.longitude);
                currentMapMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Delivery location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));

                break;

        }


    }


    private void markCurrentPosition() {

        final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {

            Log.d("ttt", "requesting location persmission");

            requestPermissions(permissions, REQUEST_LOCATION_PERMISSION);

        } else {

            showProgressDialog();

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            initializeLocationRequester();
        }

    }

    private void initializeLocationRequester() {
        locationRequester = new LocationRequester(this);
        locationRequester.getCurrentLocation();
    }


    private void showProgressDialog() {
        sweetAlertDialog = new
                SweetAlertDialog(MapsActivity.this, SweetAlertDialog.PROGRESS_TYPE);

        sweetAlertDialog.show();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                showProgressDialog();

                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                initializeLocationRequester();

            } else {

                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);

                LatLng sydney = new LatLng(-34, 151);
                currentMapMarker = mMap.addMarker(new MarkerOptions().position(sydney).title("Delivery location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            }
        }

    }

    public void markCurrentLocation(Location location) {

        sweetAlertDialog.dismiss();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        currentMapMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Delivery location"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));

    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

        if (currentMapMarker != null)
            currentMapMarker.remove();

        currentMapMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Delivery location"));

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (locationRequester != null) {
            locationRequester.resumeLocationUpdates();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (locationRequester != null) {
            locationRequester.stopLocationUpdates();
        }
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.confirmLocationBtn) {

            if (currentMapMarker != null) {

                final Intent intent = new Intent();
                intent.putExtra("chosenLatLng", currentMapMarker.getPosition());
                setResult(RESULT_OK, intent);
                finish();

            }

        }
    }
}