package com.vortexwolf.dvach.common;

import java.io.File;
import org.apache.http.impl.client.DefaultHttpClient;
import com.vortexwolf.dvach.activities.addpost.PostSender;
import com.vortexwolf.dvach.api.BoardSettingsStorage;
import com.vortexwolf.dvach.api.JsonApiReader;
import com.vortexwolf.dvach.api.ObjectMapperFactory;
import com.vortexwolf.dvach.common.library.ExtendedHttpClient;
import com.vortexwolf.dvach.db.HistoryDataSource;
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.interfaces.IBoardSettingsStorage;
import com.vortexwolf.dvach.interfaces.ICacheDirectoryManager;
import com.vortexwolf.dvach.interfaces.IDownloadFileService;
import com.vortexwolf.dvach.interfaces.IDraftPostsStorage;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.INavigationService;
import com.vortexwolf.dvach.interfaces.IOpenTabsManager;
import com.vortexwolf.dvach.interfaces.IPostSender;
import com.vortexwolf.dvach.interfaces.IPagesSerializationService;
import com.vortexwolf.dvach.presentation.services.BitmapManager;
import com.vortexwolf.dvach.presentation.services.CacheDirectoryManager;
import com.vortexwolf.dvach.presentation.services.DownloadFileService;
import com.vortexwolf.dvach.presentation.services.DraftPostsStorage;
import com.vortexwolf.dvach.presentation.services.NavigationService;
import com.vortexwolf.dvach.presentation.services.OpenTabsManager;
import com.vortexwolf.dvach.presentation.services.PagesSerializationService;
import com.vortexwolf.dvach.presentation.services.SerializationService;
import com.vortexwolf.dvach.presentation.services.Tracker;
import com.vortexwolf.dvach.settings.ApplicationSettings;
import android.app.Application;
import android.httpimage.FileSystemPersistence;
import android.httpimage.HttpImageManager;

public class MainApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		DefaultHttpClient httpClient = new ExtendedHttpClient();
		JsonApiReader jsonApiReader = new JsonApiReader(httpClient, this.getResources(), ObjectMapperFactory.createObjectMapper());
		HistoryDataSource historyDataSource = new HistoryDataSource(this);
		Tracker tracker = new Tracker();
		ApplicationSettings settings = new ApplicationSettings(this, this.getResources(), tracker);
		CacheDirectoryManager cacheManager = new CacheDirectoryManager(super.getCacheDir(), this.getPackageName(), settings, tracker);
		HttpImageManager imageManager = new HttpImageManager(new FileSystemPersistence(cacheManager));
		NavigationService navigationService = new NavigationService();
		
		Container container = Factory.getContainer();
		container.register(DefaultHttpClient.class, httpClient);
		container.register(IJsonApiReader.class, jsonApiReader);
		container.register(IPostSender.class, new PostSender(httpClient, this.getResources()));
		container.register(IBoardSettingsStorage.class, new BoardSettingsStorage(jsonApiReader));
		container.register(IDraftPostsStorage.class, new DraftPostsStorage());
		container.register(IDownloadFileService.class, new DownloadFileService(this.getResources()));
		container.register(INavigationService.class, navigationService);
		container.register(IOpenTabsManager.class, new OpenTabsManager(historyDataSource, navigationService));
		container.register(ApplicationSettings.class, settings);
		container.register(Tracker.class, tracker);
		container.register(ICacheDirectoryManager.class, cacheManager);
		container.register(IPagesSerializationService.class,  new PagesSerializationService(cacheManager, new SerializationService()));
		container.register(HttpImageManager.class, imageManager);
		container.register(IBitmapManager.class, new BitmapManager(imageManager));	
		container.register(HistoryDataSource.class, historyDataSource);	
		
		historyDataSource.open();
		tracker.startSession(this);
		cacheManager.trimCacheIfNeeded();
	}

	@Override
	public void onTerminate() {
		Factory.getContainer().resolve(Tracker.class).stopSession();

		super.onTerminate();
	}

	public static DefaultHttpClient getHttpClient(){
		return Factory.getContainer().resolve(DefaultHttpClient.class);
	}
	
	public ApplicationSettings getSettings(){
		return Factory.getContainer().resolve(ApplicationSettings.class);
	}
		
	public IJsonApiReader getJsonApiReader(){
		return Factory.getContainer().resolve(IJsonApiReader.class);
	}
	
	public IPostSender getPostSender(){
		return Factory.getContainer().resolve(IPostSender.class);
	}

	public IBoardSettingsStorage getBoardSettingsStorage() {
		return Factory.getContainer().resolve(IBoardSettingsStorage.class);
	}
	
	public IDraftPostsStorage getDraftPostsStorage(){
		return Factory.getContainer().resolve(IDraftPostsStorage.class);
	}
	
	public IDownloadFileService getDownloadFileService(){
		return Factory.getContainer().resolve(IDownloadFileService.class);
	}
	
	public Tracker getTracker(){
		return Factory.getContainer().resolve(Tracker.class);
	}

	public IBitmapManager getBitmapManager(){
		return Factory.getContainer().resolve(IBitmapManager.class);
	}
	
	public IOpenTabsManager getOpenTabsManager(){
		return Factory.getContainer().resolve(IOpenTabsManager.class);
	}
	
	public ICacheDirectoryManager getCacheManager(){
		return Factory.getContainer().resolve(ICacheDirectoryManager.class);
	}
	
	public IPagesSerializationService getSerializationService(){
		return Factory.getContainer().resolve(IPagesSerializationService.class);
	}
	
	public HistoryDataSource getHistoryDataSource(){
		return Factory.getContainer().resolve(HistoryDataSource.class);
	}
	
	@Override
	public File getCacheDir() {
		// NOTE: this method is used in Android 2.2 and higher
		return Factory.getContainer().resolve(ICacheDirectoryManager.class).getCurrentCacheDirectory();
	}
}
