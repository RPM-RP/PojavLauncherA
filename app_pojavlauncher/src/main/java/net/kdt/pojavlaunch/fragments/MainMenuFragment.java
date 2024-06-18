package net.kdt.pojavlaunch.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    public MainMenuFragment(){
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mUserPrefs = view.getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        Button mPlayButton = view.findViewById(R.id.play_button);
        TextView mUserNameText = view.findViewById(R.id.username_textbar);
        mUserNameText.setText(mUserPrefs.getString("username", ""));
        mPlayButton.setOnClickListener(v -> {
            String userName = mUserNameText.getText().toString();
            mUserPrefs.edit().putString("username", userName).apply();
            PojavProfile.setCurrentProfile(v.getContext(), userName);
            ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
