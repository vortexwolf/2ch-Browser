package ua.in.quireg.chan.mvp.models;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
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

    public Observable<List<ThreadItemViewModel>> getThreads(String board, int page) {

        return mThreadsRepository.getRemoteThreads(board,page)
                .flatMapIterable(x -> x)
                .map((threadModel) -> new ThreadItemViewModel(Websites.getDefault(), board, threadModel, mApplication.getTheme()))
                .toList()
                .toObservable();
    }
}
