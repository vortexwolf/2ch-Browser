package com.vortexwolf.dvach.common;

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
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.interfaces.IBoardSettingsStorage;
import com.vortexwolf.dvach.interfaces.IDownloadFileService;
import com.vortexwolf.dvach.interfaces.IDraftPostsStorage;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.IPostSender;
import com.vortexwolf.dvach.settings.ApplicationSettings;

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
	private ExecutorService mExecutorService;
    private IBitmapManager mBitmapManager;
    
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
		
		String cacheDir = this.getCacheDir().getAbsolutePath();
		HttpImageManager httpImageManager = new HttpImageManager(new FileSystemPersistence(cacheDir));
		this.mBitmapManager = new BitmapManager(httpImageManager);
		
		this.mTracker = Tracker.getInstance();
		this.mTracker.getInnerTracker().startNewSession(Constants.ANALYTICS_KEY, 120, this);
		
		this.mSettings = new ApplicationSettings(this.getApplicationContext(), this.getResources(), mTracker);
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
	
	/** Создает маппер, который будет игнорировать неизвестные свойства */
	private ObjectMapper createObjectMapper()
	{
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return om;
	}


}
