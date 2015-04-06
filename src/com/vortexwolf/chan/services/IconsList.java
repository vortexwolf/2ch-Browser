package com.vortexwolf.chan.services;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.MyLog;

public class IconsList {
    private static final String fileName = "politics_icons";
    private static final String TAG = "IconsList";
            
    private HashMap<String, String[]> data;
    private SerializationService mSerializationService = new SerializationService();
    private CacheDirectoryManager mCacheManager = Factory.resolve(CacheDirectoryManager.class);
    
    public IconsList() {
        try {
            File folder = mCacheManager.getCurrentCacheDirectory();
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(folder, fileName);
            data = (HashMap<String, String[]>) mSerializationService.deserializeObject(file);
            if (data == null) data = new HashMap<String, String[]>();
        } catch (Exception e) {
            MyLog.e(TAG, e);
            data = new HashMap<String, String[]>();
        }
    }
    
    public String[] getData(String boardName) {
        try {
            return data.get(boardName);
        } catch (Exception e) {
            MyLog.e(TAG, e);
            return null;
        }
    }
    
    public void setData(String boardName, String[] data) {
        try {
            if (!Arrays.equals(data, getData(boardName))) {
                this.data.put(boardName, data);
                save();
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }
    
    private void save() {
        try {
            File folder = mCacheManager.getCurrentCacheDirectory();
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(folder, fileName);
            
            mSerializationService.serializeObject(file, data);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }
}
