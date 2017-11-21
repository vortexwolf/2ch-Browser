package ua.in.quireg.chan.common;

import ua.in.quireg.chan.services.CacheDirectoryManager;
import ua.in.quireg.chan.views.activities.BaseActivity;
import ua.in.quireg.chan.views.fragments.BoardsListFragment;
import ua.in.quireg.chan.views.fragments.AppPreferenceFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules={AppModule.class, NetModule.class})
public interface AppComponent {

    void inject(BoardsListFragment fragment);
    void inject(BaseActivity activity);
    void inject(CacheDirectoryManager cacheDirectoryManager);
    void inject(AppPreferenceFragment appPreferenceFragment);
}
