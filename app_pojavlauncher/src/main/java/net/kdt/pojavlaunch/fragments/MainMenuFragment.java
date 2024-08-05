package net.kdt.pojavlaunch.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.PojavProfile;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.utils.DownloadUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

public class MainMenuFragment extends Fragment {
    private static final int[] backgrounds = new int[] {
            R.drawable.background1,
            R.drawable.background2,
            R.drawable.background3
    };
    private static final int[] overlays = new int[] {
            R.drawable.overlay1,
            R.drawable.overlay2,
            R.drawable.overlay3
    };
    private SharedPreferences mUserPrefs;
    private TextView mUserNameText;
    private TextView mOnlinePlayersText;
    private View mOnlineIcon;

    public MainMenuFragment(){
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mUserPrefs = view.getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        mUserNameText = view.findViewById(R.id.username_textbar);
        mUserNameText.setText(mUserPrefs.getString("username", ""));
        mUserNameText.addTextChangedListener(new TextSaver());
        mOnlinePlayersText = view.findViewById(R.id.online_total_view);
        mOnlineIcon = view.findViewById(R.id.online_icon);
        Button playButton = view.findViewById(R.id.play_button);
        playButton.setOnClickListener(v -> {
            String userName = mUserNameText.getText().toString();
            PojavProfile.setCurrentProfile(v.getContext(), userName);
            ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true);
        });
        View settingsButton = view.findViewById(R.id.button_settings);
        settingsButton.setOnClickListener(v -> {
            FragmentActivity activity = getActivity();
            if(activity == null) return;
            Tools.swapFragment(activity, AdvancedPreferenceFragment.class, AdvancedPreferenceFragment.TAG, null);
        });
        view.findViewById(R.id.social_youtube).setOnClickListener(new LinkOpenHandler("https://youtube.com/@rpmroleplay"));
        view.findViewById(R.id.social_vk).setOnClickListener(new LinkOpenHandler("https://vk.com/rpminfo"));
        view.findViewById(R.id.social_discord).setOnClickListener(new LinkOpenHandler("https://discord.gg/rpmroleplay"));
        view.findViewById(R.id.social_tiktok).setOnClickListener(new LinkOpenHandler("https://www.tiktok.com/@rpmroleplay"));
        view.findViewById(R.id.button_website).setOnClickListener(new LinkOpenHandler("https://rpmserver.com/"));
        Random random = new Random(System.nanoTime() * System.currentTimeMillis());
        int backgroundId = random.nextInt(3);
        ImageView backgroundView = view.findViewById(R.id.bg_image_view);
        backgroundView.setImageResource(backgrounds[backgroundId]);
        int overlayId = random.nextInt(3);
        ImageView overlayView = view.findViewById(R.id.overlay_image_view);
        overlayView.setImageResource(overlays[overlayId]);
        PojavApplication.sExecutorService.execute(this::srvStatusReadOnline);
    }

    private void srvStatusReadOnline() {
        int current, max;
        try {
            String serverStatusData = DownloadUtils.downloadString("https://api.mcsrvstat.us/3/"+ Tools.SERVER_IP);
            JSONObject jsonObject = new JSONObject(serverStatusData);
            if(!jsonObject.getBoolean("online")) return;
            JSONObject players = jsonObject.getJSONObject("players");
            current = players.getInt("online");
            max = players.getInt("max");
        }catch (IOException | JSONException e) {
            Log.w("MainMenu", "Failed to request server data", e);
            return;
        }

        Tools.runOnUiThread(()->{
            Context context = mOnlinePlayersText.getContext();
            String htmlString = context.getString(R.string.text_online_format,
                    current,max
            );
            Spanned spanned = Html.fromHtml(htmlString);
            mOnlinePlayersText.setText(spanned);
            mOnlineIcon.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private class LinkOpenHandler implements View.OnClickListener {
        private final Uri mUri;

        private LinkOpenHandler(String url) {
            this.mUri = Uri.parse(url);
        }
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(mUri);
            view.getContext().startActivity(intent);
        }
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
