package ua.in.quireg.chan.repositories;

import java.util.List;

import io.reactivex.Observable;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.settings.ApplicationSettings;

/**
 * Created by Arcturus Mengsk on 11/28/2017, 1:31 PM.
 * 2ch-Browser
 */

public class BoardsRepository {

    Observable<List<BoardModel>> providesBoards(ApplicationSettings settings) {
        return Observable.just(settings.getBoards());
    }
}
