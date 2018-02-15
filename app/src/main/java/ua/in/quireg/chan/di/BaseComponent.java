package ua.in.quireg.chan.di;


import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Arcturus Mengsk on 12/5/2017, 2:51 AM.
 * 2ch-Browser
 */
@Singleton
@Component(modules={BaseModule.class, NavigationModule.class})
public interface BaseComponent {

    AppComponent plus(NetModule netModule, DataRepositoryModule dataRepositoryModule, WebsiteModule websiteModule);


}
