package com.vortexwolf.dvach.common;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.vortexwolf.dvach.activities.addpost.PostSender;
import com.vortexwolf.dvach.api.BoardSettingsStorage;
import com.vortexwolf.dvach.api.JsonApiReader;
import com.vortexwolf.dvach.api.JsonApiReaderException;
import com.vortexwolf.dvach.api.entities.BoardSettings;
import com.vortexwolf.dvach.common.http.DownloadFileService;
import com.vortexwolf.dvach.common.http.GzipHttpClientFactory;
import com.vortexwolf.dvach.common.library.BitmapManager;
import com.vortexwolf.dvach.common.library.DraftPostsStorage;
import com.vortexwolf.dvach.common.library.Tracker;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.interfaces.IBoardSettingsStorage;
import com.vortexwolf.dvach.interfaces.IDownloadFileService;
import com.vortexwolf.dvach.interfaces.IDraftPostsStorage;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.IPostSender;
import com.vortexwolf.dvach.settings.ApplicationSettings;
import com.vortexwolf.dvach.settings.ICacheSettingsChangedListener;

import android.app.Application;
import android.content.res.Configuration;
import android.httpimage.FileSystemPersistence;
import android.httpimage.HttpImageManager;
import android.os.Environment;

public class MainApplication extends Application {
	
	private static final DefaultHttpClient sHttpClient = new GzipHttpClientFactory().createHttpClient();
	
	private ApplicationSettings mSettings;
	private Errors mErrors;
	private IJsonApiReader mJsonApiReader;
	private IPostSender mPostSender;
	private IBoardSettingsStorage mBoardSettingsStorage;
	private IDraftPostsStorage mDraftPostsStorage;
	private IDownloadFileService mDownloadFileService;
	private Tracker mTracker;
	private ExecutorService mExecutorService;
	private HttpImageManager mHttpImageManager;
    private IBitmapManager mBitmapManager;
    private File mCurrentCacheDirectory;
    
	@Override
	public void onCreate() {
		super.onCreate();
		
		this.mErrors = new Errors(this.getResources());
		this.mJsonApiReader = new JsonApiReader(sHttpClient, this.mErrors, this.createObjectMapper());
		this.mPostSender = new PostSender(sHttpClient, this.mErrors);
		this.mBoardSettingsStorage = new BoardSettingsStorage(this.mJsonApiReader);
		this.mDraftPostsStorage = new DraftPostsStorage();
		this.mDownloadFileService = new DownloadFileService(this.mErrors);
		this.mExecutorService = Executors.newFixedThreadPool(3);

		this.mTracker = Tracker.getInstance();
		this.mTracker.getInnerTracker().startNewSession(Constants.ANALYTICS_KEY, 120, this);
		
		this.mSettings = new ApplicationSettings(this.getApplicationContext(), this.getResources(), mTracker, new CacheSettingsChangedListener());
		this.mHttpImageManager = new HttpImageManager(null);
		this.updateImageManagerFromSettings();
		this.mBitmapManager = new BitmapManager(this.mHttpImageManager);	
	}

	@Override
	public void onTerminate() {
		this.mTracker.getInnerTracker().stopSession();

		super.onTerminate();
	}

	public static DefaultHttpClient getHttpClient(){
		return sHttpClient;
	}
	
	public ApplicationSettings getSettings(){
		return mSettings;
	}
	
	public Errors getErrors(){
		return mErrors;
	}
	
	public IJsonApiReader getJsonApiReader(){
		return mJsonApiReader;
	}
	
	public IPostSender getPostSender(){
		return mPostSender;
	}

	public IBoardSettingsStorage getBoardSettingsStorage() {
		return mBoardSettingsStorage;
	}
	
	public IDraftPostsStorage getDraftPostsStorage(){
		return mDraftPostsStorage;
	}
	
	public IDownloadFileService getDownloadFileService(){
		return mDownloadFileService;
	}
	
	public Tracker getTracker(){
		return mTracker;
	}

	public ExecutorService getExecutorService() {
		return mExecutorService;
	}
	
	public IBitmapManager getBitmapManager(){
		return mBitmapManager;
	}
	
	@Override
	  public File getCacheDir()
	  {
		  // NOTE: this method is used in Android 2.2 and higher
		  if (mCurrentCacheDirectory != null)
		  {
			  return mCurrentCacheDirectory;
		  }
		  else
		  {
			  return this.getInternalCacheDir();
		  }
	  }
	
	  public File getInternalCacheDir()
	  {
		  return super.getCacheDir();
	  }
	  
	  public File getExternalCacheDir()
	  {
		  return IoUtils.tryGetExternalCachePath(this);
	  }
	
	private void updateImageManagerFromSettings(){
		mCurrentCacheDirectory = this.getSettingsCacheDirectory(mSettings);
		mHttpImageManager.setPersistenceCache(mCurrentCacheDirectory != null ? new FileSystemPersistence(mCurrentCacheDirectory) : null);
	}
	
	private File getSettingsCacheDirectory(ApplicationSettings settings){
		if(settings.isFileCacheEnabled()){
			if(settings.isFileCacheSdCard()){
				File externalPath = getExternalCacheDir();
				if(externalPath != null){
					return externalPath;
				}
			}
			
			return getInternalCacheDir();
		}
		
		return null;
	}
	
	/** Создает маппер, который будет игнорировать неизвестные свойства */
	private ObjectMapper createObjectMapper()
	{
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return om;
	}


	private class CacheSettingsChangedListener implements ICacheSettingsChangedListener {

		@Override
		public void cacheFileSystemChanged(boolean newValue) {
			updateImageManagerFromSettings();
		}

		@Override
		public void cacheSDCardChanged(boolean newValue) {
			updateImageManagerFromSettings();
		}
	}
	
}
