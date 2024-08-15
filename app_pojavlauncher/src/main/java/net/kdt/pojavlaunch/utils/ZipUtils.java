package net.kdt.pojavlaunch.utils;

import net.kdt.pojavlaunch.utils.zipvalidator.ZipValidator;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZipUtils {
    /**
     * Gets an InputStream for a given ZIP entry, throwing an IOException if the ZIP entry does not
     * exist.
     * @param zipFile The ZipFile to get the entry from
     * @param entryPath The full path inside of the ZipFile
     * @return The InputStream provided by the ZipFile
     * @throws IOException if the entry was not found
     */
    public static InputStream getEntryStream(ZipFile zipFile, String entryPath) throws IOException{
        ZipEntry entry = zipFile.getEntry(entryPath);
        if(entry == null) throw new IOException("No entry in ZIP file: "+entryPath);
        return zipFile.getInputStream(entry);
    }

    /**
     * Extracts all files in a ZipFile inside of a given directory to a given destination directory
     * How to specify dirName:
     * If you want to extract all files in the ZipFile, specify ""
     * If you want to extract a single directory, specify its full path followed by a trailing /
     * @param zipFile The ZipFile to extract files from
     * @param dirName The directory to extract the files from
     * @param destination The destination directory to extract the files into
     * @throws IOException if it was not possible to create a directory or file extraction failed
     */
    public static void zipExtract(ZipFile zipFile, String dirName, File destination) throws IOException {
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

        int dirNameLen = dirName.length();
        while(zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = zipEntries.nextElement();
            String entryName = zipEntry.getName();
            if(!entryName.startsWith(dirName) || zipEntry.isDirectory()) continue;
            File zipDestination = new File(destination, entryName.substring(dirNameLen));
            FileUtils.ensureParentDirectory(zipDestination);
            try (InputStream inputStream = zipFile.getInputStream(zipEntry);
                 OutputStream outputStream = new FileOutputStream(zipDestination)) {
                IOUtils.copy(inputStream, outputStream);
            }
        }
    }

    /**
     * Check if {@code file} is a file that could be validated with the {@code validateFile} function.
     * Note that this only checks the file name.
     * @param file file for checking
     * @return whether it's a file that could be validated
     */
    public static boolean isValidatableZip(File file) {
        // It is our job to perform validation, so we need to check via the file extension.
        // The beginning of the file may be corrupted and not contain a ZIP header.
        String fileName = file.getName();
        return fileName.endsWith(".jar") || fileName.endsWith(".zip");
    }

    /**
     * Validate a ZIP file by checking each file against the CRC32 checksum.
     * @param file the target ZIP file for validation
     * @return whether or not the file passed validation
     * @throws IOException if an I/O error occured during validation
     */
    public static boolean validateFile(File file) throws IOException{
        ZipValidator validator = new ZipValidator(file);
        try {
            validator.validate();
            return true;
        }catch (InterruptedException e) {
            IOException exception = new InterruptedIOException();
            exception.initCause(e);
            throw exception;
        }catch (ZipException ignored) {}
        return false;
    }
}
