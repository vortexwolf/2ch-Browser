package httpimage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import timber.log.Timber;
import ua.in.quireg.chan.common.utils.IoUtils;
import ua.in.quireg.chan.services.CacheDirectoryManager;

/**
 * File system implementation of persistent storage for downloaded images.
 *
 * @author zonghai@gmail.com
 */
public class FileSystemPersistence implements BitmapCache {

    private final File mBaseDir;

    public FileSystemPersistence(CacheDirectoryManager cacheManager) {
        mBaseDir = cacheManager.getThumbnailsCacheDirectory();
    }

    @Override
    public void clear() {
        IoUtils.deleteDirectory(mBaseDir);
    }

    @Override
    public Bitmap loadData(String key) {
        File file = new File(mBaseDir, key);
        if(!file.exists()){
            return null;
        }
        return readBitmapFromFile(file);
    }

    @Override
    public void storeData(String key, Bitmap data) {
        try {
            File file = new File(mBaseDir, key);
            writeBitmapToFile(data, file);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private void writeBitmapToFile(Bitmap bitmap, File file) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } finally {
            IoUtils.closeStream(out);
        }
    }

    private Bitmap readBitmapFromFile(File file) {
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(fis);
            if (bitmap == null) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        } catch (FileNotFoundException e) {
            //No file, no cry.
        } finally {
            IoUtils.closeStream(fis);
        }
        return bitmap;
    }
}
