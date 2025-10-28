package com.grzeluu.lookupplant.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.grzeluu.lookupplant.R;
import com.grzeluu.lookupplant.utils.LocaleHelper;
import com.grzeluu.lookupplant.utils.ProgressDialogUtils;

public abstract class BaseActivity extends AppCompatActivity implements BaseViewContract {

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void showLoading() {
        hideLoading();
        progressDialog = ProgressDialogUtils.showLoadingDialog(this);
    }

    @Override
    public void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }

    @Override
    public void showMessage(String message) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.some_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showMessage(int resId) {
        showMessage(getString(resId));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        android.content.SharedPreferences prefs = newBase.getSharedPreferences("app_prefs", MODE_PRIVATE);
        String lang = prefs.getString(LocaleHelper.KEY_LANGUAGE, "en");
        ContextWrapper cw = LocaleHelper.setLocale(newBase, lang);
        super.attachBaseContext(cw);
    }

}
