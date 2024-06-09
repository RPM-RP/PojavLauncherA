package net.kdt.pojavlaunch.tasks;

import static net.kdt.pojavlaunch.PojavApplication.sExecutorService;
import static net.kdt.pojavlaunch.utils.DownloadUtils.downloadString;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import net.kdt.pojavlaunch.JMinecraftVersionList;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/** Class getting the version list, and that's all really */
public class AsyncVersionList {

    public void getVersionList(@Nullable VersionDoneListener listener, boolean secondPass){
        sExecutorService.execute(() -> {
            File versionFile = new File(Tools.DIR_DATA + "/version_list.json");
            JMinecraftVersionList versionList = null;
            try{
                if(!versionFile.exists() || (System.currentTimeMillis() > versionFile.lastModified() + 86400000 )){
                    versionList = downloadVersionList(LauncherPreferences.PREF_VERSION_REPOS);
                }
            }catch (Exception e){
                Log.e("AsyncVersionList", "Refreshing version list failed :" + e);
                e.printStackTrace();
            }

            // Fallback when no network or not needed
            if (versionList == null) {
                try {
                    versionList = Tools.GLOBAL_GSON.fromJson(new JsonReader(new FileReader(versionFile)), JMinecraftVersionList.class);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (JsonIOException | JsonSyntaxException e) {
                    e.printStackTrace();
                    versionFile.delete();
                    if(!secondPass)
                        getVersionList(listener, true);
                }
            }

            if(listener != null)
                listener.onVersionDone(versionList);
        });
    }


    @SuppressWarnings("SameParameterValue")
    private JMinecraftVersionList downloadVersionList(String mirrors){
        JMinecraftVersionList list = null;
        try{
            String[] mirrorsArray = mirrors.split(";");
            JMinecraftVersionList[] versionLists = new JMinecraftVersionList[mirrorsArray.length];
            int totalLength = 0;
            for(int i = 0; i < mirrorsArray.length; i++) {
                String mirror = mirrorsArray[i];
                Log.i("ExtVL", "Syncing to external: " + mirror);
                String jsonString = downloadString(mirror);
                JMinecraftVersionList versionList = Tools.GLOBAL_GSON.fromJson(jsonString, JMinecraftVersionList.class);
                totalLength += versionList.versions.length;
                Log.i("ExtVL","Downloaded the version list, len=" + versionList.versions.length);
                versionLists[i] = versionList;
            }
            list = new JMinecraftVersionList();
            list.latest = new HashMap<>();
            list.versions = new JMinecraftVersionList.Version[totalLength];
            int offset = 0;
            for(JMinecraftVersionList versionList : versionLists) {
                int copyLen = versionList.versions.length;
                System.arraycopy(versionList.versions, 0, list.versions, offset, copyLen);
                offset += copyLen;
            }
            // Then save the version list
            //TODO make it not save at times ?
            FileOutputStream fos = new FileOutputStream(Tools.DIR_DATA + "/version_list.json");
            fos.write(Tools.GLOBAL_GSON.toJson(list).getBytes());
            fos.close();
        }catch (IOException e){
            Log.e("AsyncVersionList", e.toString());
        }
        return list;
    }

    /** Basic listener, acting as a callback */
    public interface VersionDoneListener{
        void onVersionDone(JMinecraftVersionList versions);
    }

}
