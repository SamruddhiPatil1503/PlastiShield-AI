
package com.plastishield.ai;

import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class PlasticWasteTrackerActivity extends AppCompatActivity {

    private Spinner spinnerPlasticType;
    private EditText edtCollected;
    private EditText edtReused;
    private EditText edtPaintProduced;
    private Button btnSave;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        createScreen();
    }

    private void createScreen() {

        ScrollView scrollView = new ScrollView(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(20), dp(20), dp(20));
        layout.setBackgroundColor(Color.rgb(244, 248, 245));

        scrollView.addView(layout);

        TextView title = new TextView(this);
        title.setText("♻️ Plastic Waste to Paint Tracker");
        title.setTextSize(23);
        title.setTextColor(Color.rgb(23, 107, 58));
        title.setTypeface(null, 1);
        title.setGravity(17);
        title.setPadding(0, dp(15), 0, dp(20));
        layout.addView(title);

        addLabel(layout, "Select Plastic Type");

        spinnerPlasticType = new Spinner(this);

        String[] types = {"LDPE", "HDPE"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                types
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerPlasticType.setAdapter(adapter);
        layout.addView(spinnerPlasticType);

        addLabel(layout, "Collected Plastic (grams)");

        edtCollected = addInput(
                layout, "Example: 500"
        );

        addLabel(layout, "Plastic Reused in Paint (grams)");

        edtReused = addInput(
                layout, "Example: 300"
        );

        addLabel(layout, "Paint Produced (litres)");

        edtPaintProduced = addInput(
                layout, "Example: 1"
        );

        TextView note = new TextView(this);
        note.setText(
                "Note: Enter actual measured values only. " +
                        "These records are for project tracking, not laboratory certification."
        );
        note.setTextSize(13);
        note.setTextColor(Color.DKGRAY);
        note.setPadding(0, dp(15), 0, dp(15));
        layout.addView(note);

        btnSave = new Button(this);
        btnSave.setText("Save Plastic Record");
        btnSave.setTextColor(Color.WHITE);
        btnSave.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.rgb(11, 118, 80)
                )
        );

        layout.addView(btnSave);

        btnSave.setOnClickListener(v -> saveRecord());

        setContentView(scrollView);
    }

    private void addLabel(LinearLayout layout, String text) {

        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(16);
        label.setTextColor(Color.rgb(23, 107, 58));
        label.setTypeface(null, 1);
        label.setPadding(0, dp(15), 0, dp(6));

        layout.addView(label);
    }

    private EditText addInput(
            LinearLayout layout, String hint) {

        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextSize(16);
        input.setSingleLine(true);
        input.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        layout.addView(input);

        return input;
    }

    private void saveRecord() {

        String collectedText =
                edtCollected.getText().toString().trim();

        String reusedText =
                edtReused.getText().toString().trim();

        String paintText =
                edtPaintProduced.getText().toString().trim();

        if (collectedText.isEmpty() ||
                reusedText.isEmpty() ||
                paintText.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter all values",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        double collected;
        double reused;
        double paint;

        try {
            collected = Double.parseDouble(collectedText);
            reused = Double.parseDouble(reusedText);
            paint = Double.parseDouble(paintText);

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Enter valid numeric values",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (collected <= 0 || reused < 0 || paint < 0) {

            Toast.makeText(
                    this,
                    "Enter valid positive values",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (reused > collected) {

            Toast.makeText(
                    this,
                    "Reused plastic cannot exceed collected plastic",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "User not signed in. Please reopen the app.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> record = new HashMap<>();

        record.put(
                "plasticType",
                spinnerPlasticType.getSelectedItem().toString()
        );

        record.put("collectedGrams", collected);
        record.put("reusedGrams", reused);
        record.put("paintProducedLitres", paint);
        record.put("timestamp", FieldValue.serverTimestamp());

        btnSave.setEnabled(false);

        db.collection("users")
                .document(uid)
                .collection("plasticWasteRecords")
                .add(record)
                .addOnSuccessListener(documentReference -> {

                    btnSave.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Plastic record saved successfully!",
                            Toast.LENGTH_LONG
                    ).show();

                    edtCollected.setText("");
                    edtReused.setText("");
                    edtPaintProduced.setText("");
                })
                .addOnFailureListener(e -> {

                    btnSave.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Save failed: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private int dp(int value) {
        return (int) (value *
                getResources().getDisplayMetrics().density);
    }
}
