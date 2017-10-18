package ua.in.quireg.chan.common;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ua.in.quireg.chan.settings.ApplicationSettings;

@Module
public class AppModule {

    MainApplication mApplication;
    ApplicationSettings mApplicationSettings;

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

}