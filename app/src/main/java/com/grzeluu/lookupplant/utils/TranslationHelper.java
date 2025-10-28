package com.grzeluu.lookupplant.utils;

import android.content.Context;
import android.util.Log;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;

public class TranslationHelper {
    public interface Callback {
        void onComplete(String translated);
    }

    private static final String TAG = "TranslationHelper";

    public static void translate(Context context,
                                 String srcLangIso,
                                 String tgtLangIso,
                                 String text,
                                 Callback callback) {
        if (text == null || text.trim().isEmpty()) {
            callback.onComplete(text == null ? "" : text);
            return;
        }

        if (srcLangIso.equals(tgtLangIso)) {
            callback.onComplete(text);
            return;
        }

        if (isTextInLanguage(text, tgtLangIso)) {
            Log.d(TAG, "Text already in target language: " + tgtLangIso);
            callback.onComplete(text);
            return;
        }

        String src = TranslateLanguage.fromLanguageTag(srcLangIso);
        String tgt = TranslateLanguage.fromLanguageTag(tgtLangIso);

        if (src == null) {
            Log.w(TAG, "Unknown source language: " + srcLangIso + ", defaulting to English");
            src = TranslateLanguage.ENGLISH;
        }
        if (tgt == null) {
            Log.w(TAG, "Unknown target language: " + tgtLangIso + ", defaulting to English");
            tgt = TranslateLanguage.ENGLISH;
        }

        if (src.equals(tgt)) {
            callback.onComplete(text);
            return;
        }

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(src)
                .setTargetLanguage(tgt)
                .build();

        final Translator translator = Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(aVoid -> {
                    translator.translate(text)
                            .addOnSuccessListener(translated -> {
                                Log.d(TAG, "Translation success: '" +
                                        text.substring(0, Math.min(30, text.length())) + "...' -> '" +
                                        translated.substring(0, Math.min(30, translated.length())) + "...'");
                                callback.onComplete(translated);
                                translator.close();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Translation failed", e);
                                callback.onComplete(text);
                                translator.close();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Model download failed", e);
                    callback.onComplete(text);
                    translator.close();
                });
    }
    private static boolean isTextInLanguage(String text, String langIso) {
        if (text == null || text.isEmpty()) return true;

        switch (langIso) {
            case "ru":
                return text.matches(".*[а-яА-ЯёЁ].*");

            case "en":
                return text.matches(".*[a-zA-Z].*") && !text.matches(".*[а-яА-ЯёЁ].*");

            case "uk":
                return text.matches(".*[а-яА-ЯёЁіІїЇєЄґҐ].*");

            case "de":
                return text.matches(".*[a-zA-ZäöüßÄÖÜ].*") && !text.matches(".*[а-яА-ЯёЁ].*");

            case "fr":
                return text.matches(".*[a-zA-ZàâæçéèêëïîôùûüÿœÀÂÆÇÉÈÊËÏÎÔÙÛÜŸŒ].*")
                        && !text.matches(".*[а-яА-ЯёЁ].*");

            case "es":
                return text.matches(".*[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ].*")
                        && !text.matches(".*[а-яА-ЯёЁ].*");

            default:
                return false;
        }
    }
}