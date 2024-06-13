package net.kdt.pojavlaunch.tasks;

import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class DownloaderBase {
    /*package-private*/ AtomicReference<Exception> downloaderThreadException;
    /*package-private*/ AtomicInteger fileCounter;
    /*package-private*/ AtomicLong sizeCounter;

    protected ArrayList<DownloaderTask> mScheduledDownloadTasks;
    protected long mDownloadFileCount;

    protected void reset() {
        sizeCounter = new AtomicLong(0);
        fileCounter = new AtomicInteger(0);
        downloaderThreadException = new AtomicReference<>(null);
        mScheduledDownloadTasks = new ArrayList<>();
        mDownloadFileCount = 0;
    }

    protected boolean performScheduledDownloads(String progressTag, int formatString) throws Exception{
        ArrayBlockingQueue<Runnable> taskQueue =
                new ArrayBlockingQueue<>(mScheduledDownloadTasks.size(), false);
        ThreadPoolExecutor downloaderPool =
                new ThreadPoolExecutor(4, 4, 500, TimeUnit.MILLISECONDS, taskQueue);

        // I have tried pre-filling the queue directly instead of doing this, but it didn't work.
        // What a shame.
        for(DownloaderTask scheduledTask : mScheduledDownloadTasks) downloaderPool.execute(scheduledTask);
        downloaderPool.shutdown();

        try {
            Exception thrownException;
            while ((thrownException = downloaderThreadException.get()) == null &&
                    !downloaderPool.awaitTermination(33, TimeUnit.MILLISECONDS)) {
                long dlFileCounter = fileCounter.get();
                int progress = (int)((dlFileCounter * 100L) / mDownloadFileCount);
                ProgressLayout.setProgress(progressTag, progress,
                        formatString, dlFileCounter,
                        mDownloadFileCount, (double)sizeCounter.get() / (1024d * 1024d));
            }
            if(thrownException != null) {
                throw thrownException;
            }
        }catch (InterruptedException e) {
            // Interrupted while waiting, which means that the download was cancelled.
            // Kill all downloading threads immediately, and ignore any exceptions thrown by them
            downloaderPool.shutdownNow();
            return false;
        }
        return true;
    }

    protected void scheduleDownload(DownloaderTask downloaderTask) throws IOException {
        FileUtils.ensureParentDirectory(downloaderTask.targetPath);
        mScheduledDownloadTasks.add(downloaderTask);
        mDownloadFileCount++;
    }
}
