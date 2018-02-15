package ua.in.quireg.chan.repositories;

import java.util.List;

import io.reactivex.Observable;
import ua.in.quireg.chan.models.domain.BoardModel;

/**
 * Created by Arcturus Mengsk on 1/20/2018, 4:42 AM.
 * 2ch-Browser
 */

public interface BoardsRepository {

    Observable<List<BoardModel>> getLocalBoards();

    Observable<List<BoardModel>> getRemoteBoards();

}
