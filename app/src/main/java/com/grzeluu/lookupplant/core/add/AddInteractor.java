package com.grzeluu.lookupplant.core.add;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grzeluu.lookupplant.R;
import com.grzeluu.lookupplant.model.Plant;
import com.grzeluu.lookupplant.model.UserPlant;
import com.grzeluu.lookupplant.utils.SupabaseStorageHelper;

import java.util.Locale;

public class AddInteractor implements AddContract.Interactor {

    AddContract.Listener addListener;
    Context context;

    public AddInteractor(AddContract.Listener addListener, Context context) {
        this.addListener = addListener;
        this.context = context;
    }

    private String latinName;
    private String type;
    private String description;

    @Override
    public void performAddPlant(UserPlant plant, String latinName, String type, String description) {
        this.latinName = latinName;
        this.type = type;
        this.description = description;

        addListener.onStart();
        if (plant.getImage() != null) {
            addPlantWithImage(plant);
        } else {
            addPlant(plant);
        }
    }

    private void addPlantWithImage(UserPlant plant) {
        String fileName = plant.getId() + ".jpg";

        SupabaseStorageHelper.INSTANCE.uploadImage(
                context,
                Uri.parse(plant.getImage()),
                fileName,
                new SupabaseStorageHelper.UploadCallback() {
                    @Override
                    public void onSuccess(String publicUrl) {
                        ((Activity) context).runOnUiThread(() -> {
                            plant.setImage(publicUrl);
                            addPlant(plant);
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        ((Activity) context).runOnUiThread(() -> {
                            addListener.onEnd();
                            addListener.onFailure(error);
                        });
                    }
                }
        );
    }

    private void addPlant(UserPlant plant) {
        DatabaseReference userPlantsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("UserPlants");

        userPlantsRef.child(plant.getId()).setValue(plant)
                .addOnSuccessListener(task -> {
                    addToPublicPlants(plant);
                })
                .addOnFailureListener(error -> {
                    addListener.onEnd();
                    addListener.onFailure(error.getMessage());
                });
    }

    private void addToPublicPlants(UserPlant userPlant) {
        DatabaseReference publicPlantsRef = FirebaseDatabase.getInstance()
                .getReference("Plants");

        Plant publicPlant = new Plant();
        publicPlant.setId(userPlant.getId());
        publicPlant.setCommonName(userPlant.getName());
        publicPlant.setLatinName(latinName);
        publicPlant.setType(type);
        publicPlant.setDescription(description);
        publicPlant.setWateringFrequency(userPlant.getWateringFrequency());
        publicPlant.setFertilizingFrequency(userPlant.getFertilizingFrequency());
        publicPlant.setSprayingFrequency(userPlant.getSprayingFrequency());
        publicPlant.setImage(userPlant.getImage());
        publicPlant.setVerified(false);

        publicPlantsRef.child(userPlant.getId()).setValue(publicPlant)
                .addOnSuccessListener(task -> {
                    addListener.onEnd();
                    addListener.onSuccess(R.string.db_plant_added, userPlant);
                })
                .addOnFailureListener(error -> {
                    addListener.onEnd();
                    addListener.onSuccess(R.string.db_plant_added, userPlant);
                });

        publicPlant.setOriginalLanguage(Locale.getDefault().getLanguage());

    }
}