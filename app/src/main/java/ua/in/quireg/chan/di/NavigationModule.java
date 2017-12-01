package ua.in.quireg.chan.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ua.in.quireg.chan.services.NavigationController;

/**
 * Created by Arcturus Mengsk on 12/1/2017, 6:52 AM.
 * 2ch-Browser
 */

@Module
public class NavigationModule {

    @Provides
    @Singleton
    NavigationController provideNavigationController() {
        return new NavigationController();
    }

}