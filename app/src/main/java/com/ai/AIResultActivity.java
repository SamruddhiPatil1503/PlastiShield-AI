
package com.plastishield.ai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIResultActivity extends AppCompatActivity {

    private TextView txtCategory;
    private TextView txtItemName;
    private TextView txtRecyclability;
    private TextView txtExplanation;
    private TextView txtAdvice;

    private ProgressBar progressRecyclability;

    private String fullResult = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_result);

        txtCategory = findViewById(R.id.txtCategory);
        txtItemName = findViewById(R.id.txtItemName);
        txtRecyclability = findViewById(R.id.txtRecyclability);
        txtExplanation = findViewById(R.id.txtExplanation);
        txtAdvice = findViewById(R.id.txtAdvice);

        progressRecyclability =
                findViewById(R.id.progressRecyclability);

        Button btnShareResult =
                findViewById(R.id.btnShareResult);

        Button btnBackHome =
                findViewById(R.id.btnBackHome);

        Button btnMaterialSuitability =
                findViewById(R.id.btnMaterialSuitability);

        fullResult = getIntent().getStringExtra("ai_result");

        if (fullResult == null || fullResult.trim().isEmpty()) {
            fullResult = "No AI result available.";
        }

        displayResult(fullResult);

        btnShareResult.setOnClickListener(v -> shareResult());

        btnBackHome.setOnClickListener(v -> finish());

        btnMaterialSuitability.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AIResultActivity.this,
                    MaterialSuitabilityActivity.class
            );
            startActivity(intent);
        });
    }

    private void displayResult(String result) {

        String category = extractValue(result, "Category");
        String itemName = extractValue(result, "Item Name");

        Integer percentage =
                extractRecyclabilityPercentage(result);

        String identificationNote = extractSection(
                result,
                "Material Identification Note",
                "Explanation"
        );

        String explanation = extractSection(
                result,
                "Explanation",
                "Practical Recycling Advice"
        );

        String advice = extractSection(
                result,
                "Practical Recycling Advice",
                "PlastiShield Material Suitability"
        );

        String suitability = extractSection(
                result,
                "PlastiShield Material Suitability",
                null
        );

        // Remove unwanted material details from all sections

        identificationNote =
                removeMaterialDetails(identificationNote);

        explanation =
                removeMaterialDetails(explanation);

        advice =
                removeMaterialDetails(advice);

        suitability =
                removeMaterialDetails(suitability);

        txtCategory.setText(
                "♻️ Category: " +
                        (category.isEmpty() ? "Not identified" : category)
        );

        txtItemName.setText(
                "Item Name: " +
                        (itemName.isEmpty() ? "Not identified" : itemName)
        );

        if (percentage != null) {

            txtRecyclability.setText(
                    "Estimated Recyclability: " + percentage + "%"
            );

            progressRecyclability.setProgress(percentage);

        } else {

            txtRecyclability.setText(
                    "Estimated Recyclability: Not provided by AI"
            );

            progressRecyclability.setProgress(0);
        }

        // AI Explanation

        StringBuilder explanationDetails = new StringBuilder();

        if (!identificationNote.isEmpty()) {
            explanationDetails.append(
                    "MATERIAL IDENTIFICATION NOTE\n\n"
            );
            explanationDetails.append(identificationNote);
            explanationDetails.append("\n\n");
        }

        if (!explanation.isEmpty()) {
            explanationDetails.append("AI EXPLANATION\n\n");
            explanationDetails.append(explanation);
        } else if (identificationNote.isEmpty()) {
            explanationDetails.append(
                    removeMaterialDetails(result)
            );
        }

        txtExplanation.setText(explanationDetails.toString());

        // Recycling Advice and PlastiShield Suitability

        StringBuilder adviceDetails = new StringBuilder();

        if (!advice.isEmpty()) {
            adviceDetails.append(
                    "PRACTICAL RECYCLING ADVICE\n\n"
            );
            adviceDetails.append(advice);
        }

        if (!suitability.isEmpty()) {

            if (adviceDetails.length() > 0) {
                adviceDetails.append("\n\n");
            }

            adviceDetails.append(
                    "PLASTISHIELD MATERIAL SUITABILITY\n\n"
            );
            adviceDetails.append(suitability);
        }

        if (adviceDetails.length() == 0) {
            adviceDetails.append(
                    "Please refer to the AI explanation for recycling advice."
            );
        }

        txtAdvice.setText(adviceDetails.toString());
    }

    // Remove unwanted plastic type, purity and contamination details

    private String removeMaterialDetails(String text) {

        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder cleaned = new StringBuilder();

        String[] lines = text.split("\\r?\\n");

        for (String line : lines) {

            String value = line.trim().toLowerCase();

            if (value.startsWith("user-provided plastic type:") ||
                    value.startsWith("selected plastic type:") ||
                    value.startsWith("plastic type:") ||
                    value.startsWith("material purity:") ||
                    value.startsWith("purity:") ||
                    value.startsWith("contamination:") ||
                    value.equals("material details")) {

                continue;
            }

            cleaned.append(line).append("\n");
        }

        return cleaned.toString().trim();
    }

    private String extractValue(String text, String label) {

        String[] lines = text.split("\\r?\\n");

        for (String line : lines) {

            String cleaned = line
                    .replace("*", "")
                    .replace("#", "")
                    .trim();

            if (cleaned.toLowerCase().startsWith(
                    label.toLowerCase() + ":")) {

                return cleaned.substring(
                        cleaned.indexOf(":") + 1
                ).trim();
            }
        }

        return "";
    }

    private Integer extractRecyclabilityPercentage(String text) {

        String[] lines = text.split("\\r?\\n");

        for (String line : lines) {

            String cleaned = line
                    .replace("*", "")
                    .replace("#", "")
                    .trim();

            if (cleaned.toLowerCase().contains(
                    "estimated recyclability")) {

                Matcher matcher = Pattern.compile(
                        "(\\d{1,3})\\s*%"
                ).matcher(cleaned);

                if (matcher.find()) {

                    int value = Integer.parseInt(matcher.group(1));

                    if (value >= 0 && value <= 100) {
                        return value;
                    }
                }
            }
        }

        return null;
    }

    private String extractSection(
            String text,
            String heading,
            String nextHeading) {

        String[] lines = text.split("\\r?\\n");

        StringBuilder section = new StringBuilder();

        boolean found = false;

        for (String line : lines) {

            String cleanLine = line
                    .replace("*", "")
                    .replace("#", "")
                    .trim();

            String normalizedLine =
                    cleanLine.replaceAll(":$", "").trim();

            if (!found) {

                if (normalizedLine.equalsIgnoreCase(heading)) {
                    found = true;
                }

                continue;
            }

            if (nextHeading != null &&
                    normalizedLine.equalsIgnoreCase(nextHeading)) {
                break;
            }

            section.append(line).append("\n");
        }

        return section.toString().trim();
    }

    private void shareResult() {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        shareIntent.setType("text/plain");

        shareIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                "PlastiShield AI Waste Analysis"
        );

        shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "PlastiShield AI Result\n\n" + fullResult
        );

        startActivity(Intent.createChooser(
                shareIntent,
                "Share AI Result"
        ));
    }
}
