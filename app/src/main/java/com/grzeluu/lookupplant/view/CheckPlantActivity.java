package com.grzeluu.lookupplant.view;

import static com.grzeluu.lookupplant.utils.Constants.PLANT_INTENT_EXTRAS_KEY;
import static com.grzeluu.lookupplant.utils.FirebaseConstants.FIREBASE_IMAGE_REFERENCE;
import static com.grzeluu.lookupplant.utils.ProgressUtils.getProgressBarFill;
import static com.grzeluu.lookupplant.utils.TimeUtils.getCurrentDate;
import static com.grzeluu.lookupplant.utils.notification.NotificationUtils.scheduleNotificationForPlant;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.grzeluu.lookupplant.R;
import com.grzeluu.lookupplant.base.BaseActivity;
import com.grzeluu.lookupplant.core.check.CheckContract;
import com.grzeluu.lookupplant.core.check.CheckPresenter;
import com.grzeluu.lookupplant.databinding.ActivityCheckPlantBinding;
import com.grzeluu.lookupplant.model.Advice;
import com.grzeluu.lookupplant.model.Plant;
import com.grzeluu.lookupplant.model.UserPlant;
import com.grzeluu.lookupplant.utils.TranslationHelper;
import com.grzeluu.lookupplant.view.adapter.AdviceAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

import kotlin.Unit;

public class CheckPlantActivity extends BaseActivity implements CheckContract.View {

    private ActivityCheckPlantBinding binding;
    private CheckContract.Presenter presenter;
    private Plant plant;
    private LinearLayoutManager advicesLayoutManager;
    private AdviceAdapter checkPlantAdapter;
    private DatabaseReference plantRef;
    private ValueEventListener plantListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCheckPlantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.btBack.setOnClickListener(v -> finish());

        plant = (Plant) getIntent().getSerializableExtra(PLANT_INTENT_EXTRAS_KEY);
        presenter = new CheckPresenter(this);

        init();

        binding.btEditPlant.setOnClickListener(v -> {
            if (plant != null)
                openEditPublicPlant();
        });

        binding.btAddPlant.setOnClickListener(v -> openAddActivity());

        binding.btAddAdvice.setOnClickListener(v -> showAddAdviceDialog());
    }

    private void init() {
        setPlantPhoto();

        if (plant != null) {
            applyPlantWithTranslation(plant);
            initAdapter();

            initWateringFrequency(plant.getWateringFrequency());
            initFertilizingFrequency(plant.getFertilizingFrequency());
            initSprayingFrequency(plant.getSprayingFrequency());

            plantRef = FirebaseDatabase.getInstance().getReference("Plants").child(plant.getId());
            plantListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Plant updated = snapshot.getValue(Plant.class);
                    if (updated != null) {
                        plant = updated;
                        runOnUiThread(() -> {
                            applyPlantWithTranslation(plant);
                            refreshAdvices();
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("CheckPlantActivity", "Firebase listener error", error.toException());
                }
            };
            plantRef.addValueEventListener(plantListener);
        }
    }

    private void applyPlantWithTranslation(Plant plant) {
        String targetLang = Locale.getDefault().getLanguage();
        String srcLang = (plant.getOriginalLanguage() != null && !plant.getOriginalLanguage().isEmpty())
                ? plant.getOriginalLanguage()
                : "en";

        if (srcLang.equals(targetLang)) {
            binding.tvCommonName.setText(plant.getCommonName() != null ? plant.getCommonName() : "");
            binding.tvLatinName.setText(plant.getLatinName() != null ? plant.getLatinName() : "");
            binding.tvDescription.setText(plant.getDescription() != null ? plant.getDescription() : "");

            setPlantType(plant.getType());
            return;
        }

        String common = plant.getCommonName() == null ? "" : plant.getCommonName();
        binding.tvCommonName.setText(getString(R.string.translating));
        TranslationHelper.translate(this, srcLang, targetLang, common, translated ->
                runOnUiThread(() -> binding.tvCommonName.setText(translated))
        );

        binding.tvLatinName.setText(plant.getLatinName() != null ? plant.getLatinName() : "");

        String desc = plant.getDescription() == null ? "" : plant.getDescription();
        binding.tvDescription.setText(getString(R.string.translating));
        TranslationHelper.translate(this, srcLang, targetLang, desc, translated ->
                runOnUiThread(() -> binding.tvDescription.setText(translated))
        );

        setPlantType(plant.getType());
    }

    private void setPlantType(String type) {
        if (type == null || type.isEmpty()) {
            binding.tvCategory.setVisibility(View.GONE);
            return;
        }

        binding.tvCategory.setVisibility(View.VISIBLE);

        switch (type) {
            case "Leaf":
                binding.tvCategory.setText(R.string.leaf_plants);
                break;
            case "Flower":
                binding.tvCategory.setText(R.string.flowers);
                break;
            case "Succulent":
                binding.tvCategory.setText(R.string.succulents);
                break;
            default:
                binding.tvCategory.setText(type);
                break;
        }
    }


    private void showAddAdviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_advice, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText etQuestion = dialogView.findViewById(R.id.et_question);
        EditText etAnswer = dialogView.findViewById(R.id.et_answer);
        Button btCancel = dialogView.findViewById(R.id.bt_cancel);
        Button btSubmit = dialogView.findViewById(R.id.bt_submit);

        btCancel.setOnClickListener(v -> dialog.dismiss());

        btSubmit.setOnClickListener(v -> {
            String question = etQuestion.getText().toString().trim();
            String answer = etAnswer.getText().toString().trim();

            if (question.isEmpty()) {
                etQuestion.setError("Question is required");
                return;
            }

            if (answer.isEmpty()) {
                etAnswer.setError("Answer is required");
                return;
            }

            addAdviceToFirebase(question, answer);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void addAdviceToFirebase(String question, String answer) {
        String adviceId = String.valueOf(System.currentTimeMillis());

        Advice advice = new Advice();
        advice.setId(adviceId);
        advice.setQuestion(question);
        advice.setAnswer(answer);
        advice.setAuthorId(FirebaseAuth.getInstance().getUid());
        advice.setTimestamp(System.currentTimeMillis());
        advice.setVerified(false);

        DatabaseReference adviceRef = FirebaseDatabase.getInstance()
                .getReference("Plants")
                .child(plant.getId())
                .child("advicesList")
                .child(adviceId);

        adviceRef.setValue(advice)
                .addOnSuccessListener(task -> {
                    Toast.makeText(this, "Advice added successfully!", Toast.LENGTH_SHORT).show();
                    refreshAdvices();
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(this, "Failed to add advice: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void refreshAdvices() {
        DatabaseReference plantRef = FirebaseDatabase.getInstance()
                .getReference("Plants")
                .child(plant.getId());

        plantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Plant updatedPlant = snapshot.getValue(Plant.class);
                if (updatedPlant == null || updatedPlant.getAdvicesList() == null || updatedPlant.getAdvicesList().isEmpty()) {
                    runOnUiThread(() -> {
                        binding.tvAdvices.setVisibility(View.GONE);
                        binding.llAdvices.setVisibility(View.GONE);
                        binding.rvAdvices.setVisibility(View.GONE);
                    });
                    return;
                }

                plant = updatedPlant;

                runOnUiThread(() -> {
                    binding.tvAdvices.setVisibility(View.VISIBLE);
                    binding.llAdvices.setVisibility(View.VISIBLE);
                    binding.rvAdvices.setVisibility(View.VISIBLE);
                });

                List<Advice> adviceList = new ArrayList<>(plant.getAdvicesList().values());

                String srcLang = (plant.getOriginalLanguage() != null && !plant.getOriginalLanguage().isEmpty())
                        ? plant.getOriginalLanguage()
                        : "en";
                String targetLang = java.util.Locale.getDefault().getLanguage();

                if (srcLang.equals(targetLang)) {
                    runOnUiThread(() -> {
                        checkPlantAdapter = new AdviceAdapter(CheckPlantActivity.this, adviceList);
                        binding.rvAdvices.setAdapter(checkPlantAdapter);
                    });
                } else {
                    runOnUiThread(() -> {
                        checkPlantAdapter = new AdviceAdapter(CheckPlantActivity.this, adviceList);
                        binding.rvAdvices.setAdapter(checkPlantAdapter);
                    });
                    translateAdvicesInBackground(adviceList, srcLang, targetLang);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CheckPlantActivity", "refreshAdvices error", error.toException());
            }
        });
    }

    private void initAdapter() {
        advicesLayoutManager = new LinearLayoutManager(this);
        binding.rvAdvices.setLayoutManager(advicesLayoutManager);

        if (plant.getAdvicesList() == null || plant.getAdvicesList().isEmpty()) {
            binding.tvAdvices.setVisibility(View.GONE);
            binding.llAdvices.setVisibility(View.GONE);
            binding.rvAdvices.setVisibility(View.GONE);
            return;
        }

        binding.tvAdvices.setVisibility(View.VISIBLE);
        binding.llAdvices.setVisibility(View.VISIBLE);
        binding.rvAdvices.setVisibility(View.VISIBLE);

        List<Advice> adviceList = new ArrayList<>(plant.getAdvicesList().values());

        String targetLang = java.util.Locale.getDefault().getLanguage();
        String srcLang = (plant.getOriginalLanguage() != null && !plant.getOriginalLanguage().isEmpty())
                ? plant.getOriginalLanguage()
                : "en";

        if (srcLang.equals(targetLang)) {
            checkPlantAdapter = new AdviceAdapter(this, adviceList);
            binding.rvAdvices.setAdapter(checkPlantAdapter);
        } else {
            checkPlantAdapter = new AdviceAdapter(this, adviceList);
            binding.rvAdvices.setAdapter(checkPlantAdapter);

            translateAdvicesInBackground(adviceList, srcLang, targetLang);
        }
    }

    private void translateAdvicesInBackground(List<Advice> adviceList, String srcLang, String targetLang) {
        AtomicInteger remaining = new AtomicInteger(adviceList.size() * 2);

        for (Advice advice : adviceList) {
            final String qText = advice.getQuestion() != null ? advice.getQuestion() : "";
            final String aText = advice.getAnswer() != null ? advice.getAnswer() : "";

            TranslationHelper.translate(this, srcLang, targetLang, qText, translatedQ -> {
                advice.setQuestion(translatedQ != null ? translatedQ : qText);
                if (remaining.decrementAndGet() == 0) {
                    runOnUiThread(() -> checkPlantAdapter.notifyDataSetChanged());
                }
            });

            TranslationHelper.translate(this, srcLang, targetLang, aText, translatedA -> {
                advice.setAnswer(translatedA != null ? translatedA : aText);
                if (remaining.decrementAndGet() == 0) {
                    runOnUiThread(() -> checkPlantAdapter.notifyDataSetChanged());
                }
            });
        }
    }

    private void openAddActivity() {
        UserPlant userPlant = new UserPlant();
        userPlant.setId(String.valueOf(System.currentTimeMillis()));
        userPlant.setName(plant.getCommonName());
        userPlant.setWateringFrequency(plant.getWateringFrequency());
        userPlant.setFertilizingFrequency(plant.getFertilizingFrequency());
        userPlant.setSprayingFrequency(plant.getSprayingFrequency());
        userPlant.setLastWatering(getCurrentDate());
        userPlant.setLastFertilizing(getCurrentDate());
        userPlant.setLastSpraying(getCurrentDate());
        userPlant.setImage(plant.getImage());

        DatabaseReference userPlantsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("UserPlants");

        userPlantsRef.child(userPlant.getId()).setValue(userPlant)
                .addOnSuccessListener(task -> {
                    Toast.makeText(this, "Plant added to your collection!", Toast.LENGTH_SHORT).show();

                    scheduleNotificationForPlant(this, userPlant);

                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(this, "Failed to add plant: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }


    public void initWateringFrequency(long wateringFrequency) {
        if (wateringFrequency != 0) {
            binding.tvWateringDays.setText(getString(R.string.days, wateringFrequency));
            binding.pbWater.setProgress((int) getProgressBarFill(wateringFrequency));
        } else {
            binding.tvWateringDays.setText(getString(R.string.never));
            binding.pbWater.setProgress(0);
        }
    }

    public void initFertilizingFrequency(long fertilizingFrequency) {
        if (fertilizingFrequency != 0) {
            binding.tvFertilizingDays.setText(getString(R.string.days, fertilizingFrequency));
            binding.pbFertilizer.setProgress((int) getProgressBarFill(fertilizingFrequency));
        } else {
            binding.tvFertilizingDays.setText(getString(R.string.never));
            binding.pbFertilizer.setProgress(0);
        }
    }

    public void initSprayingFrequency(long sprayingFrequency) {
        if (sprayingFrequency != 0) {
            binding.tvSprayingDays.setText(getString(R.string.days, sprayingFrequency));
            binding.pbSpraying.setProgress((int) getProgressBarFill(sprayingFrequency));
        } else {
            binding.tvSprayingDays.setText(getString(R.string.never));
            binding.pbSpraying.setProgress(0);
        }
    }

    public void setPlantPhoto() {
        if (plant.getImage() != null && !plant.getImage().isEmpty()) {
            Glide
                    .with(this)
                    .load(plant.getImage())
                    .into(binding.ivPhoto);
        }
    }

    private void openEditPublicPlant() {
        Intent intent = new Intent(this, EditPublicPlantActivity.class);
        intent.putExtra(PLANT_INTENT_EXTRAS_KEY, plant);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (plantRef != null && plantListener != null) {
            plantRef.removeEventListener(plantListener);
        }
    }

}