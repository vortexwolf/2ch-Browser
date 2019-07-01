package ua.in.quireg.chan.mvp.presenters;

import android.support.annotation.NonNull;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.db.HiddenThreadsDataSource;
import ua.in.quireg.chan.models.presentation.IThreadListEntity;
import ua.in.quireg.chan.models.presentation.PageDividerViewModel;
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

    public static final int FINAL_PAGE = -1;
    public static final int FIRST_PAGE = 0;

    private ThreadsListInteractor mThreadsListInteractor = new ThreadsListInteractor();
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private Disposable mRemoteThreadsDisposable;

    private static final int FETCH_THREADS_THRESHOLD = 5; //items

    private int mPage = 0;
    private AtomicBoolean mStopFetchingNewThreads = new AtomicBoolean(false);
    private AtomicBoolean mIsUpdating = new AtomicBoolean(false);

    private List<IThreadListEntity> mListItems = Collections.synchronizedList(new ArrayList<>());

    public ThreadsListPresenter() {
        super();
        MainApplication.getAppComponent().inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        refreshList();
    }

    public void refreshList() {
        requestRemoteThreads(false);
    }

    private synchronized void requestRemoteThreads(boolean gracefully) {
        if (mIsUpdating.get()) {
            return;
        } else {
            mIsUpdating.set(true);
        }
        if (mStopFetchingNewThreads.get() && gracefully) {
            return;
        }
        mStopFetchingNewThreads.set(false);
        if (gracefully) {
            mPage++;
            getViewState().startLoadingNewPage();
        } else {
            mPage = 0;
            mListItems.clear();
            getViewState().startLoadingFirstTime();
        }
        if (mRemoteThreadsDisposable != null && !mRemoteThreadsDisposable.isDisposed()) {
            mRemoteThreadsDisposable.dispose();
        }
        mThreadsListInteractor.getThreads("/b", mPage)
                .map((list) -> {
                    PageDividerViewModel p = new PageDividerViewModel();
                    if (!list.isEmpty()) {
                        p.setPage(mPage);
                        mListItems.add(p);
                        mListItems.addAll(list);
                    } else {
                        mStopFetchingNewThreads.set(true);
                        p.setPage(FINAL_PAGE);
                        mListItems.add(p);
                    }
                    return list;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<IThreadListEntity>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mRemoteThreadsDisposable = d;
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(List<IThreadListEntity> l) {
                        Timber.d("requestRemoteThreads complete");
                        mIsUpdating.set(false);
                        getViewState().setList(mListItems);
                        if (gracefully) {
                            getViewState().stopLoadingNewPage();
                        } else {
                            getViewState().stopLoadingFirstTime();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        if (mPage > 0) {
                            --mPage; //re-fetch this page
                        }
                        mIsUpdating.set(false);
                        if (gracefully) {
                            getViewState().stopLoadingNewPage();
                        } else {
                            getViewState().stopLoadingFirstTime();
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    public void setListPosition(int position, int total) {
        if (total - position <= FETCH_THREADS_THRESHOLD) {
            if (!mIsUpdating.get() && !mStopFetchingNewThreads.get()) {
                requestRemoteThreads(true);
            }
        }
    }

    public void hideThread(@NonNull ThreadItemViewModel item) {
        if (!item.isHidden()) {
            mHiddenThreadsDataSource.removeFromHiddenThreads(
                    item.getWebsite().name(), item.getBoardName(), item.getNumber());
            item.setHidden(true);
            getViewState().showThreads(Collections.singletonList(item));
        }
    }

    public void unHideThread(@NonNull ThreadItemViewModel item) {
        if (item.isHidden()) {
            mHiddenThreadsDataSource.removeFromHiddenThreads(
                    item.getWebsite().name(), item.getBoardName(), item.getNumber());
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
