package net.kdt.pojavlaunch;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.lang.ref.WeakReference;
import android.os.Handler;

public class PermissionHandler implements ActivityResultCallback<Boolean> {
    private static final Handler mPermissionRequestHandler = new Handler(Looper.getMainLooper());
    private final AppCompatActivity mHostActivity;
    private final ActivityResultLauncher<String> mRequestPermissionLauncher;
    private WeakReference<Runnable> mRequestPermissionRunnable;
    private final String mPermissionIdentifier;
    private final SharedPreferences mPermissionSkipPrefs;
    public int reasoningTitle;
    public int reasoningText;

    public PermissionHandler(AppCompatActivity activity, String permissionId) {
        mHostActivity = activity;
        mPermissionIdentifier = permissionId;
        mPermissionSkipPrefs = activity.getSharedPreferences("permissions_skip", Context.MODE_PRIVATE);
        mRequestPermissionLauncher =
                mHostActivity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), this);
    }

    private boolean shouldSkipChecks() {
        return mPermissionSkipPrefs.getBoolean(mPermissionIdentifier, false);
    }

    private void setSkipChecks(boolean skip) {
        mPermissionSkipPrefs.edit().putBoolean(mPermissionIdentifier, skip).apply();
    }

    public void checkPermission() {
        if(shouldSkipChecks() ||
                checkForPermission()) {
            return;
        }

        if(ActivityCompat.shouldShowRequestPermissionRationale(
                mHostActivity,
                mPermissionIdentifier)) {
            showPermissionReasoning();
            return;
        }
        askForPermission(null);
    }

    private void showPermissionReasoning() {
        new AlertDialog.Builder(mHostActivity)
                .setTitle(reasoningTitle)
                .setMessage(reasoningText)
                .setPositiveButton(android.R.string.ok, (d, w) -> askForPermission(null))
                .setNegativeButton(android.R.string.cancel, (d, w)-> handleNoPermission())
                .show();
    }

    private void handleNoPermission() {
        setSkipChecks(true);
        Toast.makeText(mHostActivity, R.string.notification_permission_toast, Toast.LENGTH_LONG).show();
    }

    public boolean checkForPermission() {
        return ContextCompat.checkSelfPermission(mHostActivity, mPermissionIdentifier) != PackageManager.PERMISSION_DENIED;
    }

    public void askForPermission(Runnable onSuccessRunnable) {
        if(onSuccessRunnable != null) {
            mRequestPermissionRunnable = new WeakReference<>(onSuccessRunnable);
        }
        mRequestPermissionLauncher.launch(mPermissionIdentifier);
    }

    @Override
    public void onActivityResult(Boolean isPermissionAllowed) {
        if(!isPermissionAllowed) handleNoPermission();
        else {
            Runnable runnable = Tools.getWeakReference(mRequestPermissionRunnable);
            if(runnable != null) runnable.run();
        }
    }
}
