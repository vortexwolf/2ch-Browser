package ua.in.quireg.chan.common;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ua.in.quireg.chan.settings.ApplicationSettings;

@Module
public class AppModule {

    private MainApplication mApplication;
    private ApplicationSettings mApplicationSettings;

    public AppModule(MainApplication application, ApplicationSettings Settings) {
        mApplication = application;
        mApplicationSettings = Settings;
    }

    @Provides
    @Singleton
    MainApplication providesApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    ApplicationSettings providesApplicationSettings() {
        return mApplicationSettings;
    }

    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences(MainApplication application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

}