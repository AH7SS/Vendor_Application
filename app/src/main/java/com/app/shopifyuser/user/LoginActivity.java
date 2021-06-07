package com.app.shopifyuser.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.app.shopifyuser.R;
import com.app.shopifyuser.driver.DriverActivity;
import com.app.shopifyuser.model.RigesterRequest;
import com.app.shopifyuser.shared.LocalSave;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername,etPassword;
    AppCompatButton btnLogin,btnguest;
    SweetAlertDialog sweetAlertDialog;
    FirebaseFirestore firebaseFirestore;
    CollectionReference collectionReference;
    String email,password;
    ImageView logomainIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initItems();
        initClicks();
        findViewById(R.id.tvSignup).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this,SignupActivity.class));
        });



    }

    private void initClicks() {
        btnLogin.setOnClickListener(v -> {
            doSignin();
        });
        btnguest.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this,UserActivity.class));
        });
    }

    private void doSignin() {
        email = etUsername.getText().toString();
        password = etPassword.getText().toString();
        sweetAlertDialog.show();
        collectionReference.whereEqualTo("phoneNumber",email).whereEqualTo("password",password).limit(1).get().addOnCompleteListener(command -> {
            if(command.isSuccessful()){
                if(command.getResult().getDocuments().size()>0) {
                    RigesterRequest rigesterRequest = command.getResult().getDocuments().get(0).toObject(RigesterRequest.class);
                    LocalSave.getInstance(LoginActivity.this).saveCurrentUser(rigesterRequest);

                    if (rigesterRequest.getType() == 1) {

                        startActivity(new Intent(LoginActivity.this, UserActivity.class));
                    } else {

                        startActivity(new Intent(LoginActivity.this, DriverActivity.class));

                    }


                    sweetAlertDialog.hide();
                    finishAffinity();
                }else {
                    sweetAlertDialog.hide();
                    Toast.makeText(this, "invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initItems() {
        sweetAlertDialog = new SweetAlertDialog(LoginActivity.this,SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setCancelable(false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("users");
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_phoneNumber);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_signIn);
        btnguest = findViewById(R.id.btn_loginGuest);
        logomainIv = findViewById(R.id.imageView3);
        logomainIv.setImageResource(R.drawable.logomain);
    }

    private void forgotPassword() {

    }
}