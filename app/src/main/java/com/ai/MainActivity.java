
package com.plastishield.ai;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ActivityResultLauncher<Void> cameraLauncher;

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imagePreview;
    private Bitmap selectedBitmap;
    private WasteClassifier wasteClassifier;

    private Button btnAskAI;
    private boolean isAnalyzing = false;
    private CharSequence originalAskAIText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);

        // Firebase App Check Configuration
        FirebaseAppCheck firebaseAppCheck =
                FirebaseAppCheck.getInstance();

        if (BuildConfig.DEBUG) {
            firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
            );
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
            );
        }

        // Initialize Firebase Services
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(
                                    MainActivity.this,
                                    "User authentication successful!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Authentication failed!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        }

        setContentView(R.layout.activity_main);

        imagePreview = findViewById(R.id.imagePreview);

        wasteClassifier = new WasteClassifier();

        // Camera Launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        selectedBitmap = bitmap;

                        imagePreview.setImageBitmap(bitmap);
                        imagePreview.setVisibility(View.VISIBLE);

                        Toast.makeText(
                                this,
                                "Photo captured successfully!",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        // Connect XML Buttons
        Button btnCapture = findViewById(R.id.btnCapture);
        Button btnGallery = findViewById(R.id.btnGallery);

        btnAskAI = findViewById(R.id.btnAskAI);

        Button btnRecyclingGuide =
                findViewById(R.id.btnRecyclingGuide);

        Button btnScanHistory =
                findViewById(R.id.btnScanHistory);

        Button btnMaterialSuitability =
                findViewById(R.id.btnMaterialSuitability);

        Button btnPlasticWasteTracker =
                findViewById(R.id.btnPlasticWasteTracker);

        Button btnImpactDashboard =
                findViewById(R.id.btnImpactDashboard);

        originalAskAIText = btnAskAI.getText();

        // Capture Waste Image
        btnCapture.setOnClickListener(v ->
                cameraLauncher.launch(null)
        );

        // Upload from Gallery
        btnGallery.setOnClickListener(v -> openGallery());

        // Ask AI
        btnAskAI.setOnClickListener(v -> {

            if (isAnalyzing) {
                return;
            }

            if (selectedBitmap == null) {
                Toast.makeText(
                        this,
                        "Please capture or select an image first!",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            startAiAnalysis();
        });

        // Material Suitability
        btnMaterialSuitability.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    MaterialSuitabilityActivity.class
            );
            startActivity(intent);
        });

        // Plastic Waste Tracker
        btnPlasticWasteTracker.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    PlasticWasteTrackerActivity.class
            );
            startActivity(intent);
        });

        // Impact Dashboard
        btnImpactDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    ImpactDashboardActivity.class
            );
            startActivity(intent);
        });

        // Recycling Guide
        btnRecyclingGuide.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    RecyclingGuideActivity.class
            );
            startActivity(intent);
        });

        // Scan History
        btnScanHistory.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    ScanHistoryActivity.class
            );
            startActivity(intent);
        });
    }

    // Start AI Analysis
    private void startAiAnalysis() {

        if (selectedBitmap == null) {
            Toast.makeText(
                    this,
                    "Please select an image first!",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        isAnalyzing = true;
        btnAskAI.setEnabled(false);
        btnAskAI.setText("Analyzing...");

        Toast.makeText(
                this,
                "AI is analyzing your waste image...",
                Toast.LENGTH_SHORT
        ).show();

        wasteClassifier.classifyWaste(
                selectedBitmap,
                new WasteClassifier.ResultCallback() {

                    @Override
                    public void onResult(String result) {

                        saveScanToFirestore(result);

                        finishAnalysis();

                        Intent intent = new Intent(
                                MainActivity.this,
                                AIResultActivity.class
                        );

                        intent.putExtra("ai_result", result);

                        startActivity(intent);
                    }

                    @Override
                    public void onError(String error) {

                        finishAnalysis();

                        String errorMessage = error == null
                                ? ""
                                : error.toLowerCase(Locale.ROOT);

                        String displayMessage;

                        if (errorMessage.contains("quota")
                                || errorMessage.contains("429")
                                || errorMessage.contains("resource_exhausted")) {

                            displayMessage =
                                    "Gemini API request limit reached.\n\n"
                                            + "Please wait before trying again. "
                                            + "Check your Gemini API usage and quota.";
                        } else {
                            displayMessage =
                                    "Unable to analyze the image.\n\n"
                                            + (error == null
                                            ? "Unknown error"
                                            : error);
                        }

                        new android.app.AlertDialog.Builder(
                                MainActivity.this)
                                .setTitle("AI Analysis Failed")
                                .setMessage(displayMessage)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                });
    }

    // Save Scan Result to Firestore
    private void saveScanToFirestore(String result) {

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(
                    this,
                    "Authentication not ready. Scan not saved.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> scanData = new HashMap<>();

        scanData.put("category", extractValue(result, "Category"));
        scanData.put("itemName", extractValue(result, "Item Name"));
        scanData.put("recyclability", extractPercentage(result));
        scanData.put("explanation", result);

        scanData.put(
                "advice",
                extractValue(result, "Practical Recycling Advice")
        );

        scanData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("users")
                .document(userId)
                .collection("scanHistory")
                .add(scanData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(
                            this,
                            "Scan saved to history!",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Scan history save failed: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // Extract AI Response Value
    private String extractValue(String result, String field) {

        if (result == null) {
            return "";
        }

        String[] lines = result.split("\n");

        for (String line : lines) {

            String cleanedLine = line.trim()
                    .replace("*", "")
                    .replace("#", "")
                    .trim();

            if (cleanedLine.toLowerCase(Locale.ROOT)
                    .startsWith(field.toLowerCase(Locale.ROOT) + ":")) {

                int separator = cleanedLine.indexOf(':');

                if (separator >= 0) {
                    return cleanedLine.substring(separator + 1).trim();
                }
            }
        }

        return "";
    }

    // Extract Recyclability Percentage
    private int extractPercentage(String result) {

        if (result == null) {
            return 0;
        }

        String[] lines = result.split("\n");

        for (String line : lines) {

            String cleanedLine = line.trim()
                    .replace("*", "")
                    .replace("#", "")
                    .trim();

            if (cleanedLine.toLowerCase(Locale.ROOT)
                    .contains("estimated recyclability")) {

                java.util.regex.Matcher matcher =
                        java.util.regex.Pattern.compile("(\\d{1,3})\\s*%")
                                .matcher(cleanedLine);

                if (matcher.find()) {
                    int value = Integer.parseInt(matcher.group(1));

                    if (value >= 0 && value <= 100) {
                        return value;
                    }
                }
            }
        }

        return 0;
    }

    // Finish AI Analysis
    private void finishAnalysis() {

        isAnalyzing = false;

        btnAskAI.setEnabled(true);
        btnAskAI.setText(originalAskAIText);
    }

    // Open Gallery
    private void openGallery() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        startActivityForResult(
                Intent.createChooser(intent, "Select Waste Image"),
                PICK_IMAGE_REQUEST
        );
    }

    // Handle Selected Image
    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null) {

            Uri imageUri = data.getData();

            if (imageUri != null) {

                try {

                    InputStream inputStream =
                            getContentResolver().openInputStream(imageUri);

                    if (inputStream != null) {

                        selectedBitmap =
                                BitmapFactory.decodeStream(inputStream);

                        inputStream.close();

                        if (selectedBitmap != null) {

                            imagePreview.setImageBitmap(selectedBitmap);
                            imagePreview.setVisibility(View.VISIBLE);

                            Toast.makeText(
                                    this,
                                    "Image selected successfully!",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Toast.makeText(
                                    this,
                                    "Unable to load image!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                } catch (Exception e) {

                    Toast.makeText(
                            this,
                            "Image error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        }
    }
}
