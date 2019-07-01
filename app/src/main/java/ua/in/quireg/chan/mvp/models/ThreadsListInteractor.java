package ua.in.quireg.chan.mvp.models;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.models.domain.ThreadModel;
import ua.in.quireg.chan.models.presentation.IThreadListEntity;
import ua.in.quireg.chan.models.presentation.ThreadItemViewModel;
import ua.in.quireg.chan.repositories.ThreadsRepository;

/**
 * Created by Arcturus Mengsk on 07.08.18.
 * 2ch-Browser
 */
public class ThreadsListInteractor {

    @Inject
    ThreadsRepository mThreadsRepository;
    @Inject
    MainApplication mApplication;

    public ThreadsListInteractor() {
        super();
        MainApplication.getAppComponent().inject(this);
    }

    public Single<List<IThreadListEntity>> getThreads(String board, int page) {
        return mThreadsRepository.getRemoteThreads(board, page)
                .observeOn(Schedulers.computation())
                .flatMap((threadModelList) -> {
                    List<IThreadListEntity> result = new ArrayList<>();
                    for (ThreadModel model:threadModelList) {
                        result.add(new ThreadItemViewModel(
                                Websites.getDefault(), board, model, mApplication.getTheme()));
                    }
                    return Single.just(result);
                });
    }
}
