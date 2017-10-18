package ua.in.quireg.chan.common;

import ua.in.quireg.chan.ui.activities.BaseActivity;
import ua.in.quireg.chan.ui.fragments.BoardsListFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules={AppModule.class, NetModule.class})
public interface AppComponent {
    void inject(BoardsListFragment activity);
    void inject(BaseActivity activity);
}
