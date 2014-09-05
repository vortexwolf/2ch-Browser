package com.vortexwolf.chan.services;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.interfaces.ICacheDirectoryManager;

public class IconsList {
    private static final String fileName = "politics_icons";
    private HashMap<String, String[]> data;
    private SerializationService mSerializationService = new SerializationService();
    private ICacheDirectoryManager mCacheManager = Factory.resolve(ICacheDirectoryManager.class);
    
    public IconsList() {
        File folder = mCacheManager.getCurrentCacheDirectory();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder, fileName);
        data = (HashMap<String, String[]>) mSerializationService.deserializeObject(file);
        if (data == null) data = new HashMap<String, String[]>();
    }
    
    public String[] getData(String boardName) {
        return data.get(boardName);
    }
    
    public void setData(String boardName, String[] data) {
        if (!Arrays.equals(data, getData(boardName))) {
            this.data.put(boardName, data);
            save();
        }
    }
    
    private void save() {
        File folder = mCacheManager.getCurrentCacheDirectory();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder, fileName);

        mSerializationService.serializeObject(file, data);
    }
}
