package ua.in.quireg.chan.common;

import ua.in.quireg.chan.models.BoardsListModel;
import ua.in.quireg.chan.mvp.presenters.BoardsListPresenter;
import ua.in.quireg.chan.services.CacheDirectoryManager;
import ua.in.quireg.chan.services.presentation.OpenTabsManager;
import ua.in.quireg.chan.views.activities.BaseActivity;
import ua.in.quireg.chan.views.fragments.BoardsListFragment;
import ua.in.quireg.chan.views.fragments.AppPreferenceFragment;
import ua.in.quireg.chan.views.fragments.HistoryFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules={AppModule.class, NetModule.class, DataRepositoryModule.class})
public interface AppComponent {

    void inject(BoardsListFragment fragment);
    void inject(BaseActivity activity);
    void inject(CacheDirectoryManager cacheDirectoryManager);
    void inject(AppPreferenceFragment appPreferenceFragment);
    void inject(BoardsListPresenter boardsListPresenter);
    void inject(OpenTabsManager openTabsManager);
    void inject(BoardsListModel boardsListModel);
    void inject(HistoryFragment historyFragment);
}
