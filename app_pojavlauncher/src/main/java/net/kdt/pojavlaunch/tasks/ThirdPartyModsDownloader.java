package net.kdt.pojavlaunch.tasks;

import static net.kdt.pojavlaunch.PojavApplication.sExecutorService;

import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.DownloadUtils;
import net.kdt.pojavlaunch.utils.HashGenerator;
import net.kdt.pojavlaunch.value.ThirdPartyMod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class ThirdPartyModsDownloader extends DownloaderBase {
    private static final String MOD_LIST_URL = "https://rpmserver.com/updater/mobile/downloads.json";
    private final File mModsList = new File(Tools.DIR_CACHE, "modsList.json");
    private final File mModsFolder = new File(Tools.DIR_GAME_NEW, "mods");

    public Future<?> runDownloader(AsyncMinecraftDownloader.DoneListener doneListener) {
        return sExecutorService.submit(()->{
            try {
                download();
                doneListener.onDownloadDone();
            }catch (Exception e) {
                ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK);
                doneListener.onDownloadFailed(e);
            }
        });
    }

    private void download() throws Exception {
        ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.newdl_starting);
        reset();
        ThirdPartyMod[] modsList = readModsList();
        verifyMods(modsList);
        for(ThirdPartyMod thirdPartyMod : modsList) {
            scheduleDownload(new DownloaderTask(
                    new File(mModsFolder, thirdPartyMod.id + ".jar"),
                    thirdPartyMod.url, HashGenerator.SHA256_GENERATOR,
                    thirdPartyMod.hash, thirdPartyMod.size,
                    false, this)
            );
        }
        performScheduledDownloads(ProgressLayout.INSTALL_MODPACK, R.string.moddl_downloading);
        ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK);
    }

    private void verifyMods(ThirdPartyMod[] modsList) throws IOException{
        List<String> modsFileNames = new ArrayList<>(modsList.length);
        for(ThirdPartyMod thirdPartyMod: modsList) {
            modsFileNames.add(thirdPartyMod.id + ".jar");
        }
        File[] filesToRemove = mModsFolder.listFiles((dir,fileName)->!modsFileNames.contains(fileName));
        if(filesToRemove == null) throw new IOException("I/O error");
        Log.i("TPMdosDownloader", "Scheduled "+filesToRemove.length+ " files for cleanup");
        cleanUp(filesToRemove);
    }

    private void cleanUp(File[] fileList) throws IOException{
        if(fileList == null) throw new IOException();
        for(File file : fileList) {
            if(file.isDirectory()) {
                cleanUp(file.listFiles());
            }
            if(!file.delete()) throw new IOException();
        }
    }

    private ThirdPartyMod[] readModsList() throws IOException, DownloadUtils.ParseException {
        return DownloadUtils.downloadStringCached(MOD_LIST_URL, "modsList", input -> {
            try {
                return Tools.GLOBAL_GSON.fromJson(input, ThirdPartyMod[].class);
            }catch (JsonSyntaxException e) {
                throw new DownloadUtils.ParseException(e);
            }
        });
    }
}
