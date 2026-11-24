package com.plastishield.ai;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class RecyclingGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycling_guide);

        setupButton(R.id.btnPlastic, "Plastic",
                "1. Identify the plastic type.\n" +
                        "2. Empty and clean the container.\n" +
                        "3. Separate caps and labels when required.\n" +
                        "4. Give accepted plastic to an authorized recycler.");

        setupButton(R.id.btnPaper, "Paper",
                "1. Keep paper dry and clean.\n" +
                        "2. Separate cardboard and newspapers.\n" +
                        "3. Avoid mixing with food or oily waste.\n" +
                        "4. Give it to a paper recycler.");

        setupButton(R.id.btnMetal, "Metal",
                "1. Separate metal cans and scrap.\n" +
                        "2. Empty containers safely.\n" +
                        "3. Separate different metals when possible.\n" +
                        "4. Give them to an authorized scrap dealer.");

        setupButton(R.id.btnGlass, "Glass",
                "1. Separate glass bottles and jars.\n" +
                        "2. Empty and rinse them if safe.\n" +
                        "3. Handle broken glass carefully.\n" +
                        "4. Give accepted glass to a glass recycler.");

        setupButton(R.id.btnOrganic, "Organic Waste",
                "1. Separate food scraps and garden waste.\n" +
                        "2. Remove plastic and metal contamination.\n" +
                        "3. Compost suitable organic waste.\n" +
                        "4. Use compost for plants and gardens.");

        setupButton(R.id.btnEwaste, "E-Waste",
                "1. Keep electronic waste separate.\n" +
                        "2. Do not burn or dismantle batteries.\n" +
                        "3. Protect personal data on devices.\n" +
                        "4. Give e-waste to an authorized collection centre.");

        setupButton(R.id.btnOther, "Other Waste",
                "1. Identify the material first.\n" +
                        "2. Keep hazardous waste separate.\n" +
                        "3. Do not mix unknown chemicals with household waste.\n" +
                        "4. Follow local municipal disposal guidance.");
    }

    private void setupButton(int buttonId, String title, String advice) {
        Button button = findViewById(buttonId);

        button.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle(title + " Recycling Guide")
                        .setMessage(advice)
                        .setPositiveButton("OK", null)
                        .show()
        );
    }
}
