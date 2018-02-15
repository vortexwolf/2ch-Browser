package ua.in.quireg.chan.common;

import android.app.Application;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import com.squareup.leakcanary.LeakCanary;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.apache.http.impl.client.DefaultHttpClient;

import httpimage.BitmapMemoryCache;
import httpimage.FileSystemPersistence;
import httpimage.HttpImageManager;
import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.boards.makaba.MakabaApiReader;
import ua.in.quireg.chan.boards.makaba.MakabaModelsMapper;
import ua.in.quireg.chan.boards.makaba.MakabaUrlBuilder;
import ua.in.quireg.chan.boards.makaba.MakabaUrlParser;
import ua.in.quireg.chan.boards.makaba.MakabaWebsite;
import ua.in.quireg.chan.common.library.ExtendedHttpClient;
import ua.in.quireg.chan.common.library.ExtendedObjectMapper;
import ua.in.quireg.chan.common.utils.CompatibilityUtils;
import ua.in.quireg.chan.db.DvachSqlHelper;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.db.HiddenThreadsDataSource;
import ua.in.quireg.chan.db.HistoryDataSource;
import ua.in.quireg.chan.di.AppComponent;
import ua.in.quireg.chan.di.BaseComponent;
import ua.in.quireg.chan.di.BaseModule;
import ua.in.quireg.chan.di.DaggerBaseComponent;
import ua.in.quireg.chan.di.DataRepositoryModule;
import ua.in.quireg.chan.di.NavigationModule;
import ua.in.quireg.chan.di.NetModule;
import ua.in.quireg.chan.di.WebsiteModule;
import ua.in.quireg.chan.services.BitmapManager;
import ua.in.quireg.chan.services.CacheDirectoryManager;
import ua.in.quireg.chan.services.DvachCaptchaService;
import ua.in.quireg.chan.services.HtmlCaptchaChecker;
import ua.in.quireg.chan.services.IconsList;
import ua.in.quireg.chan.services.MailruCaptchaService;
import ua.in.quireg.chan.services.PostSender;
import ua.in.quireg.chan.services.ThreadImagesService;
import ua.in.quireg.chan.services.http.DownloadFileService;
import ua.in.quireg.chan.services.http.HttpBitmapReader;
import ua.in.quireg.chan.services.http.HttpBytesReader;
import ua.in.quireg.chan.services.http.HttpStreamReader;
import ua.in.quireg.chan.services.http.HttpStringReader;
import ua.in.quireg.chan.services.http.JsonHttpReader;
import ua.in.quireg.chan.services.presentation.DraftPostsStorage;
import ua.in.quireg.chan.services.presentation.OpenTabsManager;
import ua.in.quireg.chan.services.presentation.PagesSerializationService;
import ua.in.quireg.chan.settings.ApplicationSettings;

@ReportsCrashes(mailTo = "2chbrowsergreatagain@gmail.com",
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PHONE_MODEL,
                ReportField.CUSTOM_DATA,
                ReportField.STACK_TRACE,
                ReportField.LOGCAT
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)

public class MainApplication extends Application {

    private static AppComponent mAppComponent;
    private static BaseComponent mBaseComponent;

    private Toast mToast;

    public static AppComponent getAppComponent() {
        return mAppComponent;
    }

    public static BaseComponent getBaseComponent() {
        return mBaseComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            return;
        }

        if(Constants.LOGGING) {
            LeakCanary.install(this);
            Timber.plant(new Timber.DebugTree());
        } else {
            ACRA.init(this);
        }
        mBaseComponent = buildBaseComponent();
        mAppComponent = buildAppComponent();

        if (Constants.SDK_VERSION >= 19) {
            CompatibilityUtils.setSerialExecutor();
        }

        ExtendedHttpClient httpClient = new ExtendedHttpClient(true);
        HttpStreamReader httpStreamReader = new HttpStreamReader(httpClient, getResources());
        HttpBytesReader httpBytesReader = new HttpBytesReader(httpStreamReader, getResources());
        HttpStringReader httpStringReader = new HttpStringReader(httpBytesReader);
        HttpBitmapReader httpBitmapReader = new HttpBitmapReader(httpBytesReader);
        JsonHttpReader jsonApiReader = new JsonHttpReader(getResources(), new ExtendedObjectMapper(), httpStreamReader);


        DvachSqlHelper dbHelper = new DvachSqlHelper(this);
        HistoryDataSource historyDataSource = new HistoryDataSource(dbHelper);
        FavoritesDataSource favoritesDataSource = new FavoritesDataSource(dbHelper);
        HiddenThreadsDataSource hiddenThreadsDataSource = new HiddenThreadsDataSource(dbHelper);

        CacheDirectoryManager cacheManager = new CacheDirectoryManager(getApplicationContext());
        BitmapMemoryCache bitmapMemoryCache = new BitmapMemoryCache();
        HttpImageManager imageManager = new HttpImageManager(bitmapMemoryCache, new FileSystemPersistence(cacheManager), getResources(), httpBitmapReader);
        DownloadFileService downloadFileService = new DownloadFileService(this.getResources(), httpStreamReader);

        MakabaUrlBuilder makabaUriBuilder = new MakabaUrlBuilder();
        MakabaApiReader makabaApiReader = new MakabaApiReader(jsonApiReader, new MakabaModelsMapper(), makabaUriBuilder, getResources());

        ApplicationSettings mSettings = new ApplicationSettings(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        Container container = Factory.getContainer();
        container.register(Resources.class, getResources());
        container.register(MakabaWebsite.class, new MakabaWebsite());
        container.register(MakabaUrlParser.class, new MakabaUrlParser());
        container.register(MakabaUrlBuilder.class, makabaUriBuilder);
        container.register(MakabaApiReader.class, makabaApiReader);
        container.register(ApplicationSettings.class, mSettings);
        container.register(DefaultHttpClient.class, httpClient);
        container.register(HttpStreamReader.class, httpStreamReader);
        container.register(HttpBytesReader.class, httpBytesReader);
        container.register(HttpStringReader.class, httpStringReader);
        container.register(HttpBitmapReader.class, httpBitmapReader);
        container.register(JsonHttpReader.class, jsonApiReader);
        container.register(PostSender.class, new PostSender(httpClient, getResources(), httpStringReader));
        container.register(DraftPostsStorage.class, new DraftPostsStorage());
        container.register(OpenTabsManager.class, new OpenTabsManager());
        container.register(CacheDirectoryManager.class, cacheManager);
        container.register(PagesSerializationService.class, new PagesSerializationService(cacheManager));
        container.register(BitmapMemoryCache.class, bitmapMemoryCache);
        container.register(HttpImageManager.class, imageManager);
        container.register(BitmapManager.class, new BitmapManager(imageManager));
        container.register(HistoryDataSource.class, historyDataSource);
        container.register(FavoritesDataSource.class, favoritesDataSource);
        container.register(HiddenThreadsDataSource.class, hiddenThreadsDataSource);
        container.register(DownloadFileService.class, downloadFileService);
        container.register(ThreadImagesService.class, new ThreadImagesService());
        container.register(HtmlCaptchaChecker.class, new HtmlCaptchaChecker(httpStringReader));
        container.register(IconsList.class, new IconsList());
        container.register(MailruCaptchaService.class, new MailruCaptchaService(httpStringReader));
        container.register(DvachCaptchaService.class, new DvachCaptchaService());


        httpClient.setCookie(mSettings.getCloudflareClearanceCookie());
        httpClient.setCookie(mSettings.getPassCodeCookie());
        httpClient.setCookie(mSettings.getAdultAccessCookie());
    }

    public void rebuildAppComponent(){
        mAppComponent = buildAppComponent();
    }

    protected AppComponent buildAppComponent() {
        return getBaseComponent().plus(new NetModule(), new DataRepositoryModule(), new WebsiteModule());
    }

    public BaseComponent buildBaseComponent() {
        return DaggerBaseComponent.builder()
                .baseModule(new BaseModule(this))
                .navigationModule(new NavigationModule())
                .build();
    }

}
