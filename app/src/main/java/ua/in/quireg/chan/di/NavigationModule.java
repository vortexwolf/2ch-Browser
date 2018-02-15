package ua.in.quireg.chan.di;

import android.os.Bundle;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.terrakok.cicerone.Cicerone;
import ru.terrakok.cicerone.NavigatorHolder;
import ua.in.quireg.chan.mvp.routing.MainRouter;
import ua.in.quireg.chan.mvp.routing.TabsTransactionHistory;

/**
 * Created by Arcturus Mengsk on 12/1/2017, 6:36 AM.
 * 2ch-Browser
 */

@Module
public class NavigationModule {

    private Cicerone<MainRouter> cicerone;

    public NavigationModule() {
        cicerone = Cicerone.create(new MainRouter());
    }

    @Provides
    @Singleton
    MainRouter provideRouter() {
        return cicerone.getRouter();
    }

    @Provides
    @Singleton
    NavigatorHolder provideNavigatorHolder() {
        return cicerone.getNavigatorHolder();
    }

    @Provides
    @Singleton
    Bundle providePersistentBundle() {
        return new Bundle();
    }

    @Provides
    @Singleton
    TabsTransactionHistory provideTabsTransactionHistory() {
        return new TabsTransactionHistory();
    }
}
