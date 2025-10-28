package com.grzeluu.lookupplant.view;

import static com.grzeluu.lookupplant.utils.ProgressUtils.daysToProgress;
import static com.grzeluu.lookupplant.utils.ProgressUtils.progressToDays;
import static com.grzeluu.lookupplant.utils.SeekBarUtils.initSeekBarGroupWithText;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grzeluu.lookupplant.R;
import com.grzeluu.lookupplant.base.BaseFragment;
import com.grzeluu.lookupplant.databinding.FragmentAddPlantBinding;
import com.grzeluu.lookupplant.model.UserPlant;

public class EditPlantFragment extends BaseFragment {

    FragmentAddPlantBinding binding;
    private UserPlant plantToEdit;
    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddPlantBinding.inflate(getLayoutInflater());

        init();

        return binding.getRoot();
    }


    private void init() {
        initSeekBars();

        Bundle bundle = getArguments();
        if (bundle != null) {
            plantToEdit = (UserPlant) bundle.getSerializable("USER_PLANT_EDIT");
            if (plantToEdit != null) {
                isEditMode = true;
                loadPlantData(plantToEdit);
            }
        }

        initButtons();
    }

    private void loadPlantData(UserPlant plant) {
        binding.etName.setText(plant.getName());
        binding.etName.setEnabled(false);

        binding.wateringSettings.sbFrequency.setProgress(daysToProgress(plant.getWateringFrequency()));
        binding.fertilizingSettings.sbFrequency.setProgress(daysToProgress(plant.getFertilizingFrequency()));
        binding.sprayingSettings.sbFrequency.setProgress(daysToProgress(plant.getSprayingFrequency()));

        binding.etLatinName.setVisibility(View.GONE);
        binding.etType.setVisibility(View.GONE);
        binding.etDescription.setVisibility(View.GONE);

        if (plant.getImage() != null && !plant.getImage().isEmpty()) {
            Glide.with(this)
                    .load(plant.getImage())
                    .into(binding.ivPhoto);
        }

        binding.ivPhoto.setClickable(false);

        binding.toolbar.btAddPlant.setText("Update");
    }

    private void initSeekBars() {
        initSeekBarGroupWithText(
                getContext(),
                binding.wateringSettings.sbFrequency,
                binding.wateringSettings.tvFrequency,
                binding.wateringSettings.ivPlus,
                binding.wateringSettings.ivMinus,
                10
        );
        initSeekBarGroupWithText(
                getContext(),
                binding.fertilizingSettings.sbFrequency,
                binding.fertilizingSettings.tvFrequency,
                binding.fertilizingSettings.ivPlus,
                binding.fertilizingSettings.ivMinus,
                30
        );
        initSeekBarGroupWithText(
                getContext(),
                binding.sprayingSettings.sbFrequency,
                binding.sprayingSettings.tvFrequency,
                binding.sprayingSettings.ivPlus,
                binding.sprayingSettings.ivMinus,
                0
        );
    }

    private void initButtons() {
        binding.toolbar.btAddPlant.setOnClickListener(v -> {
            if (isEditMode) {
                updatePlant();
            }
        });
    }

    private void updatePlant() {
        // Обновляем только частоту ухода
        plantToEdit.setWateringFrequency(progressToDays(binding.wateringSettings.sbFrequency.getProgress()));
        plantToEdit.setFertilizingFrequency(progressToDays(binding.fertilizingSettings.sbFrequency.getProgress()));
        plantToEdit.setSprayingFrequency(progressToDays(binding.sprayingSettings.sbFrequency.getProgress()));

        // Сохраняем в Firebase
        DatabaseReference userPlantsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("UserPlants")
                .child(plantToEdit.getId());

        userPlantsRef.setValue(plantToEdit)
                .addOnSuccessListener(task -> {
                    Toast.makeText(getContext(), "Plant updated successfully!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(getContext(), "Failed to update: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}