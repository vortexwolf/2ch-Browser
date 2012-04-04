package com.vortexwolf.dvach.common;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import com.vortexwolf.dvach.activities.addpost.PostSender;
import com.vortexwolf.dvach.api.BoardSettingsStorage;
import com.vortexwolf.dvach.api.JsonApiReader;
import com.vortexwolf.dvach.api.ObjectMapperFactory;
import com.vortexwolf.dvach.common.http.GzipHttpClientFactory;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.interfaces.IBoardSettingsStorage;
import com.vortexwolf.dvach.interfaces.ICacheDirectoryChangedListener;
import com.vortexwolf.dvach.interfaces.IDownloadFileService;
import com.vortexwolf.dvach.interfaces.IDraftPostsStorage;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.IOpenTabsManager;
import com.vortexwolf.dvach.interfaces.IPostSender;
import com.vortexwolf.dvach.presentation.services.BitmapManager;
import com.vortexwolf.dvach.presentation.services.CacheManager;
import com.vortexwolf.dvach.presentation.services.DownloadFileService;
import com.vortexwolf.dvach.presentation.services.DraftPostsStorage;
import com.vortexwolf.dvach.presentation.services.ICacheManager;
import com.vortexwolf.dvach.presentation.services.OpenTabsManager;
import com.vortexwolf.dvach.presentation.services.Tracker;
import com.vortexwolf.dvach.settings.ApplicationSettings;
import com.vortexwolf.dvach.settings.ICacheSettingsChangedListener;

import android.app.Application;
import android.httpimage.FileSystemPersistence;
import android.httpimage.HttpImageManager;

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
	private HttpImageManager mHttpImageManager;
    private IBitmapManager mBitmapManager;
    private IOpenTabsManager mOpenTabsMaganer;
    private CacheManager mCacheManager;
    
	@Override
	public void onCreate() {
		super.onCreate();
		
		this.mErrors = new Errors(this.getResources());
		this.mJsonApiReader = new JsonApiReader(sHttpClient, this.mErrors, ObjectMapperFactory.createObjectMapper());
		this.mPostSender = new PostSender(sHttpClient, this.mErrors);
		this.mBoardSettingsStorage = new BoardSettingsStorage(this.mJsonApiReader);
		this.mDraftPostsStorage = new DraftPostsStorage();
		this.mDownloadFileService = new DownloadFileService(this.mErrors);
		this.mOpenTabsMaganer = new OpenTabsManager();

		this.mTracker = Tracker.getInstance();
		this.mTracker.startSession(this);
		
		this.mSettings = new ApplicationSettings(this, this.getResources(), mTracker);
		this.mSettings.setCacheSettingsChangedListener(new CacheSettingsChangedListener());
		
		this.mCacheManager = new CacheManager(super.getCacheDir(), this.getPackageName(), this.mSettings);

		this.mHttpImageManager = new HttpImageManager(null);
		this.updateImageManager();
		this.mBitmapManager = new BitmapManager(this.mHttpImageManager);	
	}

	@Override
	public void onTerminate() {
		this.mTracker.stopSession();

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

	public IBitmapManager getBitmapManager(){
		return mBitmapManager;
	}
	
	public IOpenTabsManager getOpenTabsManager(){
		return mOpenTabsMaganer;
	}
	
	public ICacheManager getCacheManager(){
		return mCacheManager;
	}
	
	@Override
	public File getCacheDir() {
		// NOTE: this method is used in Android 2.2 and higher
		return this.mCacheManager.getCurrentCacheDirectory();
	}
	
	private void updateImageManager(){
		mHttpImageManager.setPersistenceCache(
				mCacheManager.isCacheEnabled() ? new FileSystemPersistence(this.mCacheManager.getCurrentCacheDirectory()) : null);
	}
	
	private class CacheSettingsChangedListener implements ICacheSettingsChangedListener{
		@Override
		public void onCacheSettingsChanged() {
			updateImageManager();
		}
	}
}
