package com.ianfrost.example.example_simpleauth_firebaseauth_java.featureProfile;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ianfrost.example.example_simpleauth_firebaseauth_java.MainViewModel;
import com.ianfrost.example.example_simpleauth_firebaseauth_java.R;
import com.ianfrost.example.example_simpleauth_firebaseauth_java.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private MainViewModel parentViewModel;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        parentViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        parentViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.content.setText(getString(R.string.Profile_Welcome, user.getEmail()));
            }
        });

        binding.logoutBtn.setOnClickListener(btnView -> parentViewModel.signOut());

    }
}