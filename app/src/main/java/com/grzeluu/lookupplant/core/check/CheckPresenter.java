package com.grzeluu.lookupplant.core.check;

import com.grzeluu.lookupplant.base.BasePresenter;

public class CheckPresenter extends BasePresenter implements CheckContract.Presenter, CheckContract.Listener {

    private final CheckContract.View checkView;

    public CheckPresenter(CheckContract.View checkView) {
        super(checkView);
        this.checkView = checkView;
    }
}