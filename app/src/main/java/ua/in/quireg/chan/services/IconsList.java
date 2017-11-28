package ua.in.quireg.chan.services;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.library.MyLog;

public class IconsList {
    private static final String fileName = "politics_icons";
    private static final String TAG = IconsList.class.getSimpleName();

    private HashMap<String, String[]> data;
    private CacheDirectoryManager mCacheManager = Factory.resolve(CacheDirectoryManager.class);

    @SuppressWarnings("unchecked")
    public IconsList() {
        try {
            File folder = mCacheManager.getCurrentCacheDirectory();
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(folder, fileName);
            data = (HashMap<String, String[]>) SerializationService.deserializeFromFile(file);

        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            if (data == null) {
                data = new HashMap<>();
            }
        }
    }

    public String[] getData(String boardName) {
        return data.get(boardName);
    }

    public void setData(String boardName, String[] newData) {
        try {
            if (!Arrays.equals(newData, getData(boardName))) {
                data.put(boardName, newData);
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

            SerializationService.serializeToFile(file, data);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }
}
