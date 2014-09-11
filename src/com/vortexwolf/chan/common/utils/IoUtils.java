package com.vortexwolf.chan.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;

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

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = MediaColumns.DATA;
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static String getPath(final Context context, final Uri uri) {
        // DocumentProvider
        if (isKitKatDocument(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = CompatibilityUtilsImpl.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = CompatibilityUtilsImpl.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = CompatibilityUtilsImpl.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = BaseColumns._ID + "=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static boolean isKitKatDocument(Context context, Uri uri) {
        if (Constants.SDK_VERSION < 19) {
            return false;
        }
        
        return CompatibilityUtilsImpl.isDocumentUri(context, uri);
    }
    
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean isLocal(String url) {
        if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
            return true;
        }
        return false;
    }

    public static File getFile(Context context, Uri uri) {
        if (uri != null) {
            String path = getPath(context, uri);
            if (path != null && isLocal(path)) {
                return new File(path);
            }
        }
        return null;
    }

    public static double convertBytesToMb(long bytes) {
        return bytes / 1024d / 1024d;
    }

    public static long convertMbToBytes(double mb) {
        return (long) (mb * 1024 * 1024);
    }
    
    public static String convertHttpEntityToString(HttpEntity entity){
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream((int)entity.getContentLength());
        try {
            entity.writeTo(out);
        } catch (IOException e) {
            return null;
        }
        
        String str = new String(out.toByteArray());
        return str;
    }
    
    public static Point getImageSize(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        return new Point(options.outWidth, options.outHeight);
    }
    
    public static Bitmap readBitmapFromFile(File file, double maxDimension) {
        int scale = 1;
        Point size = getImageSize(file);
        if (size.x > maxDimension || size.y > maxDimension) {
            double realScale = Math.max(size.x, size.y) / (double)maxDimension;
            double roundedScale = Math.pow(2, Math.ceil(Math.log(realScale) / Math.log(2)));
            scale = (int) roundedScale; // 2, 4, 8, 16
        }

        // Decode with inSampleSize
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inSampleSize = scale;
        
        Bitmap b = BitmapFactory.decodeFile(file.getAbsolutePath(), o);
        return b;
    }
    
}
