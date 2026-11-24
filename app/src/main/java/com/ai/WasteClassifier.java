
package com.plastishield.ai;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;

public class WasteClassifier {

    private final GenerativeModelFutures model;

    public interface ResultCallback {
        void onResult(String result);
        void onError(String error);
    }

    public WasteClassifier() {

        GenerativeModel firebaseAI =
                FirebaseAI.getInstance(GenerativeBackend.googleAI())
                        .generativeModel("gemini-3.5-flash-lite");

        model = GenerativeModelFutures.from(firebaseAI);
    }

    // Existing method - preserved
    public void classifyWaste(Bitmap bitmap) {

        classifyWaste(bitmap, new ResultCallback() {

            @Override
            public void onResult(String result) {
                Log.d("PlastiShieldAI", result);
            }

            @Override
            public void onError(String error) {
                Log.e("PlastiShieldAI", error);
            }
        });
    }

    // Existing method - preserved for compatibility
    public void classifyWaste(Bitmap bitmap, ResultCallback callback) {

        classifyWaste(
                bitmap,
                "Unknown",
                "Unknown",
                "Unknown",
                callback
        );
    }

    // New method - Image + Material Details
    public void classifyWaste(
            Bitmap bitmap,
            String plasticType,
            String purity,
            String contamination,
            ResultCallback callback) {

        if (bitmap == null) {
            callback.onError(
                    "Image is empty. Please select an image."
            );
            return;
        }

        if (plasticType == null) plasticType = "Unknown";
        if (purity == null) purity = "Unknown";
        if (contamination == null) contamination = "Unknown";

        String materialDetails =
                "USER-PROVIDED MATERIAL DETAILS:\n" +
                        "Plastic Type: " + plasticType + "\n" +
                        "Material Purity: " + purity + "\n" +
                        "Contamination: " + contamination + "\n\n";

        String instructions =
                "Analyze the waste item shown in this image carefully.\n\n" +

                        materialDetails +

                        "IMPORTANT ABOUT USER-PROVIDED DETAILS:\n" +
                        "1. Treat the selected material details as information provided by the user, not laboratory-verified facts.\n" +
                        "2. Compare the selected plastic type with visible image evidence.\n" +
                        "3. If the selected type cannot be visually confirmed, clearly mention that it is user-provided and unverified.\n" +
                        "4. Do not claim that image analysis can scientifically confirm polymer identity or purity.\n" +
                        "5. Consider purity and contamination when explaining recycling suitability.\n\n" +

                        "ANALYSIS INSTRUCTIONS:\n" +
                        "1. Choose exactly one category from: Plastic, Paper, Metal, Glass, Organic, E-waste, Other.\n" +
                        "2. Identify the visible item's name.\n" +
                        "3. Provide an estimated recyclability percentage as ONE integer from 0 to 100, followed by %.\n" +
                        "4. This percentage must be an approximate AI assessment, not a laboratory-tested or verified value.\n" +
                        "5. Consider the image, user-provided plastic type, purity and contamination.\n" +
                        "6. Explain the reason for the estimated percentage.\n" +
                        "7. Give practical recycling advice.\n" +
                        "8. If the image is unclear or the material cannot be assessed reliably, write 'Not provided by AI' for the percentage. Do not invent certainty.\n" +
                        "9. If the image conflicts with the user's selected plastic type, clearly mention the mismatch instead of silently accepting the selected type.\n\n" +

                        "PLASTISHIELD MATERIAL SUITABILITY:\n" +
                        "10. Briefly comment on whether the material may be worth further laboratory evaluation for recycled-plastic waterproof paint.\n" +
                        "11. Do not claim that the material is suitable for paint based on image analysis alone.\n" +
                        "12. Mention that polymer identification, compatibility, adhesion, water resistance and durability require appropriate testing.\n\n" +

                        "IMPORTANT OUTPUT FORMAT:\n" +
                        "Category: [one category]\n" +
                        "Item Name: [visible item name]\n" +
                        "Estimated Recyclability: [integer]% or Not provided by AI\n" +
                        "User-Provided Plastic Type: [selected type]\n" +
                        "Material Purity: [selected purity]\n" +
                        "Contamination: [selected contamination]\n" +
                        "Material Identification Note: [confirmation limitation or visible mismatch]\n" +
                        "Explanation: [brief explanation]\n" +
                        "Practical Recycling Advice: [practical advice]\n" +
                        "PlastiShield Material Suitability: [preliminary guidance and testing required]\n\n" +

                        "Do not invent laboratory results, verified percentages or material properties.";

        Content content = new Content.Builder()
                .addImage(bitmap)
                .addText(instructions)
                .build();

        ListenableFuture<GenerateContentResponse> future =
                model.generateContent(content);

        Futures.addCallback(
                future,
                new FutureCallback<GenerateContentResponse>() {

                    @Override
                    public void onSuccess(GenerateContentResponse result) {

                        String aiResponse = result.getText();

                        new Handler(Looper.getMainLooper()).post(() -> {

                            if (aiResponse == null ||
                                    aiResponse.trim().isEmpty()) {

                                callback.onError(
                                        "AI returned an empty response. Please try again."
                                );

                            } else {
                                callback.onResult(aiResponse);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable error) {

                        Log.e(
                                "PlastiShieldAI",
                                "AI analysis failed",
                                error
                        );

                        new Handler(Looper.getMainLooper()).post(() -> {

                            String message = error.getMessage();

                            if (message == null ||
                                    message.trim().isEmpty()) {

                                message =
                                        "Unknown AI error. Please try again.";
                            }

                            callback.onError(message);
                        });
                    }
                },
                MoreExecutors.directExecutor()
        );
    }
}
