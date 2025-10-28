package com.grzeluu.lookupplant.view;

import static com.grzeluu.lookupplant.utils.Constants.PERMISSION_CAMERA;
import static com.grzeluu.lookupplant.utils.Constants.PERMISSION_STORAGE;
import static com.grzeluu.lookupplant.utils.Constants.PICK_IMAGE_CAMERA;
import static com.grzeluu.lookupplant.utils.Constants.PICK_IMAGE_GALLERY;
import static com.grzeluu.lookupplant.utils.Constants.PLANT_INTENT_EXTRAS_KEY;
import static com.grzeluu.lookupplant.utils.ProgressUtils.daysToProgress;
import static com.grzeluu.lookupplant.utils.ProgressUtils.progressToDays;
import static com.grzeluu.lookupplant.utils.SeekBarUtils.initSeekBarGroupWithText;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grzeluu.lookupplant.R;
import com.grzeluu.lookupplant.base.BaseActivity;
import com.grzeluu.lookupplant.databinding.FragmentAddPlantBinding;
import com.grzeluu.lookupplant.model.Plant;
import com.grzeluu.lookupplant.utils.SupabaseStorageHelper;

import java.util.Locale;

public class EditPublicPlantActivity extends BaseActivity {

    FragmentAddPlantBinding binding;
    private Plant plantToEdit;
    private Uri photoURI;
    private String newImageUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentAddPlantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        plantToEdit = (Plant) getIntent().getSerializableExtra(PLANT_INTENT_EXTRAS_KEY);

        if (plantToEdit == null) {
            Toast.makeText(this, "Error loading plant data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        init();
    }

    private void init() {
        initSeekBars();
        loadPlantData();
        initButtons();
    }

    @SuppressLint("SetTextI18n")
    private void loadPlantData() {
        binding.etName.setText(plantToEdit.getCommonName());
        binding.etLatinName.setText(plantToEdit.getLatinName());
        binding.etType.setText(plantToEdit.getType());
        binding.etDescription.setText(plantToEdit.getDescription());

        binding.wateringSettings.sbFrequency.setProgress(daysToProgress(plantToEdit.getWateringFrequency()));
        binding.fertilizingSettings.sbFrequency.setProgress(daysToProgress(plantToEdit.getFertilizingFrequency()));
        binding.sprayingSettings.sbFrequency.setProgress(daysToProgress(plantToEdit.getSprayingFrequency()));

        if (plantToEdit.getImage() != null && !plantToEdit.getImage().isEmpty()) {
            Glide.with(this)
                    .load(plantToEdit.getImage())
                    .into(binding.ivPhoto);
        }

        binding.toolbar.btAddPlant.setText("Update");
    }

    private void initSeekBars() {
        initSeekBarGroupWithText(
                this,
                binding.wateringSettings.sbFrequency,
                binding.wateringSettings.tvFrequency,
                binding.wateringSettings.ivPlus,
                binding.wateringSettings.ivMinus,
                10
        );
        initSeekBarGroupWithText(
                this,
                binding.fertilizingSettings.sbFrequency,
                binding.fertilizingSettings.tvFrequency,
                binding.fertilizingSettings.ivPlus,
                binding.fertilizingSettings.ivMinus,
                30
        );
        initSeekBarGroupWithText(
                this,
                binding.sprayingSettings.sbFrequency,
                binding.sprayingSettings.tvFrequency,
                binding.sprayingSettings.ivPlus,
                binding.sprayingSettings.ivMinus,
                0
        );
    }

    private void initButtons() {
        binding.ivPhoto.setOnClickListener(v -> showChoosePhotoDialog());

        binding.toolbar.btAddPlant.setOnClickListener(v -> {
            if (photoURI != null) {
                uploadNewPhoto();
            } else {
                updatePlant();
            }
        });
    }

    private void uploadNewPhoto() {
        String fileName = plantToEdit.getId() + "_" + System.currentTimeMillis() + ".jpg";

        SupabaseStorageHelper.INSTANCE.uploadImage(
                this,
                photoURI,
                fileName,
                new SupabaseStorageHelper.UploadCallback() {
                    @Override
                    public void onSuccess(String publicUrl) {
                        runOnUiThread(() -> {
                            newImageUrl = publicUrl;
                            updatePlant();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(EditPublicPlantActivity.this,
                                    "Failed to upload image: " + error,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    private void updatePlant() {
        plantToEdit.setCommonName(binding.etName.getText().toString());
        plantToEdit.setLatinName(binding.etLatinName.getText().toString());
        plantToEdit.setType(binding.etType.getText().toString());
        plantToEdit.setDescription(binding.etDescription.getText().toString());
        plantToEdit.setWateringFrequency(progressToDays(binding.wateringSettings.sbFrequency.getProgress()));
        plantToEdit.setFertilizingFrequency(progressToDays(binding.fertilizingSettings.sbFrequency.getProgress()));
        plantToEdit.setSprayingFrequency(progressToDays(binding.sprayingSettings.sbFrequency.getProgress()));

        if (newImageUrl != null) {
            plantToEdit.setImage(newImageUrl);
        }

        DatabaseReference plantRef = FirebaseDatabase.getInstance()
                .getReference("Plants")
                .child(plantToEdit.getId());

        plantRef.setValue(plantToEdit)
                .addOnSuccessListener(task -> {
                    Toast.makeText(this, "Plant updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(this, "Failed to update: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });

        if (plantToEdit.getOriginalLanguage() == null) {
            plantToEdit.setOriginalLanguage(Locale.getDefault().getLanguage());
        }

    }

    private void showChoosePhotoDialog() {
        final CharSequence[] options = getResources().getStringArray(R.array.photo_options);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_photo);

        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals(getString(R.string.take_photo))) {
                tryPickPhotoFromCamera();
            } else if (options[item].equals(getString(R.string.gallery))) {
                tryPickPhotoFromGallery();
            } else if (options[item].equals(getString(R.string.cancel))) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void tryPickPhotoFromCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
        } else {
            pickPhotoFromCamera();
        }
    }

    private void tryPickPhotoFromGallery() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
        } else {
            pickPhotoFromGallery();
        }
    }

    private void pickPhotoFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "new picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "from camera");
        photoURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(cameraIntent, PICK_IMAGE_CAMERA);
    }

    private void pickPhotoFromGallery() {
        Intent storageIntent = new Intent(Intent.ACTION_PICK);
        storageIntent.setType("image/*");
        startActivityForResult(storageIntent, PICK_IMAGE_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickPhotoFromCamera();
            } else {
                Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickPhotoFromGallery();
            } else {
                Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_GALLERY && data != null) {
                photoURI = data.getData();
                binding.ivPhoto.setImageURI(photoURI);
            } else if (requestCode == PICK_IMAGE_CAMERA) {
                binding.ivPhoto.setImageURI(photoURI);
            }
        }
    }
}