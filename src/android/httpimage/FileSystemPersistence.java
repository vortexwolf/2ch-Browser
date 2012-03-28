package android.httpimage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.IoUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


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
        try {
            this.removeDir(mBaseDir);
        } 
        catch (IOException e) {
            throw new RuntimeException ( e );
        }
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
    	//this.checkCacheSize();
    	
        OutputStream outputStream = null;
        try {
            File file = new File(mBaseDir, key) ;
            
            outputStream = new FileOutputStream(file);
            if(!data.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                throw new RuntimeException("failed to compress bitmap");
            }
        }
        catch (IOException e) {
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
    
    private void checkCacheSize(){
    	long cacheSize =IoUtils.dirSize(mBaseDir);
    	long maxCache = 5 * 1024 * 1024;
    	
    	if(cacheSize > maxCache){
    		long diff = cacheSize - maxCache;
    		
    	    File[] files = mBaseDir.listFiles();
    	    long releasedSize = 0;
    	    for (File f:files) {
    	    	releasedSize += f.length();
    	    	f.delete();
    	    	// удаляем излишки и половину доступного кэша
    	    	if(releasedSize >= diff + cacheSize / 2){
    	    		break;
    	    	}
    	    }
    	}
    }
    
    
    /**
     * Delete a directory
     *
     * @param d the directory to delete
     */
    private void removeDir(File d) throws IOException{
        // to see if this directory is actually a symbolic link to a directory,
        // we want to get its canonical path - that is, we follow the link to
        // the file it's actually linked to
        File candir = d.getCanonicalFile();
  
        // a symbolic link has a different canonical path than its actual path,
        // unless it's a link to itself
        if (!candir.equals(d.getAbsoluteFile())) {
            // this file is a symbolic link, and there's no reason for us to
            // follow it, because then we might be deleting something outside of
            // the directory we were told to delete
            return;
        }
  
        // now we go through all of the files and subdirectories in the
        // directory and delete them one by one
        File[] files = candir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
  
                // in case this directory is actually a symbolic link, or it's
                // empty, we want to try to delete the link before we try
                // anything
                boolean deleted = file.delete();
                if (!deleted) {
                    // deleting the file failed, so maybe it's a non-empty
                    // directory
                    if (file.isDirectory()) removeDir(file);
  
                    // otherwise, there's nothing else we can do
                }
            }
        }
  
        // now that we tried to clear the directory out, we can try to delete it
        // again
        d.delete();  
    }    
}
