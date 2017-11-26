package ua.in.quireg.chan.common;

import android.content.Context;
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
    Context providesApplicationContext() {
        return mApplication.getApplicationContext();
    }

    @Provides
    @Singleton
    ApplicationSettings providesApplicationSettings(Context context) {
        return new ApplicationSettings(context);
    }

    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}