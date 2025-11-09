package com.ianfrost.example.example_simpleauth_firebaseauth_java;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;

public class AuthResult {
    public final AuthAction action;
    public final boolean isSuccess;
    public final FirebaseUser user;
    public final Exception error;
    // phone 驗證，需要 verificationId、forceResendToken
    public final String verificationId;
    public final PhoneAuthProvider.ForceResendingToken forceResendToken;

    public enum AuthAction {
        LOAD,
        LOGIN,
        REGISTER_NO_VERIFY,
        REGISTER_EMAIL_VERIFY,
        REGISTER_PHONE_VERIFY,
        REGISTER_PHONE_VERIFY_End,
    }

    public AuthResult(AuthAction action, boolean isSuccess, FirebaseUser user, Exception error) {
        this(action, isSuccess, user, error, null, null);
    }

    public static AuthResult load() {
        return new AuthResult(AuthAction.LOAD, true, null, null);
    }

    public AuthResult(AuthAction action, boolean isSuccess, FirebaseUser user, Exception error, String verificationId, PhoneAuthProvider.ForceResendingToken token) {
        this.action = action;
        this.isSuccess = isSuccess;
        this.user = user;
        this.error = error;
        this.verificationId = verificationId;
        this.forceResendToken = token;
    }


}
