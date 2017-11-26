package ua.in.quireg.chan.common;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Observable;
import ua.in.quireg.chan.db.DvachSqlHelper;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.db.HiddenThreadsDataSource;
import ua.in.quireg.chan.db.HistoryDataSource;
import ua.in.quireg.chan.models.domain.BoardModel;

/**
 * Created by Arcturus Mengsk on 11/21/2017, 2:04 PM.
 * 2ch-Browser
 */

@Module
public class DataRepositoryModule {

    @Provides
    @Singleton
    Observable<List<BoardModel>> providesBoards(){
        return Observable.just(new ArrayList<>());
    }

    @Provides
    @Singleton
    DvachSqlHelper providesDvachSqlHelper(Context context){
        return new DvachSqlHelper(context);
    }

    @Provides
    @Singleton
    FavoritesDataSource providesFavouritesDataSource(DvachSqlHelper dvachSqlHelper){
        FavoritesDataSource favoritesDataSource = new FavoritesDataSource(dvachSqlHelper);
        favoritesDataSource.open();
        return favoritesDataSource;
    }

    @Provides
    @Singleton
    HistoryDataSource providesHistoryDataSource(DvachSqlHelper dvachSqlHelper){
        HistoryDataSource historyDataSource = new HistoryDataSource(dvachSqlHelper);
        historyDataSource.open();
        return historyDataSource;
    }

    @Provides
    @Singleton
    HiddenThreadsDataSource providesHiddenThreadsDataSource(DvachSqlHelper dvachSqlHelper){
        HiddenThreadsDataSource hiddenThreadsDataSource = new HiddenThreadsDataSource(dvachSqlHelper);
        hiddenThreadsDataSource.open();
        return hiddenThreadsDataSource;
    }
}
