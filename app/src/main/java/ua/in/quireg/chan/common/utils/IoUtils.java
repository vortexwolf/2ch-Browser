package ua.in.quireg.chan.common.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.annotation.NonNull;

import org.apache.http.HttpEntity;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.List;

import timber.log.Timber;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.library.CancellableInputStream;
import ua.in.quireg.chan.common.library.ProgressInputStream;
import ua.in.quireg.chan.interfaces.ICancelled;
import ua.in.quireg.chan.interfaces.IProgressChangeListener;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.settings.ApplicationSettings;

public class IoUtils {

    @NonNull
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String convertStreamToString(InputStream stream) throws IOException {
        byte[] bytes = convertStreamToBytes(stream);

        return convertBytesToString(bytes);
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
            Timber.e(e);
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

        long resultSize = 0;
        for (File file : files) {
            // Recursive call if it's a directory
            if (file.isDirectory()) {
                resultSize += dirSize(file);
            } else {
                // Sum the file size in bytes
                resultSize += file.length();
            }
        }
        return resultSize;
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

    public static double getSizeInMegabytes(File folder) {
        long size = IoUtils.dirSize(folder);
        double allSizeMb = convertBytesToMb(size);

        return Math.round(allSizeMb * 100) / 100d;
    }

    public static File getSaveFilePath(Uri uri, ApplicationSettings settings) {
        String fileName = uri.getLastPathSegment();

        File dir = settings.getDownloadDirectory();

        return new File(dir, fileName);
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = MediaColumns.DATA;
        final String[] projection = {column};

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
                final String docId = CompatibilityUtils.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = CompatibilityUtils.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = CompatibilityUtils.getDocumentId(uri);
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
                final String[] selectionArgs = new String[]{split[1]};

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

        return CompatibilityUtils.isDocumentUri(context, uri);
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

    public static String convertHttpEntityToString(HttpEntity entity) {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream((int) entity.getContentLength());
        try {
            entity.writeTo(out);
        } catch (IOException e) {
            return null;
        }

        return new String(out.toByteArray());
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
            double realScale = Math.max(size.x, size.y) / (double) maxDimension;
            double roundedScale = Math.pow(2, Math.ceil(Math.log(realScale) / Math.log(2)));
            scale = (int) roundedScale; // 2, 4, 8, 16
        }

        // Decode with inSampleSize
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inSampleSize = scale;

        return BitmapFactory.decodeFile(file.getAbsolutePath(), o);
    }

    private static byte[] getJpegInfoBytes(File file) {
        FileInputStream fis = null;
        byte[] bytes = null;
        try {
            fis = new FileInputStream(file);
            if (fis.read() != 255 || fis.read() != 216) {
                return null; // not JPEG
            }

            while (fis.read() == 255) {
                int marker = fis.read();
                int len = fis.read() << 8 | fis.read();

                // 192-207, except 196, 200 and 204
                if (marker >= 192 && marker <= 207 && marker != 196 && marker != 200 && marker != 204) {
                    bytes = new byte[len - 2];
                    fis.read(bytes, 0, bytes.length);
                    break;
                } else {
                    fis.skip(len - 2);
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            IoUtils.closeStream(fis);
        }

        return bytes;
    }

    public static boolean isNonStandardGrayscaleImage(File file) {
        byte[] bytes = IoUtils.getJpegInfoBytes(file);
        if (bytes == null) {
            return false;
        }

        // read subsampling information
        byte numberF = bytes[5]; // byte #6
        String[] samplings = new String[numberF];

        for (int i = 0; i < numberF; i++) {
            int hv = bytes[7 + i * numberF]; // byte #8, #10...

            int h = hv >> 4;
            int v = hv & 0x0F;
            samplings[i] = h + "x" + v;
        }

        return samplings.length == 1 && !samplings[0].equals("1x1");
    }

    public static boolean areEqual(List<BoardModel> first, List<BoardModel> second) {

        if (first == null || second == null || first.isEmpty() || second.isEmpty()) {
            return false;
        }

        for (BoardModel modelA : first) {
            boolean matchFound = false;
            for (BoardModel modelB : second) {
                if (modelA.getId().equals(modelB.getId())) {
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) return false;
        }
        return true;
    }

}
