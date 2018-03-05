package ua.in.quireg.chan.di;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import ua.in.quireg.chan.db.DvachSqlHelper;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.db.HiddenThreadsDataSource;
import ua.in.quireg.chan.db.HistoryDataSource;
import ua.in.quireg.chan.domain.ApiReader;
import ua.in.quireg.chan.repositories.BoardsRepository;
import ua.in.quireg.chan.repositories.BoardsRepositoryImpl;
import ua.in.quireg.chan.services.presentation.OpenTabsManager;

/**
 * Created by Arcturus Mengsk on 11/21/2017, 2:04 PM.
 * 2ch-Browser
 */

@Module
public class DataRepositoryModule {

    @Provides
    @AppScope
    BoardsRepository providesBoardsRepository(ApiReader apiReader, OkHttpClient okHttpClient, Context context) {
        return new BoardsRepositoryImpl(apiReader, okHttpClient, context);
    }

    @Provides
    @AppScope
    DvachSqlHelper providesDvachSqlHelper(Context context) {
        return new DvachSqlHelper(context);
    }

    @Provides
    @AppScope
    FavoritesDataSource providesFavoritesDataSource(DvachSqlHelper dvachSqlHelper) {
        return new FavoritesDataSource(dvachSqlHelper);
    }

    @Provides
    @AppScope
    HistoryDataSource providesHistoryDataSource(DvachSqlHelper dvachSqlHelper) {
        return new HistoryDataSource(dvachSqlHelper);
    }

    @Provides
    @AppScope
    HiddenThreadsDataSource providesHiddenThreadsDataSource(DvachSqlHelper dvachSqlHelper) {
        return new HiddenThreadsDataSource(dvachSqlHelper);
    }

    @Provides
    @AppScope
    OpenTabsManager providesOpenTabsManager() {
        return new OpenTabsManager();
    }
}
