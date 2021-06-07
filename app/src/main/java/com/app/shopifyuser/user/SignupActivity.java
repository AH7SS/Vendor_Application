package com.app.shopifyuser.user;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.app.shopifyuser.R;
import com.app.shopifyuser.model.RigesterRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SignupActivity extends AppCompatActivity {

    EditText etUsername, etPhonenumber, et_email, etPassword, etConfirmPassword;
    RadioButton rbUser, rbDriver;
    RadioGroup rgType;
    TextView tvSignIn;
    AppCompatButton btnSignup;
    SweetAlertDialog sweetAlertDialog;
    String username, phone, email, password, confirmPassword;
    int type = 1;
    FirebaseFirestore firebaseFirestore;
    CollectionReference collectionReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initViews();
        initItems();
        initClicks();

    }

    private void initItems() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("users");
        sweetAlertDialog = new SweetAlertDialog(SignupActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setCancelable(false);
    }

    private void initClicks() {
        tvSignIn.setOnClickListener(v -> {
            finish();
        });
        btnSignup.setOnClickListener(v -> {
            username = etUsername.getText().toString().trim();
            phone = etPhonenumber.getText().toString().trim();
            email = et_email.getText().toString().trim();
            password = etPassword.getText().toString().trim();
            confirmPassword = etConfirmPassword.getText().toString().trim();

            if (username.equalsIgnoreCase("") || username.length() < 6) {
                etUsername.setError("Please enter valid Username");
                return;
            }
            if (phone.equalsIgnoreCase("")) {
                etPhonenumber.setError("Please enter valid phone number");
                return;
            }
            if (email.equalsIgnoreCase("")) {
                etPhonenumber.setError("Please enter a valid email");
                return;
            }
            if (password.equalsIgnoreCase("") || password.length() < 6) {
                etPassword.setError("Please enter a valid password");
            }
            if (!confirmPassword.equalsIgnoreCase(password)) {
                etConfirmPassword.setError("Enter match password");
            }
            doSignup();


        });
    }

    private void doSignup() {
        if (!sweetAlertDialog.isShowing())
            sweetAlertDialog.show();
        RigesterRequest RegisterRequest = new RigesterRequest();

        collectionReference.whereEqualTo("phoneNumber", phone).limit(1)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {

                if (snapshots == null || snapshots.isEmpty()) {

                    collectionReference.orderBy("id", Query.Direction.DESCENDING).limit(1)
                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot snapshots) {

                            int id;
                            if (snapshots == null || snapshots.isEmpty()) {
                                id = 1;
                            } else {
                                id = Integer.parseInt(snapshots.getDocuments().get(0).getId()) + 1;
                            }

                            RegisterRequest.setId(id);
                            RegisterRequest.setPhoneNumber(phone);
                            RegisterRequest.setPassword(password);
                            RegisterRequest.setUsername(username);
                            RegisterRequest.setEmail(email);
                            RegisterRequest.setType(rbUser.isChecked() ? 1 : 2);
                            collectionReference.document(id + "").set(RegisterRequest)
                                    .addOnCompleteListener(command2 -> {
                                        if (command2.isSuccessful()) {
                                            sweetAlertDialog.hide();
                                            finish();
                                        }
                                    });

                        }
                    });

                } else {
                    etPhonenumber.setError("Phone number already exists");
                    if (sweetAlertDialog.isShowing())
                        sweetAlertDialog.hide();
                }

            }
        });

    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPhonenumber = findViewById(R.id.et_phoneNumber);
        et_email = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirmPassword);
        rbUser = findViewById(R.id.rbUser);
        rbDriver = findViewById(R.id.rbDriver);
        rgType = findViewById(R.id.rgSignin);
        tvSignIn = findViewById(R.id.tv_signIn);
        btnSignup = findViewById(R.id.btn_signUp);

    }
}