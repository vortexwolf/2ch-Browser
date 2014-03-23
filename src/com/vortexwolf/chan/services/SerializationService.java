package com.vortexwolf.chan.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.AsyncTask;

import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.IoUtils;

public class SerializationService {
    private static final String TAG = "SerializingService";

    public SerializationService() {
    }

    public void serializeObject(final File file, final Object obj) {
        SerializeTask task = new SerializeTask(file, obj);
        task.execute();
    }

    public Object deserializeObject(File file) {
        if (!file.exists()) {
            return null;
        }

        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(new FileInputStream(file));
            Object obj = is.readObject();

            return obj;
        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            IoUtils.closeStream(is);
        }

        return null;
    }
    
    private static class SerializeTask extends AsyncTask<Void, Void, Void> {
        private final File mFile;
        private final Object mObject;
        
        public SerializeTask(File file, Object object) {
            this.mFile = file;
            this.mObject = object;
        }
        
        @Override
        protected Void doInBackground(Void... arg0) {
            ObjectOutputStream os = null;
            try {
                os = new ObjectOutputStream(new FileOutputStream(this.mFile));
                os.writeObject(this.mObject);
            } catch (Exception e) {
                MyLog.e(TAG, e);
            } finally {
                IoUtils.closeStream(os);
            }
            
            return null;
        }
    }
}
