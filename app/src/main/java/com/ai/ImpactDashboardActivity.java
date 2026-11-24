
package com.plastishield.ai;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ImpactDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private TextView collectedValue;
    private TextView reusedValue;
    private TextView paintValue;
    private TextView ldpeValue;
    private TextView hdpeValue;
    private TextView recordsValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect XML Layout
        setContentView(R.layout.activity_impact_dashboard);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Connect XML TextViews
        collectedValue = findViewById(R.id.collectedValue);
        reusedValue = findViewById(R.id.reusedValue);
        paintValue = findViewById(R.id.paintValue);
        ldpeValue = findViewById(R.id.ldpeValue);
        hdpeValue = findViewById(R.id.hdpeValue);
        recordsValue = findViewById(R.id.recordsValue);

        // Load Actual Impact Data
        loadImpactData();
    }

    private void loadImpactData() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "User not signed in. Please reopen the app.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("plasticWasteRecords")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    double totalCollected = 0;
                    double totalReused = 0;
                    double totalPaint = 0;

                    int ldpeCount = 0;
                    int hdpeCount = 0;
                    int totalRecords = 0;

                    for (QueryDocumentSnapshot document :
                            querySnapshot) {

                        Double collected =
                                document.getDouble("collectedGrams");

                        Double reused =
                                document.getDouble("reusedGrams");

                        Double paint =
                                document.getDouble("paintProducedLitres");

                        String plasticType =
                                document.getString("plasticType");

                        if (collected != null) {
                            totalCollected += collected;
                        }

                        if (reused != null) {
                            totalReused += reused;
                        }

                        if (paint != null) {
                            totalPaint += paint;
                        }

                        if (plasticType != null) {

                            if (plasticType.equalsIgnoreCase("LDPE")) {
                                ldpeCount++;

                            } else if (
                                    plasticType.equalsIgnoreCase("HDPE")) {
                                hdpeCount++;
                            }
                        }

                        totalRecords++;
                    }

                    // Total Collected Plastic in KG
                    collectedValue.setText(
                            String.format(
                                    java.util.Locale.US,
                                    "%.3f kg",
                                    totalCollected / 1000.0
                            )
                    );

                    // Total Reused Plastic in KG
                    reusedValue.setText(
                            String.format(
                                    java.util.Locale.US,
                                    "%.3f kg",
                                    totalReused / 1000.0
                            )
                    );

                    // Total Paint Produced in Litres
                    paintValue.setText(
                            String.format(
                                    java.util.Locale.US,
                                    "%.2f Litres",
                                    totalPaint
                            )
                    );

                    // LDPE Records
                    ldpeValue.setText(
                            ldpeCount + " records"
                    );

                    // HDPE Records
                    hdpeValue.setText(
                            hdpeCount + " records"
                    );

                    // Total Records
                    recordsValue.setText(
                            String.valueOf(totalRecords)
                    );

                    if (totalRecords == 0) {

                        Toast.makeText(
                                ImpactDashboardActivity.this,
                                "No plastic records found yet.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            ImpactDashboardActivity.this,
                            "Dashboard loading failed: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}
