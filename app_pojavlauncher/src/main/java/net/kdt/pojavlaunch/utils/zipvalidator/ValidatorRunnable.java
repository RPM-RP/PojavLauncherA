package net.kdt.pojavlaunch.utils.zipvalidator;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class ValidatorRunnable implements Runnable {
    private final ZipValidator mParentValidator;
    private final ZipEntry mZipEntry;
    private ThreadData mThreadData;
    public ValidatorRunnable(ZipValidator parentValidator, ZipEntry zipEntry) {
        this.mParentValidator = parentValidator;
        this.mZipEntry = zipEntry;
    }



    private void validate(long crc, long size, InputStream inputStream) throws IOException {
        long totalSize = 0; int readCount;
        byte[] buffer = mThreadData.buffer;
        CRC32 crc32 = mThreadData.crc;
        crc32.reset();
        while((readCount = inputStream.read(buffer)) != -1) {
            crc32.update(buffer, 0, readCount);
            totalSize += readCount;
        }
        if(size != totalSize)
            throw new ZipException("ZIP file size incorrect for "+mZipEntry.getName());
        if(crc != crc32.getValue())
            throw new ZipException("ZIP file CRC32 incorrect for "+mZipEntry.getName());
    }

    @Override
    public void run() {
        try {
            mThreadData = mParentValidator.getThreadData();
            if(mThreadData == null) return;
            long crc = mZipEntry.getCrc();
            long size = mZipEntry.getSize();
            validate(crc, size, mThreadData.zipFile.getInputStream(mZipEntry));
        }catch (IOException e) {
            mParentValidator.validatorException.set(e);
        }
    }
}
