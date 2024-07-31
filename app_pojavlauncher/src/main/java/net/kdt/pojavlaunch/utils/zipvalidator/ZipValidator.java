package net.kdt.pojavlaunch.utils.zipvalidator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZipValidator {
    private final List<ThreadData> sThreadDatas = Collections.synchronizedList(new ArrayList<>(2));
    private final ThreadLocal<ThreadData> sThreadData = new ThreadLocal<>();
    final AtomicReference<IOException> validatorException = new AtomicReference<>();
    private final File mFileForValidation;


    public ZipValidator(File fileForValidation) {
        mFileForValidation = fileForValidation;
    }

    public void validate() throws IOException, InterruptedException {
        try(ZipFile zipFile = new ZipFile(mFileForValidation)) {
            internalValidate(zipFile);
        }finally {
            for (ThreadData threadData : sThreadDatas) {
                threadData.close();
            }
        }
    }

    private void internalValidate(ZipFile zipFile) throws IOException, InterruptedException{
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        while(zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = zipEntries.nextElement();
            executorService.execute(new ValidatorRunnable(this, zipEntry));
        }
        executorService.shutdown();
        IOException validatorException = null;
        while(!executorService.awaitTermination(16, TimeUnit.MILLISECONDS)) {
            validatorException = this.validatorException.get();
            if(validatorException != null) break;
        }
        if(validatorException != null) {
            executorService.shutdownNow();
            throw validatorException;
        }
    }

    ThreadData getThreadData() throws IOException {
        ThreadData threadData = sThreadData.get();
        if(threadData != null) return threadData;
        threadData = new ThreadData(mFileForValidation);
        sThreadDatas.add(threadData);
        return threadData;
    }

}
