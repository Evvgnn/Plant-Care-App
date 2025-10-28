package com.grzeluu.lookupplant.core.forgot;

import com.grzeluu.lookupplant.base.BaseListenerContract;
import com.grzeluu.lookupplant.base.BaseViewContract;

public interface ForgotContract {
    interface View extends BaseViewContract {
        void onSendEmailSuccess(int message);

        void setEmailError(int message);

        void onSendEmailFailure(String message);
    }

    interface Presenter extends BaseListenerContract{
        void resetPassword(String email);
    }

    interface Interactor {
        void performResetPassword(String email);
    }

    interface ForgotListener extends BaseListenerContract {
        void onSuccess(int message);
        void onFailure(String message);
    }
}