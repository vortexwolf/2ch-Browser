package ua.in.quireg.chan.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import ua.in.quireg.chan.db.DvachSqlHelper;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.db.HiddenThreadsDataSource;
import ua.in.quireg.chan.db.HistoryDataSource;
import ua.in.quireg.chan.domain.NetworkResponseReader;
import ua.in.quireg.chan.repositories.BoardsRepository;
import ua.in.quireg.chan.settings.ApplicationSettings;

/**
 * Created by Arcturus Mengsk on 11/21/2017, 2:04 PM.
 * 2ch-Browser
 */

@Module
public class DataRepositoryModule {

    @Provides
    @Singleton
    BoardsRepository providesBoardsRepository(ApplicationSettings applicationSettings,
                                              NetworkResponseReader networkResponseReader,
                                              OkHttpClient okHttpClient) {
        return new BoardsRepository(applicationSettings, networkResponseReader, okHttpClient);
    }

    @Provides
    @Singleton
    DvachSqlHelper providesDvachSqlHelper(Context context) {
        return new DvachSqlHelper(context);
    }

    @Provides
    @Singleton
    FavoritesDataSource providesFavoritesDataSource(DvachSqlHelper dvachSqlHelper) {
        FavoritesDataSource favoritesDataSource = new FavoritesDataSource(dvachSqlHelper);
        favoritesDataSource.open();
        return favoritesDataSource;
    }

    @Provides
    @Singleton
    HistoryDataSource providesHistoryDataSource(DvachSqlHelper dvachSqlHelper) {
        HistoryDataSource historyDataSource = new HistoryDataSource(dvachSqlHelper);
        historyDataSource.open();
        return historyDataSource;
    }

    @Provides
    @Singleton
    HiddenThreadsDataSource providesHiddenThreadsDataSource(DvachSqlHelper dvachSqlHelper) {
        HiddenThreadsDataSource hiddenThreadsDataSource = new HiddenThreadsDataSource(dvachSqlHelper);
        hiddenThreadsDataSource.open();
        return hiddenThreadsDataSource;
    }
}
