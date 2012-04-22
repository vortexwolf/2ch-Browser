package android.httpimage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.IoUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


/**
 * File system implementation of persistent storage for downloaded images.
 * 
 * @author zonghai@gmail.com
 */
public class FileSystemPersistence implements BitmapCache{

    private static final String TAG = "ThumbnailFSPersistent";
    
    private File mBaseDir;
 
    public FileSystemPersistence (File baseDir) {
        mBaseDir = baseDir;
        if (!mBaseDir.exists()) {
        	mBaseDir.mkdirs();
        }
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
        if( !exists(key)) {
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
        OutputStream outputStream = null;
        if(mBaseDir.list() != null && mBaseDir.list().length > Constants.MAX_FILE_CACHE_THUMBNAILS){
        	IoUtils.freeSpace(mBaseDir, IoUtils.convertMbToBytes(5));
        }
        
        try {
            File file = new File(mBaseDir, key) ;
            
            outputStream = new FileOutputStream(file);
            if(!data.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                throw new RuntimeException("failed to compress bitmap");
            }
        }
        catch (FileNotFoundException e){
        	// No space left
        	IoUtils.freeSpace(mBaseDir, IoUtils.convertMbToBytes(5));
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
