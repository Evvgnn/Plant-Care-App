package com.grzeluu.lookupplant.core.add;

import com.grzeluu.lookupplant.base.BaseListenerContract;
import com.grzeluu.lookupplant.base.BasePresenterContract;
import com.grzeluu.lookupplant.base.BaseViewContract;
import com.grzeluu.lookupplant.model.UserPlant;

public interface AddContract {
    interface View extends BaseViewContract {
        void setNameError(int error);
        void plantAdded(int message, UserPlant plant);
    }

    interface Presenter extends BasePresenterContract {
        void addPlant(UserPlant plant, String latinName, String type, String description);
    }

    interface Interactor {
        void performAddPlant(UserPlant plant, String latinName, String type, String description);
    }

    interface Listener extends BaseListenerContract {
        void onSuccess(int message, UserPlant plant);
        void onFailure(String message);
    }
}