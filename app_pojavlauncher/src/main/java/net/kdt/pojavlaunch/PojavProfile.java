package net.kdt.pojavlaunch;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.kdt.pojavlaunch.value.MinecraftAccount;

public class PojavProfile {
	private static final String PROFILE_PREF = "pojav_profile";
	private static final String PROFILE_PREF_FILE = "file";

	public static SharedPreferences getPrefs(Context ctx) {
		return ctx.getSharedPreferences(PROFILE_PREF, Context.MODE_PRIVATE);
	}

    public static MinecraftAccount getCurrentProfileContent(@NonNull Context ctx, @Nullable String profileName) {
        return MinecraftAccount.load(profileName == null ? getCurrentProfileName(ctx) : profileName);
    }

    public static String getCurrentProfileName(Context ctx) {
        String name = getPrefs(ctx).getString(PROFILE_PREF_FILE, "");
        // A dirty fix
        if (!name.isEmpty() && name.startsWith(Tools.DIR_ACCOUNT_NEW) && name.endsWith(".json")) {
            name = name.substring(0, name.length() - 5).replace(Tools.DIR_ACCOUNT_NEW, "").replace(".json", "");
            setCurrentProfile(ctx, name);
        }
        return name;
    }
	
	public static void setCurrentProfile(@NonNull Context ctx, String name) {
		getPrefs(ctx).edit().putString(PROFILE_PREF_FILE, name != null ? name : "").apply();
	}
}
