package com.grzeluu.lookupplant.core.main;

import com.grzeluu.lookupplant.base.BaseListenerContract;
import com.grzeluu.lookupplant.base.BasePresenterContract;
import com.grzeluu.lookupplant.base.BaseViewContract;
import com.grzeluu.lookupplant.model.User;

public interface MainContract {
    interface View extends BaseViewContract {
        void requireLogin();

        void setUser(User username);

        void setEmail(String email);
    }

    interface Presenter extends BasePresenterContract {
        void checkIfUserIsLoggedIn();
        void onDrawerOptionLogoutClick();
    }

    interface Interactor {
        void performGetUserData();

        void performLogout();
    }

    interface Listener extends BaseListenerContract {
        void onSuccess(User user);

        void onFailure();
    }
}
