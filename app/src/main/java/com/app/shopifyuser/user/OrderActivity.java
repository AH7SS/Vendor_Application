package com.app.shopifyuser.user;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.shopifyuser.R;
import com.app.shopifyuser.model.Menu;
import com.app.shopifyuser.shared.LocalSave;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class OrderActivity extends AppCompatActivity {


    ImageView ivImage;
    TextView tvTitle, tvDescription, tvNumber;
    ImageButton ibAdd, ibRemove;
    Menu menu;
    private Button addToCartBtn;

    DocumentReference userRef;

    int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        initViews();
        initItems();
        initClicks();
    }

    private void initClicks() {
        ibAdd.setOnClickListener(v -> {
            quantity += 1;
            tvNumber.setText(quantity + "");
        });
        ibRemove.setOnClickListener(v -> {
            if (quantity != 1) {
                quantity -= 1;
                tvNumber.setText(quantity + "");

            }
        });

        addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog
                        (OrderActivity.this, SweetAlertDialog.PROGRESS_TYPE);

                sweetAlertDialog.setTitle("Adding to cart");
                sweetAlertDialog.show();

                if (userRef == null) {

                    userRef = FirebaseFirestore.getInstance().collection("users")
                            .document(String.valueOf(LocalSave.getInstance(OrderActivity.this)
                                    .getCurrentUser().getId())
                            );

                }

                userRef.collection("Cart").document(String.valueOf(menu.getId())).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {

                        if (!snapshot.exists()) {

                            final HashMap<String, Object> cartMap = new HashMap<>();
                            cartMap.put("id", menu.getId());
                            cartMap.put("quantity", quantity);



                            snapshot.getReference().set(cartMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    final double price = menu.getPrice() * quantity;

                                    userRef.update("cartTotal", FieldValue.increment(price))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    sweetAlertDialog.dismiss();
                                                    finish();
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    sweetAlertDialog.dismiss();
                                    Toast.makeText(OrderActivity.this,
                                            "There was an error while trying to add to your cart!" +
                                                    " Please try again", Toast.LENGTH_LONG).show();
                                    Log.d("ttt", "adding to cart error: " + e.getMessage());

                                }
                            });

                        } else {
                            snapshot.getReference().update("quantity", FieldValue.increment(quantity))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            sweetAlertDialog.dismiss();
                                            finish();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    sweetAlertDialog.dismiss();
                                    Toast.makeText(OrderActivity.this,
                                            "There was an error while trying to add to your cart!" +
                                                    " Please try again", Toast.LENGTH_LONG).show();
                                    Log.d("ttt", "updating cart item error: " + e.getMessage());

                                }
                            });
                        }

                    }
                });




            }
        });

    }

    private void initItems() {
        menu = MenuActivity.menus.get(getIntent().getIntExtra("position", 0));
        Picasso.get().load(menu.getImage()).into(ivImage);
        tvTitle.setText(menu.getName());
        tvDescription.setText(menu.getDescription());
    }


    private void initViews() {
        ivImage = findViewById(R.id.ivImage);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDesc);
        tvNumber = findViewById(R.id.tvNumber);
        ibAdd = findViewById(R.id.ibPlus);
        ibRemove = findViewById(R.id.ibRemove);
        addToCartBtn = findViewById(R.id.addToCartBtn);
    }


}