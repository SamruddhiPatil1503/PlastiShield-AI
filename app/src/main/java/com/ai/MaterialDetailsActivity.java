
package com.plastishield.ai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MaterialDetailsActivity extends AppCompatActivity {

    private Spinner spinnerPlasticType;
    private Spinner spinnerPurity;
    private Spinner spinnerContamination;
    private Button btnContinueAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_details);

        spinnerPlasticType = findViewById(R.id.spinnerPlasticType);
        spinnerPurity = findViewById(R.id.spinnerPurity);
        spinnerContamination = findViewById(R.id.spinnerContamination);
        btnContinueAnalysis = findViewById(R.id.btnContinueAnalysis);

        btnContinueAnalysis.setOnClickListener(v -> continueAnalysis());
    }

    private void continueAnalysis() {

        String plasticType =
                spinnerPlasticType.getSelectedItem().toString();

        String purity =
                spinnerPurity.getSelectedItem().toString();

        String contamination =
                spinnerContamination.getSelectedItem().toString();

        if (spinnerPlasticType.getSelectedItemPosition() == 0 ||
                spinnerPurity.getSelectedItemPosition() == 0 ||
                spinnerContamination.getSelectedItemPosition() == 0) {

            Toast.makeText(
                    this,
                    "Please select all material details",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent resultIntent = new Intent();

        resultIntent.putExtra("plastic_type", plasticType);
        resultIntent.putExtra("material_purity", purity);
        resultIntent.putExtra("contamination", contamination);

        setResult(RESULT_OK, resultIntent);

        finish();
    }
}
