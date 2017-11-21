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

    public AppModule(MainApplication application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    MainApplication providesApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    ApplicationSettings providesApplicationSettings() {
        return new ApplicationSettings(mApplication.getApplicationContext());
    }

    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences(MainApplication application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

}