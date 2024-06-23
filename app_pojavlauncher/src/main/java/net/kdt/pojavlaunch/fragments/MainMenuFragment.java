package net.kdt.pojavlaunch.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.PojavProfile;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;

public class MainMenuFragment extends Fragment {
    public static final String TAG = "MainMenuFragment";
    private SharedPreferences mUserPrefs;
    private TextView mUserNameText;

    public MainMenuFragment(){
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mUserPrefs = view.getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        Button mPlayButton = view.findViewById(R.id.play_button);
        mUserNameText = view.findViewById(R.id.username_textbar);
        mUserNameText.setText(mUserPrefs.getString("username", ""));
        mUserNameText.addTextChangedListener(new TextSaver());
        mPlayButton.setOnClickListener(v -> {
            String userName = mUserNameText.getText().toString();
            PojavProfile.setCurrentProfile(v.getContext(), userName);
            ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private class TextSaver implements TextWatcher, Runnable {
        private final Handler mHandler = new Handler(Looper.getMainLooper());
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, 150);
        }

        @Override
        public void run() {
            String userName = mUserNameText.getText().toString();
            mUserPrefs.edit().putString("username", userName).apply();
        }
    }
}
