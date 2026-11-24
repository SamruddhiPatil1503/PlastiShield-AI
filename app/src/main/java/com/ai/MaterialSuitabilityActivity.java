
package com.plastishield.ai;

import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MaterialSuitabilityActivity extends AppCompatActivity {

    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createScreen();
    }

    private void createScreen() {

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.rgb(244, 248, 245));

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 30, 24, 30);

        scrollView.addView(layout);

        TextView title = new TextView(this);
        title.setText("PlastiShield Material Suitability");
        title.setTextSize(25);
        title.setTextColor(Color.rgb(23, 107, 69));
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, 1);
        addView(title, 20);

        TextView subtitle = new TextView(this);
        subtitle.setText(
                "Explore recycled plastic materials for research " +
                        "in waterproof paint and protective coatings."
        );
        subtitle.setTextSize(15);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setGravity(Gravity.CENTER);
        addView(subtitle, 25);

        addSection(
                "1. LDPE - Low Density Polyethylene",
                "Common sources: Carry bags, plastic films and packaging.\n\n" +
                        "Research possibility: LDPE may be investigated as a recycled " +
                        "polymer component in suitable coating formulations.\n\n" +
                        "Important: Compatibility, processing temperature, adhesion " +
                        "and water resistance must be tested."
        );

        addSection(
                "2. HDPE - High Density Polyethylene",
                "Common sources: Detergent bottles, containers and rigid packaging.\n\n" +
                        "Research possibility: Recycled HDPE may be evaluated for " +
                        "protective coating applications after suitable processing.\n\n" +
                        "Important: Formulation compatibility and bonding with the " +
                        "coating system require laboratory validation."
        );

        addSection(
                "3. Other Plastic Materials",
                "PP, PET, PVC and mixed plastics have different chemical " +
                        "properties.\n\n" +
                        "They should not be mixed directly into a paint formulation " +
                        "without identifying the polymer and checking compatibility."
        );

        addSection(
                "4. Recommended Laboratory Tests",
                "• Water absorption and water resistance\n\n" +
                        "• Adhesion to concrete surface\n\n" +
                        "• Flexibility and crack resistance\n\n" +
                        "• Weather and UV resistance\n\n" +
                        "• Chemical and thermal stability"
        );

        addSection(
                "5. PlastiShield Research Note",
                "The objective of PlastiShield is to investigate the use of " +
                        "recycled LDPE and HDPE in eco-friendly waterproof paint " +
                        "and protective coating formulations.\n\n" +
                        "Material suitability cannot be confirmed from an image alone. " +
                        "Polymer identification, formulation trials and laboratory " +
                        "testing are essential before practical use."
        );

        TextView footer = new TextView(this);
        footer.setText("PlastiShield AI | Turning Plastic Waste into Home Protection");
        footer.setTextSize(13);
        footer.setTextColor(Color.GRAY);
        footer.setGravity(Gravity.CENTER);
        addView(footer, 20);

        setContentView(scrollView);
    }

    private void addSection(String heading, String description) {

        TextView headingView = new TextView(this);
        headingView.setText(heading);
        headingView.setTextSize(19);
        headingView.setTypeface(null, 1);
        headingView.setTextColor(Color.rgb(23, 107, 69));
        addView(headingView, 8);

        TextView descriptionView = new TextView(this);
        descriptionView.setText(description);
        descriptionView.setTextSize(15);
        descriptionView.setTextColor(Color.rgb(55, 55, 55));
        descriptionView.setPadding(16, 14, 16, 14);
        descriptionView.setBackgroundColor(Color.WHITE);
        addView(descriptionView, 20);
    }

    private void addView(TextView view, int bottomMargin) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = bottomMargin;
        layout.addView(view, params);
    }
}
