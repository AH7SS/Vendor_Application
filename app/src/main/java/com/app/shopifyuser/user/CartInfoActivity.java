package com.app.shopifyuser.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.app.shopifyuser.R;
import com.app.shopifyuser.Utils.TimeFormatter;
import com.app.shopifyuser.adapters.CartAdapter;
import com.app.shopifyuser.model.CartItem;
import com.app.shopifyuser.model.DeliveryOrder;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CartInfoActivity extends AppCompatActivity
        implements CartAdapter.RemoveCartItemListener {

    private static final int CART_PAGE_LIMIT = 10;



    private Toolbar activeToolbar;
    private TextView cartCostTv, cartOrderedAtTv, cartScheduledAtTv, cartDriverTv, cartDriverNumberTv, orderStatusTv;
    private RecyclerView cartOrdersRv;
    private ImageView cartLocationIv;


    private ArrayList<CartItem> cartItems;
    private CartAdapter cartAdapter;
    private double totalPrice;


    private DocumentReference userRef;
    private CollectionReference cartRef;
    private String cartId;
    private GeoPoint deliveryGeoPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_info);

        cartId = getIntent().getStringExtra("cartId");

        initViews();
        initItems();
        initClicks();

    }


    private void initViews() {
        activeToolbar = findViewById(R.id.activeToolbar);
        cartCostTv = findViewById(R.id.cartCostTv);
        cartOrderedAtTv = findViewById(R.id.cartOrderedAtTv);
        cartScheduledAtTv = findViewById(R.id.cartScheduledAtTv);
        cartDriverTv = findViewById(R.id.cartDriverTv);
        cartDriverNumberTv = findViewById(R.id.cartDriverNumberTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        cartOrdersRv = findViewById(R.id.cartOrdersRv);
        cartLocationIv = findViewById(R.id.cartLocationIv);
    }


    private void initItems() {

        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartItems);
        cartOrdersRv.setAdapter(cartAdapter);


        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();


        firestore.collection("Deliveries")
                .document(cartId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {

                if (snapshot == null || !snapshot.exists())
                    return;


                deliveryGeoPoint = snapshot.getGeoPoint("location");


                if (snapshot.contains("status")) {

                    final Long status = snapshot.getLong("status");

                    if (status != null) {
                        switch (status.intValue()) {

                            case DeliveryOrder.STATUS_PENDING:

                                orderStatusTv.setText("Order status: Pending");

                                break;
                            case DeliveryOrder.STATUS_PICKUP:

                                orderStatusTv.setText("Order status: Picked up");
                                break;
                            case DeliveryOrder.STATUS_DELIVERED:

                                orderStatusTv.setText("Order status: Delivered");

                                break;
                            case DeliveryOrder.STATUS_CANCELLED:

                                orderStatusTv.setText("Order status: Cancelled");

                                break;

                            default:

                                orderStatusTv.setText("Order status: Unknown");

                                break;

                        }
                    }

                }

                cartCostTv.setText("Total cost: " + snapshot.getDouble("totalPrice") + "SAR");
                cartOrderedAtTv.setText("Ordered at: " + TimeFormatter.formatTime(snapshot.getLong("orderedAt")));
                cartScheduledAtTv.setText("Scheduled at: " + TimeFormatter.formatTime(snapshot.getLong("scheduledTime")));

                firestore.collection("users")
                        .document(snapshot.getString("byUser"))
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        if (snapshot.exists()) {
                            cartDriverTv.setText("Driver's name: " + snapshot.getString("username"));
                            cartDriverNumberTv.setText("Driver's phone number: " + snapshot.getString("phoneNumber"));
                        }
                    }
                });

                final List<HashMap<String, Long>> orders =
                        (List<HashMap<String, Long>>) snapshot.get("orders");

                if (orders != null) {
                    for (HashMap<String, Long> order : orders) {

                        cartItems.add(new CartItem(order.get("itemId").intValue(),
                                order.get("quantity").intValue()));

                    }
                }




            }
        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    cartAdapter.notifyDataSetChanged();
                }

            }
        });


    }

    private void initClicks() {

        activeToolbar.setNavigationOnClickListener(v -> finish());

        cartLocationIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (deliveryGeoPoint != null) {

                    final Intent mapIntent = new Intent(CartInfoActivity.this, MapsActivity.class);
                    mapIntent.putExtra("mapType", MapsActivity.MAP_TYPE_MARK_LOCATION);
                    mapIntent.putExtra("deliveryLatLng",
                            new LatLng(deliveryGeoPoint.getLatitude(), deliveryGeoPoint.getLongitude()));
                    startActivity(mapIntent);

                }

            }
        });


    }


    @Override
    public Task<Void> removeCartItem(int itemId, int position) {
        return null;
    }
}