package net.kdt.pojavlaunch.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class ParallelListener implements AsyncMinecraftDownloader.DoneListener{
    private final List<Future<?>> mTrackedTasks = new ArrayList<>();
    private final AsyncMinecraftDownloader.DoneListener mTopLevelListener;
    private int mCompletedTasks = 0;
    private boolean mIgnore = false;

    public ParallelListener(AsyncMinecraftDownloader.DoneListener mTopLevelListener) {
        this.mTopLevelListener = mTopLevelListener;
    }

    public void register(Future<?> downloaderFuture) {
        mTrackedTasks.add(downloaderFuture);
    }

    @Override
    public synchronized void onDownloadDone() {
        if(mIgnore) return;
        mCompletedTasks++;
        if(mCompletedTasks != mTrackedTasks.size()) return;
        mTopLevelListener.onDownloadDone();
    }

    @Override
    public synchronized void onDownloadFailed(Throwable throwable) {
        if(mIgnore) return;
        for(Future<?> taskFuture : mTrackedTasks) taskFuture.cancel(true);
        mIgnore = true;
        mTopLevelListener.onDownloadFailed(throwable);
    }
}
