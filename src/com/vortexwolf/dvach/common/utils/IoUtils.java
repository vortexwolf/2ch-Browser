package com.vortexwolf.dvach.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.net.Uri;
import android.os.Environment;

import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class IoUtils {

    public static String convertStreamToString(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        copyStream(stream, output);
        
        String result = output.toString(Constants.UTF8_CHARSET.name());
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
    
    public static long dirSize(File dir) {
        if (dir == null || !dir.exists()) {
            return 0;
        }

        long result = 0;
        for (File file : dir.listFiles()) {
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
            for (File file : path.listFiles()) {
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
            for (File file : path.listFiles()) {
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
