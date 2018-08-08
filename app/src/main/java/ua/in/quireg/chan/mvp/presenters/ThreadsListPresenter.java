package ua.in.quireg.chan.mvp.presenters;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import java.util.Collections;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.db.HiddenThreadsDataSource;
import ua.in.quireg.chan.models.presentation.ThreadItemViewModel;
import ua.in.quireg.chan.mvp.models.ThreadsListInteractor;
import ua.in.quireg.chan.mvp.routing.MainRouter;
import ua.in.quireg.chan.mvp.views.ThreadsListView;
import ua.in.quireg.chan.services.presentation.PagesSerializationService;

/**
 * Created by Arcturus Mengsk on 3/17/2018, 4:34 AM.
 * 2ch-Browser
 */

@InjectViewState
public class ThreadsListPresenter extends MvpPresenter<ThreadsListView> {

    @Inject
    HiddenThreadsDataSource mHiddenThreadsDataSource;
    @Inject
    PagesSerializationService mPagesSerializationService;
    @Inject MainRouter mMainRouter;

    private ThreadsListInteractor mThreadsListInteractor = new ThreadsListInteractor();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private int mRecyclerViewPosition = RecyclerView.NO_POSITION;
    private int mPage = 0;
    private boolean updateInProgress = false;

    public ThreadsListPresenter() {
        super();
        MainApplication.getAppComponent().inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        requestThreads();
    }

    public void requestThreads() {
        requestThreadsUpdate(0, false);
    }

    public void requestThreadsUpdate(int page, boolean gracefully) {
        if (updateInProgress) {
            return;
        }
        updateInProgress = true;

        Disposable d = mThreadsListInteractor.getThreads("/b", page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (l) -> {
                            if (!gracefully) getViewState().clearList();
                            getViewState().showThreads(l);
                        },
                        (e) -> {
                            Timber.e(e);
                            updateInProgress = false;
                        },
                        () -> {
                            Timber.d("updateInProgress = false");
                            mPage = page;
                            updateInProgress = false;
                        }
                );
        compositeDisposable.add(d);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    public void currentRecyclerViewPosition(int position, int total) {
        mRecyclerViewPosition = position;
        if (total - position <= 5) {
            requestThreadsUpdate(mPage + 1, true);
        }
    }

    public void hideThread(@NonNull ThreadItemViewModel item) {
        if (!item.isHidden()) {
            mHiddenThreadsDataSource.removeFromHiddenThreads(item.getWebsite().name(), item.getBoardName(), item.getNumber());
            item.setHidden(true);
            getViewState().showThreads(Collections.singletonList(item));
        }
    }

    public void unHideThread(@NonNull ThreadItemViewModel item) {
        if (item.isHidden()) {
            mHiddenThreadsDataSource.removeFromHiddenThreads(item.getWebsite().name(), item.getBoardName(), item.getNumber());
            item.setHidden(false);
            getViewState().showThreads(Collections.singletonList(item));
        }
    }

    public void onItemClick(@NonNull ThreadItemViewModel item) {
        if (item.isHidden()) {
            unHideThread(item);
        } else {
            mMainRouter.navigateThread(item.getNumber(), false);
        }
    }

}
