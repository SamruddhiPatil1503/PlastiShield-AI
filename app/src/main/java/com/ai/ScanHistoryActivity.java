
package com.plastishield.ai;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScanHistoryActivity extends AppCompatActivity {

    private LinearLayout historyContainer;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        createLayout();
        loadScanHistory();
    }

    private void createLayout() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF4F8F5);
        root.setPadding(20, 20, 20, 20);

        TextView heading = new TextView(this);
        heading.setText("Scan History");
        heading.setTextSize(25);
        heading.setTextColor(0xFF1B5E20);
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(10, 20, 10, 20);

        root.addView(heading);

        progressBar = new ProgressBar(this);
        root.addView(progressBar);

        ScrollView scrollView = new ScrollView(this);

        historyContainer = new LinearLayout(this);
        historyContainer.setOrientation(LinearLayout.VERTICAL);

        scrollView.addView(historyContainer);

        root.addView(
                scrollView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        setContentView(root);
    }

    private void loadScanHistory() {

        if (auth.getCurrentUser() == null) {

            progressBar.setVisibility(View.GONE);
            showMessage("Please login first.");
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("scanHistory")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    progressBar.setVisibility(View.GONE);
                    historyContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        showMessage("No scan history available.");
                        return;
                    }

                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        addHistoryCard(document);
                    }

                })
                .addOnFailureListener(e -> {

                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(
                            ScanHistoryActivity.this,
                            "Unable to load history: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void addHistoryCard(DocumentSnapshot document) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18, 18, 18, 18);
        card.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 10, 0, 15);
        card.setLayoutParams(cardParams);

        String category = document.getString("category");
        String itemName = document.getString("itemName");
        String explanation = document.getString("explanation");
        String advice = document.getString("advice");

        Object recyclabilityValue = document.get("recyclability");
        String recyclability = "Not available";

        if (recyclabilityValue instanceof Number) {
            recyclability =
                    ((Number) recyclabilityValue).intValue() + "%";
        } else if (recyclabilityValue != null) {
            recyclability = recyclabilityValue.toString();

            if (!recyclability.contains("%")) {
                recyclability += "%";
            }
        }

        addText(card, "Item: " + safe(itemName),
                19, 0xFF1B5E20);

        addText(card, "Category: " + safe(category),
                15, 0xFF333333);

        addText(card, "Recyclability: " + recyclability,
                15, 0xFF333333);

        addText(card, "Explanation: " + safe(explanation),
                14, 0xFF555555);

        addText(card, "Advice: " + safe(advice),
                14, 0xFF555555);

        Timestamp timestamp = document.getTimestamp("timestamp");

        if (timestamp != null) {

            Date date = timestamp.toDate();

            SimpleDateFormat formatter =
                    new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                    );

            addText(card, "Date: " + formatter.format(date),
                    13, 0xFF777777);
        }

        historyContainer.addView(card);
    }

    private void addText(
            LinearLayout parent,
            String value,
            int size,
            int color
    ) {

        TextView textView = new TextView(this);

        textView.setText(value);
        textView.setTextSize(size);
        textView.setTextColor(color);
        textView.setPadding(5, 7, 5, 7);

        parent.addView(textView);
    }

    private String safe(String value) {

        return value == null || value.trim().isEmpty()
                ? "Not available"
                : value;
    }

    private void showMessage(String message) {

        TextView textView = new TextView(this);

        textView.setText(message);
        textView.setTextSize(16);
        textView.setTextColor(0xFF555555);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(20, 30, 20, 30);

        historyContainer.addView(textView);
    }
}
