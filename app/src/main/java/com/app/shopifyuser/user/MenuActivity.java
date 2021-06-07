package com.app.shopifyuser.user;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.shopifyuser.R;
import com.app.shopifyuser.adapters.MenuAdapter;
import com.app.shopifyuser.model.Menu;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MenuActivity extends AppCompatActivity {
    RecyclerView rvMenu;
    public static ArrayList<Menu> menus = new ArrayList<>();
    FirebaseFirestore firebaseFirestore;
    CollectionReference collectionReference;
    MenuAdapter menuAdapter;
    SweetAlertDialog sweetAlertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        initViews();
        initItems();
        initClicks();
    }

    private void initClicks() {

        sweetAlertDialog.show();


        collectionReference.whereEqualTo("resturant_id",getIntent().getIntExtra("id",0)).get().addOnCompleteListener(command -> {
            menus.clear();
            sweetAlertDialog.hide();
            Log.v("ttt",command.getResult().size()+"");
            if(command.isSuccessful()){
                for(DocumentSnapshot d : command.getResult().getDocuments()){
                    menus.add(d.toObject(Menu.class));
                }
                menuAdapter.notifyDataSetChanged();
            }
        });


    }

    private void initItems() {
        sweetAlertDialog = new SweetAlertDialog(MenuActivity.this,SweetAlertDialog.PROGRESS_TYPE);
        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("menus");
        menuAdapter = new MenuAdapter(MenuActivity.this,menus);
        rvMenu.setLayoutManager(new LinearLayoutManager(MenuActivity.this));
        rvMenu.setAdapter(menuAdapter);
    }

    private void initViews() {

        rvMenu = findViewById(R.id.rvMenu);
    }


}