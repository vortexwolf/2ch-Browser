package com.vortexwolf.dvach.presentation.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;

import com.vortexwolf.dvach.activities.threads.ThreadsListAdapter;
import com.vortexwolf.dvach.api.entities.PostInfo;
import com.vortexwolf.dvach.api.entities.ThreadInfo;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.interfaces.ICacheManager;
import com.vortexwolf.dvach.interfaces.ISerializationService;

public class SerializationService implements ISerializationService {
	private static final String TAG = "SerializingService";
	
	private static final String sExtension = ".2ch";
	
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(1);
	private final ICacheManager mCacheManager;
	
	public SerializationService(ICacheManager cacheManager){
		this.mCacheManager = cacheManager;
	}
	
	@Override
	public void serializeThreads(String boardName, int pageNumber, ThreadInfo[] threads){
		File file = this.getFilePath(boardName + "-" + pageNumber);
		
		this.serializeObject(file, threads);
	}
	
	@Override
	public void serializePosts(String threadNumber, PostInfo[] posts){
		File file = this.getFilePath(threadNumber);
		
		this.serializeObject(file, posts);
	}
	
	@Override
	public ThreadInfo[] deserializeThreads(String boardName, int pageNumber){
		File file = this.getFilePath(boardName + "-" + pageNumber);
		
		ThreadInfo[] threads = (ThreadInfo[])this.deserializeObject(file);
		
		return threads;
	}
	
	@Override
	public PostInfo[] deserializePosts(String threadNumber){
		File file = this.getFilePath(threadNumber);
		
		PostInfo[] posts = (PostInfo[])this.deserializeObject(file);
		
		return posts;
	}
	
	private File getFilePath(String fileName){
		File folder = this.mCacheManager.getPagesCacheDirectory();
		if(!folder.exists()){
			folder.mkdirs();
		}
		
		File file = new File(folder, fileName + sExtension);
		
		return file;
	}
	
	private void serializeObject(final File file, final Object obj){
		mExecutor.execute(new Runnable(){
			@Override
			public void run() {
				try {
					FileOutputStream fos = new FileOutputStream(file);
					ObjectOutputStream os = new ObjectOutputStream(fos);
					os.writeObject(obj);
					os.close();
				} catch (IOException e) {
					MyLog.e(TAG, e);
				}
			}
		});
	}
	
	private Object deserializeObject(File file){
		if(!file.exists()){
			return null;
		}
		try {
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream is = new ObjectInputStream(fis);
			Object obj = is.readObject();
			is.close();
			
			return obj;
		} catch (IOException e) {
			MyLog.e(TAG, e);
		} catch (ClassNotFoundException e) {
			MyLog.e(TAG, e);
		}
		
		return null;
	}
}
