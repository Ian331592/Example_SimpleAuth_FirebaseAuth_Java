package com.ianfrost.example.example_simpleauth_firebaseauth_java;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainViewModel extends ViewModel {

    // Firebase驗證庫(處理用戶登入、登出、註冊)
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    // 用戶資料
    private final MutableLiveData<FirebaseUser> user = new MutableLiveData<>();

    /**
     * 取得用戶
     */
    public LiveData<FirebaseUser> getUser() {
        return user;
    }

    /**
     * 用戶資料取得並儲存
     */
    public void checkCurrentUser() {
        FirebaseUser getUser = firebaseAuth.getCurrentUser();
        user.setValue(getUser);
    }

    /**
     * 用戶登出
     */
    public void signOut() {
        firebaseAuth.signOut();
        checkCurrentUser();
    }

}

