package android.httpimage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.interfaces.ICacheDirectoryManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


/**
 * File system implementation of persistent storage for downloaded images.
 * 
 * @author zonghai@gmail.com
 */
public class FileSystemPersistence implements BitmapCache{

    private static final String TAG = "ThumbnailFSPersistent";
    
    private final ICacheDirectoryManager mCacheManager;
    private final File mBaseDir;
 
    public FileSystemPersistence (ICacheDirectoryManager cacheManager) {
    	mCacheManager = cacheManager;
    	
        mBaseDir = cacheManager.getThumbnailsCacheDirectory();
        if (!mBaseDir.exists()) {
        	mBaseDir.mkdirs();
        }
    }
    
    @Override
	public boolean isEnabled(){
    	return mCacheManager.isCacheEnabled();
    }
    
    @Override
    public void clear() {
        IoUtils.deleteDirectory(mBaseDir);
    }

    
    @Override
    public boolean exists(String key) {
        File file = new File(mBaseDir, key) ;
        return file.exists();
    }

    
    @Override
    public void invalidate(String key) {
        
    }

    
    @Override
    public Bitmap loadData(String key) {
        if(!mCacheManager.isCacheEnabled() || !exists(key)) {
            return null;
        }
        
        //load the bitmap as-is (no scaling, no crop)
        Bitmap bitmap = null;
        
        File file = new File(mBaseDir, key) ;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(fis);
            if(bitmap == null) {
            	file.delete();
                 // something wrong with the persistent data, can't be decoded to bitmap.
            	return null;
                //throw new RuntimeException("data from db can't be decoded to bitmap");
            }
            return bitmap;
        }
        catch (IOException e) {
            MyLog.e(TAG, e);
            return null;
        }
        finally {
            if(fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {}
            }
        }
    }

    
    @Override
    public void storeData(String key, Bitmap data) {
    	if(!mCacheManager.isCacheEnabled()){
    		return;
    	}
    	
        OutputStream outputStream = null;
        
        try {
            File file = new File(mBaseDir, key) ;
            
            outputStream = new FileOutputStream(file);
            if(!data.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                throw new RuntimeException("failed to compress bitmap");
            }
        }
        catch (FileNotFoundException e){
        	// No space left
        	mCacheManager.trimCacheIfNeeded();
        	MyLog.e(TAG, e);
        }
        finally {
            if(outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {}
            }
        }
    }
}
