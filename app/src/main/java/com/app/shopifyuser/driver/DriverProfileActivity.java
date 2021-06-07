package com.app.shopifyuser.driver;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.shopifyuser.R;
import com.app.shopifyuser.shared.LocalSave;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DriverProfileActivity extends AppCompatActivity {



    private Toolbar profileToolbar;
    private EditText et_username, et_phoneNumber, etEmail, et_password;
    private Button saveBtn;


    private DocumentReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        initViews();
        initItems();
        initClicks();


    }

    private void initViews() {

        profileToolbar = findViewById(R.id.profileToolbar);
        et_username = findViewById(R.id.et_username);
        et_phoneNumber = findViewById(R.id.et_phoneNumber);
        etEmail = findViewById(R.id.etEmail);
        et_password = findViewById(R.id.et_password);
        saveBtn = findViewById(R.id.saveBtn);
    }

    private void initClicks() {

        profileToolbar.setNavigationOnClickListener(v -> finish());

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String newUsername = et_username.getText().toString().trim();
                final String newPhoneNumber = et_phoneNumber.getText().toString().trim();
                final String newEmail = etEmail.getText().toString().trim();
                final String newPassword = et_password.getText().toString().trim();

                if (newUsername.isEmpty()) {
                    Toast.makeText(DriverProfileActivity.this,
                            "You need to enter a username", Toast.LENGTH_SHORT).show();

                    return;
                }

                if (newPhoneNumber.isEmpty()) {
                    Toast.makeText(DriverProfileActivity.this,
                            "You need to enter a phone number", Toast.LENGTH_SHORT).show();

                    return;
                }

                if (newPassword.isEmpty()) {
                    Toast.makeText(DriverProfileActivity.this,
                            "You need to enter a password", Toast.LENGTH_SHORT).show();

                    return;
                }

                final SweetAlertDialog sweetAlertDialog =
                        new SweetAlertDialog(DriverProfileActivity.this,
                                SweetAlertDialog.PROGRESS_TYPE);

                sweetAlertDialog.setTitle("Updating driver info!");
                sweetAlertDialog.setCancelable(false);
                sweetAlertDialog.show();


                userRef.update("username", newUsername,
                        "phoneNumber", newPhoneNumber,
                        "password", newPassword,
                        "email", newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        sweetAlertDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(DriverProfileActivity.this,
                                "An error occured while trying to update your driver info!" +
                                        " Please try again", Toast.LENGTH_SHORT).show();
                        sweetAlertDialog.dismiss();
                    }
                });
            }
        });
    }

    private void initItems() {

        userRef = FirebaseFirestore.getInstance().collection("users")
                .document(String.valueOf(LocalSave.getInstance(this).getCurrentUser().getId()));

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {

                if (snapshot.exists()) {

                    et_username.setText(snapshot.getString("username"));
                    et_phoneNumber.setText(snapshot.getString("phoneNumber"));
                    etEmail.setText(snapshot.getString("email"));
                    et_password.setText(snapshot.getString("password"));


                } else {

                    saveBtn.setClickable(false);
                }


            }
        });

    }

}