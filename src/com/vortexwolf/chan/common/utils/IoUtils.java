package com.vortexwolf.chan.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import android.net.Uri;
import android.os.Environment;

import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.library.CancellableInputStream;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.library.ProgressInputStream;
import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.IProgressChangeListener;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class IoUtils {
    public static final String TAG = "IoUtils";

    public static String convertStreamToString(InputStream stream) throws IOException {
        byte[] bytes = convertStreamToBytes(stream);
        
        String result = convertBytesToString(bytes);
        return result;
    }
    
    public static byte[] convertStreamToBytes(InputStream stream) throws IOException {
        if (stream == null) {
            return null;
        }
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        copyStream(stream, output);

        return output.toByteArray();
    }
    
    public static String convertBytesToString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        
        String result;
        try {
            result = new String(bytes, Constants.UTF8_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        
        return result;
    }
    
    public static void copyStream(InputStream from, OutputStream to) throws IOException {
        byte data[] = new byte[8192];
        int count;

        while ((count = from.read(data)) != -1) {
            to.write(data, 0, count);
        }
        
        from.close();
    }
    
    public static InputStream modifyInputStream(InputStream stream, long contentLength, IProgressChangeListener listener, ICancelled task) throws IllegalStateException, IOException {
        if (listener != null) {
            listener.setContentLength(contentLength);

            ProgressInputStream pin = new ProgressInputStream(stream);
            pin.addProgressChangeListener(listener);
            
            stream = pin;
        }

        if (task != null) {
            stream = new CancellableInputStream(stream, task);
        }

        return stream;
    }
    
    public static void closeStream(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }
    
    public static long dirSize(File dir) {
        if (dir == null || !dir.exists()) {
            return 0;
        }
        
        File[] files = dir.listFiles();
        if (files == null) {
            return 0;
        }

        long result = 0;
        for (File file : files) {
            // Recursive call if it's a directory
            if (file.isDirectory()) {
                result += dirSize(file);
            } else {
                // Sum the file size in bytes
                result += file.length();
            }
        }
        return result; // return the file size
    }

    public static void deleteDirectory(File path) {
        if (path != null && path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return;
            }
            
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }

            path.delete();
        }
    }

    public static long freeSpace(File path, long bytesToRelease) {
        long released = 0;

        if (path != null && path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return 0;
            }
            
            for (File file : files) {
                if (file.isDirectory()) {
                    released += freeSpace(file, bytesToRelease);
                } else {
                    released += file.length();
                    file.delete();
                }

                if (released > bytesToRelease) {
                    break;
                }
            }
        }

        return released;
    }

    public static double getSizeInMegabytes(File folder1, File folder2) {
        long size1 = IoUtils.dirSize(folder1);
        long size2 = IoUtils.dirSize(folder2);

        double allSizeMb = convertBytesToMb(size1 + size2);
        double result = Math.round(allSizeMb * 100) / 100d;

        return result;
    }

    public static File getSaveFilePath(Uri uri, ApplicationSettings settings) {
        String fileName = uri.getLastPathSegment();

        File dir = new File(Environment.getExternalStorageDirectory(), settings.getDownloadPath());
        dir.mkdirs();
        File file = new File(dir, fileName);

        return file;
    }

    public static double convertBytesToMb(long bytes) {
        return bytes / 1024d / 1024d;
    }

    public static long convertMbToBytes(double mb) {
        return (long) (mb * 1024 * 1024);
    }
}
