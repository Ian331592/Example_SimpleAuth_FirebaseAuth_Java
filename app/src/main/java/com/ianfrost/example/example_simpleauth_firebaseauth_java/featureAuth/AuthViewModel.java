package com.ianfrost.example.example_simpleauth_firebaseauth_java.featureAuth;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.ianfrost.example.example_simpleauth_firebaseauth_java.AuthResult;

import java.util.concurrent.TimeUnit;

public class AuthViewModel extends ViewModel {

    // FirebaseAuth，驗證庫初始化
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    // LiveData 管理 UI/資料雙向綁定
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<String> phone = new MutableLiveData<>();

    // 手機註冊驗證需求
    // 手機註冊-簡訊識別碼
    private final MutableLiveData<String> verificationId = new MutableLiveData<>();
    // 手機註冊-簡訊驗證碼儲存
    private final MutableLiveData<String> verifyCode = new MutableLiveData<>();

    // 頁面狀態(發出請求、登入、無驗證註冊、信箱驗證註冊、手機號碼驗證註冊、等待輸入手機號碼驗證註冊)
    private final MutableLiveData<AuthResult.AuthAction> switchStatus = new MutableLiveData<>(AuthResult.AuthAction.LOGIN);

    /**
     * 登入/註冊/驗證處理結果的回呼介面。
     * 用於 AuthViewModel 執行驗證流程後，回傳執行結果（成功/失敗／狀態等）。
     */
    public interface AuthResultCallback {
        void onResult(AuthResult result);
    }

    /**
     * 取得信箱
     *
     * @return 信箱
     */
    public MutableLiveData<String> getEmail() {
        return email;
    }

    /**
     * 取得密碼
     *
     * @return 密碼
     */
    public MutableLiveData<String> getPassword() {
        return password;
    }

    /**
     * 取得電話號碼
     *
     * @return 手機號碼
     */
    public MutableLiveData<String> getPhone() {
        return phone;
    }

    /**
     * 取得認證狀態
     * 狀態：{@link AuthResult.AuthAction}
     *
     * @return 認證狀態
     */
    public MutableLiveData<AuthResult.AuthAction> getSwitchStatus() {
        return switchStatus;
    }

    /**
     * 取得手機驗證碼
     *
     * @return 手機簡訊驗證碼
     */
    public MutableLiveData<String> getVerifyCode() {
        return verifyCode;
    }

    /**
     * 認證狀態切換
     */
    public void switchStatus() {
        AuthResult.AuthAction current = switchStatus.getValue();
        if (current == null) current = AuthResult.AuthAction.LOGIN;
        switchStatus.setValue(
                current == AuthResult.AuthAction.LOGIN ? AuthResult.AuthAction.REGISTER_NO_VERIFY :
                        current == AuthResult.AuthAction.REGISTER_NO_VERIFY ? AuthResult.AuthAction.REGISTER_EMAIL_VERIFY :
                                current == AuthResult.AuthAction.REGISTER_EMAIL_VERIFY ? AuthResult.AuthAction.REGISTER_PHONE_VERIFY :
                                        AuthResult.AuthAction.LOGIN
        );
    }

    /**
     * 根據認證狀態決定採用哪一種註冊/登入方式
     * 狀態：{@link AuthResult.AuthAction}
     *
     * @param activity 該頁主程序
     * @param callback 回傳送出結果 {@link AuthResultCallback}
     */
    public void doConfirm(Activity activity, AuthResultCallback callback) {
        AuthResult.AuthAction status = switchStatus.getValue();
        if (status == null) {
            return;
        }

        switch (status) {
            case LOGIN:
                login(callback);
                break;
            case REGISTER_NO_VERIFY:
                registerNoVerify(callback);
                break;
            case REGISTER_EMAIL_VERIFY:
                registerWithEmailVerify(callback);
                break;
            case REGISTER_PHONE_VERIFY:
                registerWithPhoneVerify(activity, callback); // 手機階段1
                break;
            case REGISTER_PHONE_VERIFY_End:
                registerWithPhoneCodeVerify(callback); // 手機階段2
                break;
        }
    }

    /**
     * 安全取得字串，trim/null safe
     *
     * @param data 字串
     */
    private String getSafeText(MutableLiveData<String> data) {
        String s = data.getValue();
        return s == null ? "" : s.trim();
    }

    /**
     * 登入流程
     *
     * @param callback 回傳認證送出的結果 {@link AuthResultCallback}
     */
    public void login(AuthResultCallback callback) {
        final AuthResult.AuthAction action = AuthResult.AuthAction.LOGIN;
        String emailVal = getSafeText(email);
        String pwVal = getSafeText(password);

        try {
            if (emailVal.isEmpty()) {
                callback.onResult(new AuthResult(action, false, null, new IllegalArgumentException("Email 不可為空")));
                return;
            }
            if (pwVal.isEmpty()) {
                callback.onResult(new AuthResult(action, false, null, new IllegalArgumentException("密碼不可為空")));
                return;
            }

            callback.onResult(AuthResult.load());
            firebaseAuth.signInWithEmailAndPassword(emailVal, pwVal)
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful()) {
                                callback.onResult(new AuthResult(action, true, firebaseAuth.getCurrentUser(), null));
                            } else {
                                callback.onResult(new AuthResult(action, false, null,
                                        safeException(task.getException(), "登入失敗")));
                            }
                        } catch (Exception ex) {
                            callback.onResult(new AuthResult(action, false, null, ex));
                        }
                    });
        } catch (Exception ex) {
            callback.onResult(new AuthResult(action, false, null, ex));
        }
    }

    /**
     * 註冊 - 無驗證
     *
     * @param callback 回傳認證送出的結果 {@link AuthResultCallback}
     */
    public void registerNoVerify(AuthResultCallback callback) {
        final AuthResult.AuthAction action = AuthResult.AuthAction.REGISTER_NO_VERIFY;
        String emailVal = getSafeText(email);
        String pwVal = getSafeText(password);

        try {
            if (emailVal.isEmpty()) {
                callback.onResult(new AuthResult(action, false, null, new IllegalArgumentException("Email 不可為空")));
                return;
            }
            if (pwVal.isEmpty()) {
                callback.onResult(new AuthResult(action, false, null, new IllegalArgumentException("密碼不可為空")));
                return;
            }

            callback.onResult(AuthResult.load());
            firebaseAuth.createUserWithEmailAndPassword(emailVal, pwVal)
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful()) {
                                callback.onResult(new AuthResult(action, true, firebaseAuth.getCurrentUser(), null));
                            } else {
                                callback.onResult(new AuthResult(action, false, null,
                                        safeException(task.getException(), "註冊失敗")));
                            }
                        } catch (Exception ex) {
                            callback.onResult(new AuthResult(action, false, null, ex));
                        }
                    });
        } catch (Exception ex) {
            callback.onResult(new AuthResult(action, false, null, ex));
        }
    }

    /**
     * 註冊 - 信箱驗證
     *
     * @param callback 回傳認證送出的結果 {@link AuthResultCallback}
     */
    public void registerWithEmailVerify(AuthResultCallback callback) {
        final AuthResult.AuthAction action = AuthResult.AuthAction.REGISTER_EMAIL_VERIFY;
        String emailVal = getSafeText(email);
        String pwVal = getSafeText(password);

        try {
            if (emailVal.isEmpty()) {
                callback.onResult(new AuthResult(action, false, null, new IllegalArgumentException("Email 不可為空")));
                return;
            }
            if (pwVal.isEmpty()) {
                callback.onResult(new AuthResult(action, false, null, new IllegalArgumentException("密碼不可為空")));
                return;
            }

            callback.onResult(AuthResult.load());
            firebaseAuth.createUserWithEmailAndPassword(emailVal, pwVal)
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {
                                    user.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                        try {
                                            if (verifyTask.isSuccessful()) {
                                                callback.onResult(new AuthResult(action, true, user, null));
                                            } else {
                                                callback.onResult(new AuthResult(action, false, null,
                                                        safeException(verifyTask.getException(), "驗證信寄出失敗")));
                                            }
                                        } catch (Exception ex) {
                                            callback.onResult(new AuthResult(action, false, null, ex));
                                        }
                                    });
                                } else {
                                    callback.onResult(new AuthResult(action, false, null, new Exception("註冊成功後找不到用戶資訊")));
                                }
                            } else {
                                callback.onResult(new AuthResult(action, false, null,
                                        safeException(task.getException(), "註冊失敗")));
                            }
                        } catch (Exception ex) {
                            callback.onResult(new AuthResult(action, false, null, ex));
                        }
                    });
        } catch (Exception ex) {
            callback.onResult(new AuthResult(action, false, null, ex));
        }
    }

    /**
     * 註冊 - 手機號碼驗證註冊
     *
     * @param callback 回傳認證送出的結果 {@link AuthResultCallback}
     */
    public void registerWithPhoneVerify(Activity activity, AuthResultCallback callback) {
        final AuthResult.AuthAction action = AuthResult.AuthAction.REGISTER_PHONE_VERIFY;
        String emailVal = getSafeText(email);
        String pwVal = getSafeText(password);
        String phoneVal = getSafeText(phone);

        // 基本檢查
        if (emailVal.isEmpty() || pwVal.isEmpty() || phoneVal.isEmpty()) {
            callback.onResult(new AuthResult(action, false, null, new IllegalArgumentException("Email、密碼、手機不可為空")));
            return;
        }

        callback.onResult(AuthResult.load());
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneVal)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // 如果自動完成驗證，可以幫用戶直接流程完成 (進階可做)
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        callback.onResult(new AuthResult(action, false, null, e));
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationIdVal, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId.setValue(verificationIdVal);
                        switchStatus.setValue(AuthResult.AuthAction.REGISTER_PHONE_VERIFY_End); // 進入下一階段
                        callback.onResult(new AuthResult(action, false, null, null, verificationIdVal, token));
                        // UI監聽switchStatus, 顯示驗證碼輸入欄位即可
                    }
                }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    /**
     * 註冊 - 手機號碼註冊 -> 取得驗證碼後驗證
     *
     * @param callback 回傳認證送出的結果 {@link AuthResultCallback}
     */
    public void registerWithPhoneCodeVerify(AuthResultCallback callback) {
        final AuthResult.AuthAction action = AuthResult.AuthAction.REGISTER_PHONE_VERIFY_End;
        String emailVal = getSafeText(email);
        String pwVal = getSafeText(password);
        String verificationIdVal = getSafeText(verificationId);
        String code = getSafeText(verifyCode);

        if (verificationIdVal.isEmpty() || code.isEmpty()) {
            callback.onResult(new AuthResult(action, false, null, new IllegalArgumentException("驗證碼不可為空")));
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationIdVal, code);
        callback.onResult(AuthResult.load());
        firebaseAuth.createUserWithEmailAndPassword(emailVal, pwVal)
                .addOnCompleteListener(regTask -> {
                    if (regTask.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            user.linkWithCredential(credential)
                                    .addOnCompleteListener(linkTask -> {
                                        if (linkTask.isSuccessful()) {
                                            callback.onResult(new AuthResult(action, true, user, null));
                                        } else {
                                            callback.onResult(new AuthResult(action, false, null, safeException(linkTask.getException(), "手機驗證綁定失敗")));
                                        }
                                    });
                        } else {
                            callback.onResult(new AuthResult(action, false, null, new Exception("註冊成功後找不到用戶資訊")));
                        }
                    } else {
                        callback.onResult(new AuthResult(action, false, null, safeException(regTask.getException(), "註冊失敗")));
                    }
                });
    }

    /**
     * 保證永遠不丟 null Exception，log 替代
     *
     * @param ex 除錯取得的錯誤資訊
     * @param fallback 錯誤資訊字串
     */
    private Exception safeException(Exception ex, String fallback) {
        if (ex != null) return ex;
        return new Exception(fallback != null ? fallback : "未知錯誤");
    }

}