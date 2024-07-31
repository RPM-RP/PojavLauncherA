package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.kdt.mcgui.MineButton;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceControlFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceExperimentalFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceJavaFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceMiscellaneousFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceVideoFragment;

// It's a lie, this is not advanced at all. I'd say its kinda shit, even
// But the release is in 24.5 hours so idc
public class AdvancedPreferenceFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = "AdvancedPreferenceFragment";
    private final ArrayMap<View, Class<? extends Fragment>> mButtonFragmentMap;
    public AdvancedPreferenceFragment() {
        super(R.layout.fragment_advanced_preferences);
        mButtonFragmentMap = new ArrayMap<>();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new PreferenceButtonInflater(view).inflate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mButtonFragmentMap.clear();
    }

    @Override
    public void onClick(View view) {
        Class<? extends Fragment> fragmentClass = mButtonFragmentMap.get(view);
        if(fragmentClass == null) return;
        getChildFragmentManager().beginTransaction()
                .replace(R.id.advancedPrefFragmentContainer, fragmentClass, null, null).commit();
    }


    private final class PreferenceButtonInflater {
        private final LinearLayout mButtonHost;
        private final LayoutInflater mInflater;
        public PreferenceButtonInflater(View view) {
            mButtonHost = view.findViewById(R.id.prefs_buttonHost);
            mInflater = LayoutInflater.from(view.getContext());
        }

        public void inflate() {
            addButton(LauncherPreferenceFragment.class, R.string.pref_main);
            addButton(LauncherPreferenceVideoFragment.class, R.string.pref_video);
            addButton(LauncherPreferenceControlFragment.class, R.string.pref_controls);
            addButton(LauncherPreferenceJavaFragment.class, R.string.pref_java);
            addButton(LauncherPreferenceMiscellaneousFragment.class, R.string.pref_misc);
            addButton(LauncherPreferenceExperimentalFragment.class, R.string.pref_experimental);
        }

        private void addButton(Class<? extends Fragment> fragment, int localeId) {
            MineButton button = (MineButton) mInflater.inflate(R.layout.button_settings, mButtonHost, false);
            button.setOnClickListener(AdvancedPreferenceFragment.this);
            button.setText(localeId);
            mButtonFragmentMap.put(button, fragment);
            mButtonHost.addView(button);
        }
    }
}
