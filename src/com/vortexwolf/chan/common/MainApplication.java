package com.vortexwolf.chan.common;

import java.io.File;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Application;
import android.content.res.Resources;
import android.httpimage.BitmapMemoryCache;
import android.httpimage.FileSystemPersistence;
import android.httpimage.HttpImageManager;

import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.boards.dvach.DvachUriParser;
import com.vortexwolf.chan.boards.makaba.MakabaApiReader;
import com.vortexwolf.chan.boards.makaba.MakabaModelsMapper;
import com.vortexwolf.chan.boards.makaba.MakabaUriBuilder;
import com.vortexwolf.chan.common.library.ExtendedHttpClient;
import com.vortexwolf.chan.common.library.ExtendedObjectMapper;
import com.vortexwolf.chan.common.utils.CompatibilityUtilsImpl;
import com.vortexwolf.chan.db.DvachSqlHelper;
import com.vortexwolf.chan.db.FavoritesDataSource;
import com.vortexwolf.chan.db.HiddenThreadsDataSource;
import com.vortexwolf.chan.db.HistoryDataSource;
import com.vortexwolf.chan.interfaces.ICacheDirectoryManager;
import com.vortexwolf.chan.interfaces.IDraftPostsStorage;
import com.vortexwolf.chan.interfaces.IOpenTabsManager;
import com.vortexwolf.chan.interfaces.IPostSender;
import com.vortexwolf.chan.services.BitmapManager;
import com.vortexwolf.chan.services.CacheDirectoryManager;
import com.vortexwolf.chan.services.HtmlCaptchaChecker;
import com.vortexwolf.chan.services.IconsList;
import com.vortexwolf.chan.services.MyTracker;
import com.vortexwolf.chan.services.NavigationService;
import com.vortexwolf.chan.services.PostSender;
import com.vortexwolf.chan.services.SerializationService;
import com.vortexwolf.chan.services.ThreadImagesService;
import com.vortexwolf.chan.services.http.DownloadFileService;
import com.vortexwolf.chan.services.http.HttpBitmapReader;
import com.vortexwolf.chan.services.http.HttpBytesReader;
import com.vortexwolf.chan.services.http.HttpStreamReader;
import com.vortexwolf.chan.services.http.HttpStringReader;
import com.vortexwolf.chan.services.http.JsonHttpReader;
import com.vortexwolf.chan.services.presentation.DraftPostsStorage;
import com.vortexwolf.chan.services.presentation.OpenTabsManager;
import com.vortexwolf.chan.services.presentation.PagesSerializationService;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class MainApplication extends Application {

    public static boolean MULTITOUCH_SUPPORT = false;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Constants.SDK_VERSION >= 7) {
            MULTITOUCH_SUPPORT = CompatibilityUtilsImpl.hasMultitouchSupport(this.getPackageManager());
        }

        if (Constants.SDK_VERSION >= 19) {
            CompatibilityUtilsImpl.setSerialExecutor();
        }

        MyTracker tracker = new MyTracker(this);
        ApplicationSettings settings = new ApplicationSettings(this, this.getResources());
        ExtendedHttpClient httpClient = new ExtendedHttpClient(!settings.isUnsafeSSL());
        DvachUriBuilder dvachUriBuilder = new DvachUriBuilder(settings);
        MakabaUriBuilder makabaUriBuilder = new MakabaUriBuilder(settings);
        DvachUriParser uriParser = new DvachUriParser();
        HttpStreamReader httpStreamReader = new HttpStreamReader(httpClient, this.getResources());
        HttpBytesReader httpBytesReader = new HttpBytesReader(httpStreamReader, this.getResources());
        HttpStringReader httpStringReader = new HttpStringReader(httpBytesReader);
        HttpBitmapReader httpBitmapReader = new HttpBitmapReader(httpBytesReader);
        JsonHttpReader jsonApiReader = new JsonHttpReader(this.getResources(), new ExtendedObjectMapper(), httpStreamReader);
        DvachSqlHelper dbHelper = new DvachSqlHelper(this);
        MakabaApiReader makabaApiReader = new MakabaApiReader(jsonApiReader, new MakabaModelsMapper(), makabaUriBuilder, this.getResources(), settings);
        HistoryDataSource historyDataSource = new HistoryDataSource(dbHelper);
        FavoritesDataSource favoritesDataSource = new FavoritesDataSource(dbHelper, uriParser, dvachUriBuilder);
        HiddenThreadsDataSource hiddenThreadsDataSource = new HiddenThreadsDataSource(dbHelper);
        CacheDirectoryManager cacheManager = new CacheDirectoryManager(super.getCacheDir(), this.getPackageName(), settings, tracker);
        BitmapMemoryCache bitmapMemoryCache = new BitmapMemoryCache();
        HttpImageManager imageManager = new HttpImageManager(bitmapMemoryCache, new FileSystemPersistence(cacheManager), this.getResources(), httpBitmapReader);
        NavigationService navigationService = new NavigationService(uriParser);
        DownloadFileService downloadFileService = new DownloadFileService(this.getResources(), httpStreamReader);

        Container container = Factory.getContainer();
        container.register(Resources.class, this.getResources());
        container.register(DvachUriBuilder.class, dvachUriBuilder);
        container.register(DvachUriParser.class, uriParser);
        container.register(MakabaUriBuilder.class, makabaUriBuilder);
        container.register(ApplicationSettings.class, settings);
        container.register(DefaultHttpClient.class, httpClient);
        container.register(HttpStreamReader.class, httpStreamReader);
        container.register(HttpBytesReader.class, httpBytesReader);
        container.register(HttpStringReader.class, httpStringReader);
        container.register(HttpBitmapReader.class, httpBitmapReader);
        container.register(JsonHttpReader.class, jsonApiReader);
        container.register(MakabaApiReader.class, makabaApiReader);
        container.register(IPostSender.class, new PostSender(httpClient, this.getResources(), dvachUriBuilder, settings, httpStringReader));
        container.register(IDraftPostsStorage.class, new DraftPostsStorage());
        container.register(NavigationService.class, navigationService);
        container.register(IOpenTabsManager.class, new OpenTabsManager(historyDataSource, navigationService));
        container.register(MyTracker.class, tracker);
        container.register(ICacheDirectoryManager.class, cacheManager);
        container.register(PagesSerializationService.class, new PagesSerializationService(cacheManager, new SerializationService()));
        container.register(BitmapMemoryCache.class, bitmapMemoryCache);
        container.register(HttpImageManager.class, imageManager);
        container.register(BitmapManager.class, new BitmapManager(imageManager));
        container.register(HistoryDataSource.class, historyDataSource);
        container.register(FavoritesDataSource.class, favoritesDataSource);
        container.register(HiddenThreadsDataSource.class, hiddenThreadsDataSource);
        container.register(DownloadFileService.class, downloadFileService);
        container.register(ThreadImagesService.class, new ThreadImagesService());
        container.register(HtmlCaptchaChecker.class, new HtmlCaptchaChecker(httpStringReader, dvachUriBuilder, settings));
        container.register(IconsList.class, new IconsList());

        historyDataSource.open();
        favoritesDataSource.open();
        hiddenThreadsDataSource.open();

        if (!settings.isUnlimitedCache()) {
            cacheManager.trimCacheIfNeeded();
        }

        httpClient.setCookie(settings.getCloudflareClearanceCookie());
        httpClient.setCookie(settings.getPassCodeCookie());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public File getCacheDir() {
        // NOTE: this method is used in Android 2.2 and higher
        return Factory.getContainer().resolve(ICacheDirectoryManager.class).getCurrentCacheDirectory();
    }
}
