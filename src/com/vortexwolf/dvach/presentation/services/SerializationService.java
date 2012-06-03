package com.vortexwolf.dvach.presentation.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.vortexwolf.dvach.common.library.MyLog;

public class SerializationService {
	private static final String TAG = "SerializingService";
	
    private final ExecutorService mExecutor;
	
	public SerializationService(){
		mExecutor = Executors.newFixedThreadPool(1);
	}
	
	public void serializeObject(final File file, final Object obj){
		mExecutor.execute(new Runnable(){
			@Override
			public void run() {
				try {
					ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
					os.writeObject(obj);
					os.close();
				} catch (IOException e) {
					MyLog.e(TAG, e);
				}
			}
		});
	}
	
	public Object deserializeObject(File file){
		if(!file.exists()){
			return null;
		}
		
		try {
			ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
			Object obj = is.readObject();
			is.close();
			
			return obj;
		} catch (Exception e) {
			MyLog.e(TAG, e);
		}
		
		return null;
	}
}
