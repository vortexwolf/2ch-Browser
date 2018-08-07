package ua.in.quireg.chan.repositories;

import java.util.List;

import io.reactivex.Observable;
import ua.in.quireg.chan.models.domain.ThreadModel;

/**
 * Created by Arcturus Mengsk on 1/20/2018, 4:42 AM.
 * 2ch-Browser
 */

public interface ThreadsRepository {

    Observable<List<ThreadModel>> getLocalThreads(String board);

    Observable<List<ThreadModel>> getRemoteThreads(String board, int page);

}
