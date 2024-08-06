package net.kdt.pojavlaunch.utils;

import android.util.Log;

import androidx.annotation.Nullable;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.zipvalidator.ZipValidator;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.zip.ZipException;

@SuppressWarnings("IOStreamConstructor")
public class DownloadUtils {
    public static final String USER_AGENT = Tools.APP_NAME;

    public static void download(String url, OutputStream os) throws IOException {
        download(new URL(url), os);
    }

    public static void download(URL url, OutputStream os) throws IOException {
        InputStream is = null;
        try {
            // System.out.println("Connecting: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setConnectTimeout(10000);
            conn.setDoInput(true);
            conn.connect();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Server returned HTTP " + conn.getResponseCode()
                        + ": " + conn.getResponseMessage());
            }
            is = conn.getInputStream();
            IOUtils.copy(is, os);
        } catch (IOException e) {
            throw new IOException("Unable to download from " + url, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String downloadString(String url) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        download(url, bos);
        bos.close();
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }

    public static void downloadFile(String url, File out) throws IOException {
        FileUtils.ensureParentDirectory(out);
        try (FileOutputStream fileOutputStream = new FileOutputStream(out)) {
            download(url, fileOutputStream);
        }
    }

    public static void downloadFileMonitored(String urlInput, File outputFile, @Nullable byte[] buffer,
                                             Tools.DownloaderFeedback monitor) throws IOException {
        FileUtils.ensureParentDirectory(outputFile);

        HttpURLConnection conn = (HttpURLConnection) new URL(urlInput).openConnection();
        InputStream readStr = conn.getInputStream();
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            int current;
            int overall = 0;
            int length = conn.getContentLength();

            if (buffer == null) buffer = new byte[65535];

            while ((current = readStr.read(buffer)) != -1) {
                overall += current;
                fos.write(buffer, 0, current);
                monitor.updateProgress(overall, length);
            }
            conn.disconnect();
        }

    }

    public static <T> T downloadStringCached(String url, String cacheName, ParseCallback<T> parseCallback) throws IOException, ParseException{
        File cacheDestination = new File(Tools.DIR_CACHE, "string_cache/"+cacheName);
        if(cacheDestination.isFile() &&
                cacheDestination.canRead() &&
                System.currentTimeMillis() < (cacheDestination.lastModified() + 86400000)) {
            try {
                String cachedString = Tools.read(new FileInputStream(cacheDestination));
                return parseCallback.process(cachedString);
            }catch(IOException e) {
                Log.i("DownloadUtils", "Failed to read the cached file", e);
            }catch (ParseException e) {
                Log.i("DownloadUtils", "Failed to parse the cached file", e);
            }
        }
        String urlContent = DownloadUtils.downloadString(url);
        // if we download the file and fail parsing it, we will yeet outta there
        // and not cache the unparseable sting. We will return this after trying to save the downloaded
        // string into cache
        T parseResult = parseCallback.process(urlContent);

        boolean tryWriteCache;
        if(cacheDestination.exists()) {
            tryWriteCache = cacheDestination.canWrite();
        } else {
            tryWriteCache = FileUtils.ensureParentDirectorySilently(cacheDestination);
        }

        if(tryWriteCache) try {
            Tools.write(cacheDestination.getAbsolutePath(), urlContent);
        }catch(IOException e) {
            Log.i("DownloadUtils", "Failed to cache the string", e);
        }
        return parseResult;
    }

    private static <T> T downloadFile(Callable<T> downloadFunction) throws IOException{
        try {
            return downloadFunction.call();
        } catch (IOException e){
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean verifyFile(File file, String hash, HashGenerator hashGenerator) throws IOException{
        boolean fileExists = file.exists();
        if(hash == null && isZipFile(file)) {
            return fileExists && verifyZipFile(file);
        }
        return fileExists && Tools.compareHash(file, hash, hashGenerator);
    }

    private static boolean isZipFile(File file) {
        String fileName = file.getName();
        return fileName.endsWith(".jar") || fileName.endsWith(".zip");
    }

    private static boolean verifyZipFile(File file) throws IOException{
        ZipValidator zipValidator = new ZipValidator(file);
        try {
            zipValidator.validate();
            return true;
        }catch (InterruptedException e) {
            IOException ioexception = new InterruptedIOException("Rethrown InterruptedException as IO exception");
            ioexception.initCause(e);
            throw ioexception;
        }catch (ZipException ignored) {} // Try not to cause too much fuss if it's an actual validation error
        return false;
    }

    @SuppressWarnings({"UnusedReturnValue"})
    public static <T> T ensureSha1(File outputFile, @Nullable String hash, Callable<T> downloadFunction) throws IOException {
        return ensureHash(outputFile, hash, downloadFunction, HashGenerator.SHA1_GENERATOR);
    }

    public static <T> T ensureHash(File outputFile, @Nullable String hash, Callable<T> downloadFunction, HashGenerator hashGenerator) throws IOException {
        // Skip if needed
        if(hash == null) {
            // If the file exists and we don't know it's SHA1, don't try to redownload it.
            // Unless it's a zip, which we can validate locally
            if(outputFile.exists() && !isZipFile(outputFile)) return null;
            else return downloadFile(downloadFunction);
        }

        int attempts = 0;
        boolean fileOkay = verifyFile(outputFile, hash, hashGenerator);
        T result = null;
        while (attempts < 5 && !fileOkay){
            attempts++;
            downloadFile(downloadFunction);
            fileOkay = verifyFile(outputFile, hash, hashGenerator);
            Log.i("DU", "Download attempt "+attempts + " result: "+fileOkay);
        }
        Log.i("DU", "Complete, attempts: "+attempts+" okay: "+fileOkay);
        if(!fileOkay) throw new SHA1VerificationException("Hash verifcation failed after 5 download attempts ("+outputFile.getName()+")");
        return result;
    }

    public interface ParseCallback<T> {
        T process(String input) throws ParseException;
    }
    public static class ParseException extends Exception {
        public ParseException(Exception e) {
            super(e);
        }
    }

    public static class SHA1VerificationException extends IOException {
        public SHA1VerificationException(String message) {
            super(message);
        }
    }
}

