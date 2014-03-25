package com.vortexwolf.chan.common;

import java.io.File;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Application;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.httpimage.BitmapMemoryCache;
import android.httpimage.BitmapCache;
import android.httpimage.FileSystemPersistence;
import android.httpimage.HttpImageManager;

import com.vortexwolf.chan.common.library.ExtendedHttpClient;
import com.vortexwolf.chan.common.library.ExtendedObjectMapper;
import com.vortexwolf.chan.db.DvachSqlHelper;
import com.vortexwolf.chan.db.FavoritesDataSource;
import com.vortexwolf.chan.db.HiddenThreadsDataSource;
import com.vortexwolf.chan.db.HistoryDataSource;
import com.vortexwolf.chan.interfaces.IBitmapManager;
import com.vortexwolf.chan.interfaces.ICacheDirectoryManager;
import com.vortexwolf.chan.interfaces.IDraftPostsStorage;
import com.vortexwolf.chan.interfaces.IJsonApiReader;
import com.vortexwolf.chan.interfaces.INavigationService;
import com.vortexwolf.chan.interfaces.IOpenTabsManager;
import com.vortexwolf.chan.interfaces.IPagesSerializationService;
import com.vortexwolf.chan.interfaces.IPostSender;
import com.vortexwolf.chan.services.BitmapManager;
import com.vortexwolf.chan.services.CacheDirectoryManager;
import com.vortexwolf.chan.services.MyTracker;
import com.vortexwolf.chan.services.NavigationService;
import com.vortexwolf.chan.services.SerializationService;
import com.vortexwolf.chan.services.ThreadImagesService;
import com.vortexwolf.chan.services.domain.DownloadFileService;
import com.vortexwolf.chan.services.domain.JsonApiReader;
import com.vortexwolf.chan.services.domain.PostSender;
import com.vortexwolf.chan.services.presentation.DraftPostsStorage;
import com.vortexwolf.chan.services.presentation.DvachUriBuilder;
import com.vortexwolf.chan.services.presentation.OpenTabsManager;
import com.vortexwolf.chan.services.presentation.PagesSerializationService;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class MainApplication extends Application {

    public static boolean MULTITOUCH_SUPPORT = false;
    
    @Override
    public void onCreate() {
        super.onCreate();

        if (Constants.SDK_VERSION >= 7) {
            MULTITOUCH_SUPPORT = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
        }

        MyTracker tracker = new MyTracker(this.getApplicationContext());
        DefaultHttpClient httpClient = new ExtendedHttpClient();
        ApplicationSettings settings = new ApplicationSettings(this, this.getResources());
        DvachUriBuilder dvachUriBuilder = new DvachUriBuilder(settings);
        JsonApiReader jsonApiReader = new JsonApiReader(httpClient, this.getResources(), new ExtendedObjectMapper(), dvachUriBuilder);
        DvachSqlHelper dbHelper = new DvachSqlHelper(this);
        HistoryDataSource historyDataSource = new HistoryDataSource(dbHelper);
        FavoritesDataSource favoritesDataSource = new FavoritesDataSource(dbHelper);
        HiddenThreadsDataSource hiddenThreadsDataSource = new HiddenThreadsDataSource(dbHelper);
        CacheDirectoryManager cacheManager = new CacheDirectoryManager(super.getCacheDir(), this.getPackageName(), settings, tracker);
        BitmapMemoryCache bitmapMemoryCache = new BitmapMemoryCache();
        HttpImageManager imageManager = new HttpImageManager(bitmapMemoryCache, new FileSystemPersistence(cacheManager), httpClient, this.getResources());
        NavigationService navigationService = new NavigationService();
        DownloadFileService downloadFileService = new DownloadFileService(this.getResources(), httpClient);

        Container container = Factory.getContainer();
        container.register(Resources.class, this.getResources());
        container.register(DvachUriBuilder.class, dvachUriBuilder);
        container.register(ApplicationSettings.class, settings);
        container.register(DefaultHttpClient.class, httpClient);
        container.register(IJsonApiReader.class, jsonApiReader);
        container.register(IPostSender.class, new PostSender(httpClient, this.getResources(), dvachUriBuilder, settings));
        container.register(IDraftPostsStorage.class, new DraftPostsStorage());
        container.register(INavigationService.class, navigationService);
        container.register(IOpenTabsManager.class, new OpenTabsManager(historyDataSource, navigationService));
        container.register(MyTracker.class, tracker);
        container.register(ICacheDirectoryManager.class, cacheManager);
        container.register(IPagesSerializationService.class, new PagesSerializationService(cacheManager, new SerializationService()));
        container.register(BitmapMemoryCache.class, bitmapMemoryCache);
        container.register(HttpImageManager.class, imageManager);
        container.register(IBitmapManager.class, new BitmapManager(imageManager));
        container.register(HistoryDataSource.class, historyDataSource);
        container.register(FavoritesDataSource.class, favoritesDataSource);
        container.register(HiddenThreadsDataSource.class, hiddenThreadsDataSource);
        container.register(DownloadFileService.class, downloadFileService);
        container.register(ThreadImagesService.class, new ThreadImagesService());

        historyDataSource.open();
        favoritesDataSource.open();
        hiddenThreadsDataSource.open();
        
        tracker.startSession(this);
        cacheManager.trimCacheIfNeeded();
    }

    @Override
    public void onTerminate() {
        Factory.getContainer().resolve(MyTracker.class).stopSession();

        super.onTerminate();
    }

    public static DefaultHttpClient getHttpClient() {
        return Factory.getContainer().resolve(DefaultHttpClient.class);
    }

    public ApplicationSettings getSettings() {
        return Factory.getContainer().resolve(ApplicationSettings.class);
    }

    public IJsonApiReader getJsonApiReader() {
        return Factory.getContainer().resolve(IJsonApiReader.class);
    }

    public IPostSender getPostSender() {
        return Factory.getContainer().resolve(IPostSender.class);
    }

    public IDraftPostsStorage getDraftPostsStorage() {
        return Factory.getContainer().resolve(IDraftPostsStorage.class);
    }

    public MyTracker getTracker() {
        return Factory.getContainer().resolve(MyTracker.class);
    }

    public IBitmapManager getBitmapManager() {
        return Factory.getContainer().resolve(IBitmapManager.class);
    }

    public IOpenTabsManager getOpenTabsManager() {
        return Factory.getContainer().resolve(IOpenTabsManager.class);
    }

    public ICacheDirectoryManager getCacheManager() {
        return Factory.getContainer().resolve(ICacheDirectoryManager.class);
    }

    public IPagesSerializationService getSerializationService() {
        return Factory.getContainer().resolve(IPagesSerializationService.class);
    }

    public HistoryDataSource getHistoryDataSource() {
        return Factory.getContainer().resolve(HistoryDataSource.class);
    }

    @Override
    public File getCacheDir() {
        // NOTE: this method is used in Android 2.2 and higher
        return Factory.getContainer().resolve(ICacheDirectoryManager.class).getCurrentCacheDirectory();
    }
}
