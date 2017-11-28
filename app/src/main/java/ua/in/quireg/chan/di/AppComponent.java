package ua.in.quireg.chan.di;

import javax.inject.Singleton;

import dagger.Component;
import ua.in.quireg.chan.ui.adapters.HistoryAdapter;
import ua.in.quireg.chan.mvp.models.BoardsListModel;
import ua.in.quireg.chan.mvp.presenters.BoardsListPresenter;
import ua.in.quireg.chan.services.CacheDirectoryManager;
import ua.in.quireg.chan.services.presentation.OpenTabsManager;
import ua.in.quireg.chan.ui.activities.BaseActivity;
import ua.in.quireg.chan.ui.fragments.AppPreferenceFragment;
import ua.in.quireg.chan.ui.fragments.BoardsListFragment;
import ua.in.quireg.chan.ui.fragments.HistoryFragment;

@Singleton
@Component(modules={AppModule.class, NetModule.class, DataRepositoryModule.class})
public interface AppComponent {

    void inject(BaseActivity activity);
    void inject(BoardsListFragment fragment);
    void inject(CacheDirectoryManager cacheDirectoryManager);
    void inject(AppPreferenceFragment appPreferenceFragment);
    void inject(BoardsListPresenter boardsListPresenter);
    void inject(OpenTabsManager openTabsManager);
    void inject(BoardsListModel boardsListModel);
    void inject(HistoryFragment historyFragment);
    void inject(HistoryAdapter historyAdapter);
}
