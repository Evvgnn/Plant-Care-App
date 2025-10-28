package com.grzeluu.lookupplant.core.forgot;

import com.google.firebase.auth.FirebaseAuth;
import com.grzeluu.lookupplant.R;

public class ForgotInteractor implements ForgotContract.Interactor {

    private ForgotContract.ForgotListener forgotListener;

    public ForgotInteractor(ForgotContract.ForgotListener forgotListener) {
        this.forgotListener = forgotListener;
    }

    @Override
    public void performResetPassword(String email) {
        forgotListener.onStart();
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener(task -> {
                        forgotListener.onEnd();
                        forgotListener.onSuccess(R.string.email_sent);
                })
                .addOnFailureListener(error -> {
                    forgotListener.onEnd();
                    forgotListener.onFailure(error.getMessage());
                });
    }
}
