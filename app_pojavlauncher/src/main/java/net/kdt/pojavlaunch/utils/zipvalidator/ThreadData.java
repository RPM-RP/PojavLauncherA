package net.kdt.pojavlaunch.utils.zipvalidator;

import java.io.File;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.ZipFile;

public class ThreadData {
    public final ZipFile zipFile;
    public final byte[] buffer;
    public final CRC32 crc;

    public ThreadData(File file) throws IOException {
        zipFile = new ZipFile(file);
        buffer = new byte[65535];
        crc = new CRC32();
    }
    public void close() throws IOException{
        zipFile.close();
    }
}
