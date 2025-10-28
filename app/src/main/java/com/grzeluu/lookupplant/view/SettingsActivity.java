package com.grzeluu.lookupplant.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.grzeluu.lookupplant.R;
import com.grzeluu.lookupplant.utils.LocaleHelper;

public class SettingsActivity extends AppCompatActivity {

    private Button btnEnglish, btnRussian;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnEnglish = findViewById(R.id.btn_english);
        btnRussian = findViewById(R.id.btn_russian);

        btnEnglish.setOnClickListener(v -> changeLanguage("en"));
        btnRussian.setOnClickListener(v -> changeLanguage("ru"));
    }

    private void changeLanguage(String langCode) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putString(LocaleHelper.KEY_LANGUAGE, langCode).apply();

        LocaleHelper.setLocale(this, langCode);

        Intent intent = new Intent(this, com.grzeluu.lookupplant.view.MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
