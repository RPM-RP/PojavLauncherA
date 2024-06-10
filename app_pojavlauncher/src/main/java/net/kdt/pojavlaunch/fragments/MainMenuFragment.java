package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.PojavProfile;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;

public class MainMenuFragment extends Fragment {
    public static final String TAG = "MainMenuFragment";


    public MainMenuFragment(){
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button mPlayButton = view.findViewById(R.id.play_button);
        TextView mUserNameText = view.findViewById(R.id.username_textbar);
        mPlayButton.setOnClickListener(v -> {
            PojavProfile.setCurrentProfile(v.getContext(), mUserNameText.getText().toString());
            ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
