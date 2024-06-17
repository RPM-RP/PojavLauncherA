package net.kdt.pojavlaunch.tasks;

import android.util.Log;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.DownloadUtils;
import net.kdt.pojavlaunch.utils.HashGenerator;

import java.io.File;

public class DownloaderTask implements Runnable, Tools.DownloaderFeedback {
    private static final ThreadLocal<byte[]> sThreadLocalDownloadBuffer = new ThreadLocal<>();
    /*package-private*/ final File targetPath;
    private final String mTargetUrl;
    private final HashGenerator mHashGenerator;
    private String mTargetHash;
    private final boolean mSkipIfFailed;
    private int mLastCurr;
    private final long mDownloadSize;
    private final DownloaderBase mProgressData;

    public DownloaderTask(File targetPath, String targetUrl, HashGenerator hashGenerator, String targetSha1,
                          long downloadSize, boolean skipIfFailed, DownloaderBase progressData) {
        this.targetPath = targetPath;
        this.mTargetUrl = targetUrl;
        this.mHashGenerator = hashGenerator;
        this.mTargetHash = targetSha1;
        this.mDownloadSize = downloadSize;
        this.mSkipIfFailed = skipIfFailed;
        this.mProgressData = progressData;
    }

    @Override
    public void run() {
        try {
            runCatching();
        }catch (Exception e) {
            mProgressData.downloaderThreadException.set(e);
        }
    }

    private void runCatching() throws Exception {
        if(Tools.isValidString(mTargetHash)) {
            verifyFileHash();
        }else {
            mTargetHash = null; // Nullify SHA1 as DownloadUtils.ensureSha1 only checks for null,
            // not for string validity
            if(targetPath.exists()) finishWithoutDownloading();
            else downloadFile();
        }
    }

    private void verifyFileHash() throws Exception {
        if(targetPath.isFile() && targetPath.canRead() && Tools.compareHash(targetPath, mTargetHash, mHashGenerator)) {
            finishWithoutDownloading();
        } else {
            Log.i("DownloaderTask", "Downloading" + targetPath.getName());
            // Rely on the download function to throw an IOE in case if the file is not
            // writable/not a file/etc...
            downloadFile();
        }
    }

    private void downloadFile() throws Exception {
        try {
            DownloadUtils.ensureHash(targetPath, mTargetHash, () -> {
                performDownload(mTargetUrl, targetPath, getLocalBuffer(), this);
                return null;
            }, mHashGenerator);
        }catch (Exception e) {
            if(!mSkipIfFailed) throw e;
        }
        mProgressData.fileCounter.incrementAndGet();
    }

    protected void performDownload(String url, File path, byte[] buffer, Tools.DownloaderFeedback monitor) throws Exception {
        DownloadUtils.downloadFileMonitored(url, path, buffer, monitor);
    }

    private void finishWithoutDownloading() {
        mProgressData.fileCounter.incrementAndGet();
        mProgressData.sizeCounter.addAndGet(mDownloadSize);
    }

    @Override
    public void updateProgress(int curr, int max) {
        mProgressData.sizeCounter.addAndGet(curr - mLastCurr);
        mLastCurr = curr;
    }

    private static byte[] getLocalBuffer() {
        byte[] tlb = sThreadLocalDownloadBuffer.get();
        if(tlb != null) return tlb;
        tlb = new byte[32768];
        sThreadLocalDownloadBuffer.set(tlb);
        return tlb;
    }
}
