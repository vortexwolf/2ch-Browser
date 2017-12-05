package ua.in.quireg.chan.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.domain.ApiReader;
import ua.in.quireg.chan.domain.ApiReaderImpl;
import ua.in.quireg.chan.mvp.presenters.MainActivityPresenter;
import ua.in.quireg.chan.mvp.presenters.MainActivityPresenterImpl;
import ua.in.quireg.chan.settings.ApplicationSettings;

@Module
public class BaseModule {

    private MainApplication mApplication;

    public BaseModule(MainApplication application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    MainApplication providesApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    Context providesApplicationContext() {
        return mApplication.getApplicationContext();
    }

    @Provides
    @Singleton
    ApplicationSettings providesApplicationSettings(Context context, SharedPreferences preferences) {
        return new ApplicationSettings(context, preferences);
    }
    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @Singleton
    MainActivityPresenter provideMainActivityPresenter() {
        return new MainActivityPresenterImpl();
    }



}