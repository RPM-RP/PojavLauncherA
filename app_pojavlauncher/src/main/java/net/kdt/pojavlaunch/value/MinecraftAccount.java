package net.kdt.pojavlaunch.value;


import androidx.annotation.Keep;

import net.kdt.pojavlaunch.Tools;

import java.lang.ref.WeakReference;

@Keep
public class MinecraftAccount {
    private static WeakReference<MinecraftAccount> mAccount;
    public String accessToken = "0"; // access token
    public String clientToken = "0"; // clientID: refresh and invalidate
    public String profileId = "00000000-0000-0000-0000-000000000000"; // profile UUID, for obtaining skin
    public String username = "Steve";
    public String selectedVersion = "1.7.10";
    public String msaRefreshToken = "0";
    public String xuid;

    public static MinecraftAccount load(String name) {
        // For the purposes of this project, we do not need a full-on account system. So we will generate
        // a dummy account with the correctly set user name.
        MinecraftAccount account;
        if(mAccount == null || mAccount.get() == null) {
            account = new MinecraftAccount();
            account.accessToken = "0";
            account.clientToken = "0";
            account.profileId = "00000000-0000-0000-0000-000000000000";
            account.selectedVersion = Tools.GAME_VERSION;
            account.msaRefreshToken = "0";
            mAccount = new WeakReference<>(account);
        }else {
            account = mAccount.get();
        }
        account.username = name;
        return account;
    }
}
