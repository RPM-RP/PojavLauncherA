package net.kdt.pojavlaunch;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;

public class PermissionHandler implements ActivityResultCallback<Boolean> {
    private final AppCompatActivity mHostActivity;
    private final ActivityResultLauncher<String> mRequestPermissionLauncher;
    private WeakReference<PermissionResult> mPermissionResultRequest;
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

    public void setSkipChecks(boolean skip) {
        mPermissionSkipPrefs.edit().putBoolean(mPermissionIdentifier, skip).apply();
    }

    public void checkPermission(PermissionResult result) {
        boolean hasPermission = checkForPermission();
        if(shouldSkipChecks() || hasPermission) {
            if(result != null) result.onPermissionRequestComplete(hasPermission);
            return;
        }

        if(ActivityCompat.shouldShowRequestPermissionRationale(
                mHostActivity,
                mPermissionIdentifier)) {
            showPermissionReasoning(result);
            return;
        }
        askForPermission(result);
    }

    private void showPermissionReasoning(PermissionResult permissionResult) {
        new AlertDialog.Builder(mHostActivity)
                .setTitle(reasoningTitle)
                .setMessage(reasoningText)
                .setPositiveButton(android.R.string.ok, (d, w) -> askForPermission(permissionResult))
                .setNegativeButton(android.R.string.cancel, (d, w)-> {
                    if(permissionResult != null) permissionResult.onPermissionRequestComplete(false);
                    handleNoPermission();
                })
                .show();
    }

    private void handleNoPermission() {
        setSkipChecks(true);
        Toast.makeText(mHostActivity, R.string.notification_permission_toast, Toast.LENGTH_LONG).show();
    }

    public boolean checkForPermission() {
        return ContextCompat.checkSelfPermission(mHostActivity, mPermissionIdentifier) != PackageManager.PERMISSION_DENIED;
    }

    public void askForPermission(PermissionResult permissionResult) {
        if(permissionResult != null) {
            mPermissionResultRequest = new WeakReference<>(permissionResult);
        }
        mRequestPermissionLauncher.launch(mPermissionIdentifier);
    }


    @Override
    public void onActivityResult(Boolean isPermissionAllowed) {
        if(!isPermissionAllowed) handleNoPermission();
        PermissionResult permissionResult = Tools.getWeakReference(mPermissionResultRequest);
        if(permissionResult != null) permissionResult.onPermissionRequestComplete(isPermissionAllowed);
    }
    public interface PermissionResult {
        void onPermissionRequestComplete(boolean success);
    }
}
