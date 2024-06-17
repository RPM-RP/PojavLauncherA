package net.kdt.pojavlaunch.tasks;

import static net.kdt.pojavlaunch.PojavApplication.sExecutorService;

import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.DownloadUtils;
import net.kdt.pojavlaunch.utils.FileUtils;
import net.kdt.pojavlaunch.utils.HashGenerator;
import net.kdt.pojavlaunch.value.ResourcepackInfo;
import net.kdt.pojavlaunch.value.ThirdPartyConfig;
import net.kdt.pojavlaunch.value.ThirdPartyMod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class ThirdPartyModsDownloader extends DownloaderBase {
    private static final String MOD_LIST_URL = "https://rpmserver.com/updater/mobile/downloads.json";
    private static final String CONFIG_LIST_URL = "https://rpmserver.com/updater/mobile/configs.json";
    private static final String RESOURCE_PACK_URL = "https://rpmserver.com/updater/mobile/resourcePack.json";
    private final File mModsFolder = new File(Tools.DIR_GAME_NEW, "mods");
    private final File mResourcepackLocation = new File(Tools.DIR_GAME_NEW, "resourcepacks/ServerResourcepack.zip");

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
        ThirdPartyMod[] modsList = readJson(MOD_LIST_URL, ThirdPartyMod[].class);
        verifyMods(modsList);
        for(ThirdPartyMod thirdPartyMod : modsList) {
            scheduleDownload(new DownloaderTask(
                    new File(mModsFolder, thirdPartyMod.id + ".jar"),
                    thirdPartyMod.url, HashGenerator.SHA256_GENERATOR,
                    thirdPartyMod.hash, thirdPartyMod.size,
                    false, this
            ));
        }
        ThirdPartyConfig[] configsList = readJson(CONFIG_LIST_URL, ThirdPartyConfig[].class);
        for(ThirdPartyConfig thirdPartyConfig : configsList) {
            scheduleDownload(new DownloaderTask(
                    new File(Tools.DIR_GAME_NEW, thirdPartyConfig.path),
                    thirdPartyConfig.url, HashGenerator.SHA1_GENERATOR,
                    thirdPartyConfig.sha1, thirdPartyConfig.size,
                    false, this
            ));
        }
        ResourcepackInfo resourcepackInfo = readJson(RESOURCE_PACK_URL, ResourcepackInfo.class);
        scheduleDownload(new DownloaderTask(
                mResourcepackLocation, resourcepackInfo.url, HashGenerator.SHA256_GENERATOR,
                resourcepackInfo.hash, resourcepackInfo.size, false, this
        ));
        performScheduledDownloads(ProgressLayout.INSTALL_MODPACK, R.string.moddl_downloading);
        ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK);
    }

    private void verifyMods(ThirdPartyMod[] modsList) throws IOException{
        List<String> modsFileNames = new ArrayList<>(modsList.length);
        for(ThirdPartyMod thirdPartyMod: modsList) {
            modsFileNames.add(thirdPartyMod.id + ".jar");
        }
        FileUtils.ensureDirectory(mModsFolder);
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

    private static <T> T readJson(String url, Class<T> type) throws IOException, DownloadUtils.ParseException {
        return DownloadUtils.downloadStringCached(url, type.getCanonicalName(), input -> {
            try {
                return Tools.GLOBAL_GSON.fromJson(input, type);
            }catch (JsonSyntaxException e) {
                throw new DownloadUtils.ParseException(e);
            }
        });
    }
}
