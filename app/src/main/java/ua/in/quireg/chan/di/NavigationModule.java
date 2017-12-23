package ua.in.quireg.chan.di;

import android.os.Bundle;

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
    @AppScope
    MainRouter provideRouter() {
        return cicerone.getRouter();
    }

    @Provides
    @AppScope
    NavigatorHolder provideNavigatorHolder() {
        return cicerone.getNavigatorHolder();
    }

    @Provides
    @AppScope
    Bundle providePersistentBundle() {
        return new Bundle();
    }

    @Provides
    @AppScope
    TabsTransactionHistory provideTabsTransactionHistory() {
        return new TabsTransactionHistory();
    }
}
