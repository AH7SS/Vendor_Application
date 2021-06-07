package com.app.shopifyuser.user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.app.shopifyuser.R;
import com.app.shopifyuser.adapters.MenuAdapter;
import com.app.shopifyuser.model.Menu;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DetailsActivity extends AppCompatActivity {


    FirebaseFirestore firebaseFirestore;
    CollectionReference collectionReference;
    SweetAlertDialog sweetAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        initViews();
        initItems();
        initClicks();
    }

    private void initClicks() {

    }

    private void initItems() {

    }

    private void initViews() {

    }
}