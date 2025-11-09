package com.ianfrost.example.example_simpleauth_firebaseauth_java;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ianfrost.example.example_simpleauth_firebaseauth_java.databinding.ActivityMainBinding;
import com.ianfrost.example.example_simpleauth_firebaseauth_java.featureAuth.AuthFragment;
import com.ianfrost.example.example_simpleauth_firebaseauth_java.featureProfile.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        getWindow().setStatusBarColor(getResources().getColor(R.color.statusBar, getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.statusBar, getTheme()));

        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getUser().observe(this, user -> {
            Fragment nowUseFragment = getSupportFragmentManager().findFragmentById(binding.fragmentContainer.getId());
            if (user != null) {
                if (!(nowUseFragment instanceof ProfileFragment)) {
                    getSupportFragmentManager().beginTransaction().replace(binding.fragmentContainer.getId(), ProfileFragment.newInstance()).commit();
                }
            } else {
                if (!(nowUseFragment instanceof AuthFragment)) {
                    getSupportFragmentManager().beginTransaction().replace(binding.fragmentContainer.getId(), AuthFragment.newInstance() ).commit();
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        viewModel.checkCurrentUser();

    }


}