package net.kdt.pojavlaunch.value.launcherprofiles;

import android.util.Log;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.Tools;

import java.io.File;

public class LauncherProfiles {
    private static MinecraftProfile mProfile;
    private static final File mProfileFile = new File(Tools.DIR_GAME_HOME, "profile.json");

    /** Reload the profile from the file, creating a default one if necessary */
    public static void load(){
        // Fill with default
        if(mProfileFile.exists() && mProfileFile.canRead()) {
            try {
                String profileContent = Tools.read(mProfileFile);
                mProfile = Tools.GLOBAL_GSON.fromJson(profileContent, MinecraftProfile.class);
            }catch (Exception e) {
                Log.w("LauncherProfiles", "Failed to read profile", e);
            }
        }
        if(mProfile == null) mProfile = MinecraftProfile.getDefaultProfile();
    }

    /** Apply the current configuration into a file */
    public static void write() {
        if(mProfile == null) return;
        if(!mProfileFile.canWrite()) return;
        try {
            String profileContent = Tools.GLOBAL_GSON.toJson(mProfile);
            Tools.write(mProfileFile.getAbsolutePath(), profileContent);
        }catch (Exception e) {
            Log.w("LauncherProfiles", "Failed to write profile", e);
        }
    }

    public static @NonNull MinecraftProfile getCurrentProfile() {
        if(mProfile == null) load();
        return mProfile;
    }
}
