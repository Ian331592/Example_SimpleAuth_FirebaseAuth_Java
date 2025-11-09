package com.ianfrost.example.example_simpleauth_firebaseauth_java.featureAuth;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ianfrost.example.example_simpleauth_firebaseauth_java.AuthResult;
import com.ianfrost.example.example_simpleauth_firebaseauth_java.MainViewModel;
import com.ianfrost.example.example_simpleauth_firebaseauth_java.R;
import com.ianfrost.example.example_simpleauth_firebaseauth_java.databinding.FragmentAuthBinding;

public class AuthFragment extends Fragment {

    // UI綁定
    private FragmentAuthBinding binding;

    // 父MainActivity的資料管理，負責儲存登入後的使用者資料
    private MainViewModel parentViewModel;

    // 本頁面資料管理，負責登入、註冊的【資料管理、請求送出、取得送出結果(處理UI)】
    private AuthViewModel viewModel;

    /**
     * 實例化
     */
    public static AuthFragment newInstance() {
        return new AuthFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 利用 DataBinding inflate 並回傳最上層 root view
        binding = FragmentAuthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModel 初始化
        parentViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 綁定 ViewModel 到 DataBinding（可於 XML 直接寫 observe, 2-way binding...等）
        binding.setAuth(viewModel);

        //  UI 監聽註冊
        setupAuthStatusObserver();
        setupClickListeners();
    }

    /**
     * 認證狀態監聽（根據回傳狀態自動切換 UI 版型）
     * 狀態有：登入、無驗證註冊、信箱驗證註冊、手機號碼驗證
     * 請至GitHub查看提供的手機測試號碼，FirebaseAuth免費版不提供真實的號碼測試
     */
    private void setupAuthStatusObserver() {
        viewModel.getSwitchStatus().observe(getViewLifecycleOwner(), authAction -> {
            // 注意多餘的重複 setVisibility 可以統合，如有多組可考慮 extract view switch 到 utility
            switch (authAction) {
                case LOGIN:
                    setAuthUIVisibility(true, true, false, false);
                    binding.switchStatusTxv.setText(R.string.Main_SwitchStatus_Login);
                    binding.confirmBtn.setText(R.string.Main_Login);
                    binding.switchBtn.setText(R.string.Main_Switch_Register);
                    break;
                case REGISTER_NO_VERIFY:
                    setAuthUIVisibility(true, true, false, false);
                    binding.switchStatusTxv.setText(R.string.Main_SwitchStatus_Register_No_Verify);
                    binding.confirmBtn.setText(R.string.Main_Register);
                    binding.switchBtn.setText(R.string.Main_Switch_Register);
                    break;
                case REGISTER_EMAIL_VERIFY:
                    setAuthUIVisibility(true, true, false, false);
                    binding.switchStatusTxv.setText(R.string.Main_SwitchStatus_Register_Email_Verify);
                    binding.confirmBtn.setText(R.string.Main_Register);
                    binding.switchBtn.setText(R.string.Main_Switch_Register);
                    break;
                case REGISTER_PHONE_VERIFY:
                    setAuthUIVisibility(true, true, true, false);
                    binding.switchStatusTxv.setText(R.string.Main_SwitchStatus_Register_Phone_Verify);
                    binding.confirmBtn.setText(R.string.Main_Register);
                    binding.switchBtn.setText(R.string.Main_Switch_Login);
                    break;
                case REGISTER_PHONE_VERIFY_End:
                    setAuthUIVisibility(false, false, false, true);
                    binding.switchStatusTxv.setText(R.string.Main_SwitchStatus_Register_Phone_Verify);
                    binding.confirmBtn.setText(R.string.Main_Register);
                    binding.switchBtn.setText(R.string.Main_Switch_Login);
                    break;
            }
        });
    }

    /**
     * UI 顯示切換
     *
     * @param email      信箱輸入版面
     * @param password   密碼輸入版面
     * @param phone      手機號碼輸入版面
     * @param verifyCode 驗證碼輸入版面
     */
    private void setAuthUIVisibility(boolean email, boolean password, boolean phone, boolean verifyCode) {
        binding.emailLayout.setVisibility(email ? View.VISIBLE : View.GONE);
        binding.passwordLayout.setVisibility(password ? View.VISIBLE : View.GONE);
        binding.phoneLayout.setVisibility(phone ? View.VISIBLE : View.GONE);
        binding.verifyCodeLayout.setVisibility(verifyCode ? View.VISIBLE : View.GONE);
    }

    /**
     * 設定按鈕監聽事件
     */
    private void setupClickListeners() {
        binding.confirmBtn.setOnClickListener(v -> handleConfirmBtnClick());
        binding.switchBtn.setOnClickListener(v -> viewModel.switchStatus());
    }

    /**
     * 確認按鈕事件
     * 根據當前認證狀態送出不同請求
     * 狀態有：登入、無驗證註冊、信箱驗證註冊、手機號碼驗證
     */
    private void handleConfirmBtnClick() {
        viewModel.doConfirm(requireActivity(), result -> {
            if (result.isSuccess) {
                handleAuthSuccess(result);
            } else if (result.verificationId != null) {
                // [手機註冊階段1] 手機驗證碼已發送
                binding.progressBar.setVisibility(View.GONE);
                showAlert("手機驗證碼已送出，請輸入簡訊收到的驗證碼");
                // TODO: 可導向簡訊驗證畫面
            } else {
                // 失敗處理
                binding.progressBar.setVisibility(View.GONE);
                showAlert("失敗：" + (result.error != null ? result.error.getMessage() : "未知錯誤"));
            }
        });
    }

    /**
     * 登入／註冊／驗證成功處理邏輯
     *
     * @param result 認證結果
     */
    private void handleAuthSuccess(AuthResult result) {
        switch (result.action) {
            case LOAD:
                binding.progressBar.setVisibility(View.VISIBLE);
                break;
            case LOGIN:
                binding.progressBar.setVisibility(View.GONE);
                parentViewModel.checkCurrentUser();
                showAlert("使用者登入成功");
                break;
            case REGISTER_NO_VERIFY:
                binding.progressBar.setVisibility(View.GONE);
                showAlert("使用者註冊成功");
                break;
            case REGISTER_EMAIL_VERIFY:
                binding.progressBar.setVisibility(View.GONE);
                showAlert("使用者信箱驗證註冊成功，請至信箱收信完成驗證");
                break;
            case REGISTER_PHONE_VERIFY_End:
                binding.progressBar.setVisibility(View.GONE);
                showAlert("使用者手機驗證註冊成功!");
                viewModel.switchStatus();
                break;
        }
    }

    /**
     * 顯示提醒訊息對話框
     *
     * @param msg 提醒內容
     */
    private void showAlert(String msg) {
        new AlertDialog.Builder(requireContext())
                .setTitle("提醒")
                .setMessage(msg)
                .setPositiveButton("確定", null)
                .show();
    }
}