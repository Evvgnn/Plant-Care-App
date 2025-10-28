package com.grzeluu.lookupplant.base;

import androidx.annotation.StringRes;

public interface BaseViewContract {

    void showLoading();

    void hideLoading();

    void showMessage(String message);

    void showMessage(@StringRes int resId);
}