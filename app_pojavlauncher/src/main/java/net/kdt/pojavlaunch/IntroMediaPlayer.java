package net.kdt.pojavlaunch;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class IntroMediaPlayer extends TextureView implements TextureView.SurfaceTextureListener, MediaPlayer.OnCompletionListener {
    private final MediaPlayer mMediaPlayer = new MediaPlayer();

    public IntroMediaPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSurfaceTextureListener(this);
    }

    public IntroMediaPlayer(@NonNull Context context) {
        super(context);
        setSurfaceTextureListener(this);
    }

    public IntroMediaPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setSurfaceTextureListener(this);
    }

    public IntroMediaPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setSurfaceTextureListener(this);
    }

    private void setupPlayer(SurfaceTexture surfaceTexture) {
        mMediaPlayer.reset();
        Uri videoUri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(getContext().getPackageName())
                .appendPath(Integer.toString(R.raw.rpm_mobile_loading))
                .build();
        try {
            mMediaPlayer.setDataSource(getContext(), videoUri);
            mMediaPlayer.setSurface(new Surface(surfaceTexture));
            mMediaPlayer.prepare();
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.start();
        } catch (Exception e) {
            Tools.showError(getContext(), e);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        mMediaPlayer.reset();
        setupPlayer(surfaceTexture);
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        mMediaPlayer.reset();
        setupPlayer(surfaceTexture);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        mMediaPlayer.reset();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.seekTo(12*1000);
    }
}
