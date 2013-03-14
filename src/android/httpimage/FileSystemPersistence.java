package android.httpimage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.interfaces.ICacheDirectoryManager;

/**
 * File system implementation of persistent storage for downloaded images.
 * 
 * @author zonghai@gmail.com
 */
public class FileSystemPersistence implements BitmapCache {

    private static final String TAG = "ThumbnailFSPersistent";

    private final ICacheDirectoryManager mCacheManager;
    private final File mBaseDir;

    public FileSystemPersistence(ICacheDirectoryManager cacheManager) {
        this.mCacheManager = cacheManager;

        this.mBaseDir = cacheManager.getThumbnailsCacheDirectory();
        if (!this.mBaseDir.exists()) {
            this.mBaseDir.mkdirs();
        }
    }

    @Override
    public boolean isEnabled() {
        return this.mCacheManager.isCacheEnabled();
    }

    @Override
    public void clear() {
        IoUtils.deleteDirectory(this.mBaseDir);
    }

    @Override
    public boolean exists(String key) {
        File file = new File(this.mBaseDir, key);
        return file.exists();
    }

    @Override
    public void invalidate(String key) {

    }

    @Override
    public Bitmap loadData(String key) {
        if (!this.mCacheManager.isCacheEnabled() || !this.exists(key)) {
            return null;
        }

        // load the bitmap as-is (no scaling, no crop)
        Bitmap bitmap = null;

        File file = new File(this.mBaseDir, key);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(fis);
            if (bitmap == null) {
                file.delete();
                // something wrong with the persistent data, can't be decoded to
                // bitmap.
                return null;
                // throw new
                // RuntimeException("data from db can't be decoded to bitmap");
            }
            return bitmap;
        } catch (IOException e) {
            MyLog.e(TAG, e);
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public void storeData(String key, Bitmap data) {
        if (!this.mCacheManager.isCacheEnabled()) {
            return;
        }

        OutputStream outputStream = null;

        try {
            File file = new File(this.mBaseDir, key);

            outputStream = new FileOutputStream(file);
            if (!data.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                throw new RuntimeException("failed to compress bitmap");
            }
        } catch (FileNotFoundException e) {
            // No space left
            this.mCacheManager.trimCacheIfNeeded();
            MyLog.e(TAG, e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
