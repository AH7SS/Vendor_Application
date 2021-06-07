package com.app.shopifyuser.user;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.shopifyuser.R;
import com.app.shopifyuser.model.Report;
import com.app.shopifyuser.shared.LocalSave;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ReportProblemsActivity extends AppCompatActivity {



    private Toolbar reportToolbar;
    private EditText titleEd, descriptionEd;
    private Button sendReportBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_problems);

        initViews();
        initItems();
        initClicks();


    }

    private void initViews() {
        reportToolbar = findViewById(R.id.reportToolbar);
        titleEd = findViewById(R.id.titleEd);
        descriptionEd = findViewById(R.id.descriptionEd);
        sendReportBtn = findViewById(R.id.sendReportBtn);
    }


    private void initItems() {

    }


    private void initClicks() {

        reportToolbar.setNavigationOnClickListener(v -> finish());

        sendReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String title = titleEd.getText().toString().trim();
                final String description = descriptionEd.getText().toString().trim();


                if (title.isEmpty()) {
                    Toast.makeText(ReportProblemsActivity.this,
                            "Please fill in the title!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (description.isEmpty()) {
                    Toast.makeText(ReportProblemsActivity.this,
                            "Please fill in the description!", Toast.LENGTH_SHORT).show();
                    return;
                }


                final SweetAlertDialog sweetAlertDialog =
                        new SweetAlertDialog(ReportProblemsActivity.this, SweetAlertDialog.PROGRESS_TYPE);

                sweetAlertDialog.setTitle("Sending report!");
                sweetAlertDialog.setCancelable(false);
                sweetAlertDialog.show();


                final String reportId = UUID.randomUUID().toString();

                final Report report = new Report(reportId, title, description,
                        String.valueOf(LocalSave.getInstance(ReportProblemsActivity.this).getCurrentUser().getId()),
                        System.currentTimeMillis());

                FirebaseFirestore.getInstance().collection("Reports")
                        .document(reportId).set(report)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                sweetAlertDialog.dismiss();

                                if (task.isSuccessful()) {

                                    Toast.makeText(ReportProblemsActivity.this,
                                            "Your report was sent", Toast.LENGTH_SHORT).show();

                                    titleEd.setText("");
                                    descriptionEd.setText("");

                                } else {

                                    Toast.makeText(ReportProblemsActivity.this,
                                            "Failed to send report!" +
                                                    " Please try again", Toast.LENGTH_SHORT).show();


                                }
                            }
                        });

            }
        });

    }

}