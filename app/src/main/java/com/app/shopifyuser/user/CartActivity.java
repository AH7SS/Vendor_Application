package com.app.shopifyuser.user;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.shopifyuser.R;
import com.app.shopifyuser.adapters.CartAdapter;
import com.app.shopifyuser.model.CartItem;
import com.app.shopifyuser.model.DeliveryOrder;
import com.app.shopifyuser.shared.LocalSave;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CartActivity extends AppCompatActivity implements CartAdapter.RemoveCartItemListener,
        SwipeRefreshLayout.OnRefreshListener, CheckoutFragment.ScheduleDeliveryListener {


    public static final int CHECKOUT_RESULT_KEY = 10;
    private static final int CART_PAGE_LIMIT = 10;

    private int currentUserId;


    private RecyclerView cartRv;
    private TextView totalPriceTv, noItemsTv;
    private Button checkOutBtn;



    private ArrayList<CartItem> cartItems;
    private CartAdapter cartAdapter;
    private double totalPrice;




    private DocumentReference userRef;
    private CollectionReference cartRef;
    private LatLng chosenLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        initItems();
        initClicks();


    }

    private void initViews() {
        cartRv = findViewById(R.id.cartRv);
        totalPriceTv = findViewById(R.id.totalPriceTv);
        noItemsTv = findViewById(R.id.noItemsTv);
        checkOutBtn = findViewById(R.id.checkOutBtn);
    }

    private void initClicks() {

        checkOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showPayOptionsBsd();

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHECKOUT_RESULT_KEY && resultCode == RESULT_OK && data != null) {

            if (data.hasExtra("chosenLatLng")) {

                chosenLatLng = data.getParcelableExtra("chosenLatLng");

                new CheckoutFragment(this)
                        .show(getSupportFragmentManager(), "checkOut");

                Log.d("ttt", "chosenLatLng: " + chosenLatLng.toString());

            }

        }

    }

    private void initItems() {

        currentUserId = LocalSave.getInstance(CartActivity.this).getCurrentUser().getId();

        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartItems, this);
        cartRv.setAdapter(cartAdapter);

        userRef = FirebaseFirestore.getInstance().collection("users")
                .document(String.valueOf(currentUserId));

        cartRef = userRef.collection("Cart");



        cartRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {

                if (snapshots != null && !snapshots.isEmpty()) {
                    cartItems.addAll(snapshots.toObjects(CartItem.class));
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!cartItems.isEmpty()) {
                    cartAdapter.notifyDataSetChanged();
                    checkOutBtn.setBackgroundResource(R.drawable.btn_background);

                    setTotalPrice();

                } else {
                    noItemsTv.setVisibility(View.VISIBLE);
                    cartRv.setVisibility(View.INVISIBLE);
                }


            }
        });


    }



    private void setTotalPrice() {

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {

                totalPrice = snapshot.getDouble("cartTotal");

                totalPriceTv.setText(totalPrice + "SAR");

            }
        });



    }

    @Override
    public Task<Void> removeCartItem(int itemId, int position) {

        Task<Void> deleteTask = userRef.collection("Cart")
                .document(String.valueOf(cartItems.get(position).getId()))
                .delete();

        deleteTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                final CartItem removedCartItem = cartItems.get(position);

                final double removedPrice = removedCartItem.getPrice() *
                        removedCartItem.getQuantity();

                userRef.update("cartTotal", FieldValue.increment(-removedPrice))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                totalPrice -= removedPrice;
                                totalPriceTv.setText(totalPrice + "SAR");
                            }
                        });

                cartItems.remove(position);
                cartAdapter.notifyItemRemoved(position);

            }
        });

        return deleteTask;

    }


    private void showPayOptionsBsd() {


        final BottomSheetDialog bsd = new BottomSheetDialog(this, R.style.SheetDialog);
        final View parentView = getLayoutInflater().inflate(R.layout.pay_options_bsd,
                null);
        parentView.setBackgroundColor(Color.TRANSPARENT);

        parentView.findViewById(R.id.payWithCashTv).setOnClickListener(view -> {

            bsd.dismiss();

            final Intent mapIntent = new Intent(CartActivity.this, MapsActivity.class);
            mapIntent.putExtra("mapType", MapsActivity.MAP_TYPE_CURRENT_LOCATION);

            startActivityForResult(mapIntent, CHECKOUT_RESULT_KEY);

        });

        parentView.findViewById(R.id.payWithCreditTv).setOnClickListener(view -> {

            bsd.dismiss();

            final Intent mapIntent = new Intent(CartActivity.this, MapsActivity.class);
            mapIntent.putExtra("mapType", MapsActivity.MAP_TYPE_CURRENT_LOCATION);

            startActivityForResult(mapIntent, CHECKOUT_RESULT_KEY);

        });

        bsd.setOnDismissListener(dialogInterface -> checkOutBtn.setClickable(true));

        bsd.setContentView(parentView);
        bsd.show();


    }

    public void checkOut(long scheduleTime) {


        SweetAlertDialog sweetAlertDialog =
                new SweetAlertDialog(CartActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setTitle("Checking out");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();

        Query mainQuery = FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("type", 2).orderBy("geohash");

        final GeoLocation center = new GeoLocation(chosenLatLng.latitude,
                chosenLatLng.longitude);

        final double radius = 10 * 1000;

        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        for (GeoQueryBounds b : GeoFireUtils.getGeoHashQueryBounds(center, radius)) {
            Query q = mainQuery.startAt(b.startHash).endAt(b.endHash).limit(1);
            tasks.add(q.get());
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {



                String driverId = null;

                for (Task<QuerySnapshot> driverTask : tasks) {

                    if (!driverTask.getResult().getDocuments().isEmpty()) {


                        driverId = driverTask.getResult().getDocuments().get(0).getId();
                        assignDeliveryToDriver(driverId, scheduleTime, chosenLatLng, sweetAlertDialog);
                        return;
                    }

                }

                if (driverId == null || driverId.isEmpty()) {

                    sweetAlertDialog.dismiss();

                    Toast.makeText(CartActivity.this, "Sorry we couldn't find " +
                            "any driver in your area!" +
                            "Please try again later", Toast.LENGTH_LONG).show();


                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                sweetAlertDialog.dismiss();

                Toast.makeText(CartActivity.this, "Sorry we couldn't find " +
                        "any driver in your area!" +
                        "Please try again later", Toast.LENGTH_LONG).show();

                Log.d("ttt", "no driver: " + e.getMessage());

            }
        });
    }

    private void assignDeliveryToDriver(String id, long scheduleTime, LatLng latLng,
                                        SweetAlertDialog sweetAlertDialog) {

        final String deliveryUid = UUID.randomUUID().toString();

        final List<Map<String, Integer>> cartItemsMap = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Map<String, Integer> itemMap = new HashMap<>();
            itemMap.put("itemId", cartItem.getId());
            itemMap.put("quantity", cartItem.getQuantity());
            cartItemsMap.add(itemMap);
        }

        final DeliveryOrder deliveryOrder =
                new DeliveryOrder(deliveryUid, String.valueOf(currentUserId), id, scheduleTime,
                        System.currentTimeMillis(),
                        cartItemsMap, new GeoPoint(latLng.latitude, latLng.longitude)
                        , totalPrice, DeliveryOrder.STATUS_PENDING);

        FirebaseFirestore.getInstance().collection("Deliveries")
                .document(deliveryUid).set(deliveryOrder).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("ttt", "successfully added to cart");

                userRef.collection("Cart").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {

                        for (DocumentSnapshot documentSnapshot : snapshots) {
                            documentSnapshot.getReference().delete();
                        }

                        userRef.update("deliveries", FieldValue.arrayUnion(deliveryUid),
                                "cartTotal", 0)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        finish();
                                        sweetAlertDialog.dismiss();
                                    }
                                });
                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                sweetAlertDialog.dismiss();
                Log.d("ttt", "failed to order: " + e.getMessage());
            }
        });

    }

    @Override
    public void onRefresh() {


    }

    @Override
    public void confirmSchedule(long time) {
        checkOut(time);
    }
}