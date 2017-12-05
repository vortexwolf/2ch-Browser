package ua.in.quireg.chan.mvp.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.mvp.models.BoardsListModel;
import ua.in.quireg.chan.mvp.views.BoardsListView;

/**
 * Created by Arcturus Mengsk on 11/21/2017, 5:06 PM.
 * 2ch-Browser
 */

@InjectViewState
public class BoardsListPresenter extends MvpPresenter<BoardsListView> {

    @Inject MainActivityPresenter mMainActivityPresenter;

    private boolean mFirstViewAttached = false;

    private BoardsListModel mBoardsListModel = new BoardsListModel();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public BoardsListPresenter() {
        super();
        MainApplication.getAppComponent().inject(this);
    }

    @Override
    public void attachView(BoardsListView view) {
        super.attachView(view);
        Timber.v("attachView()");

        if (mFirstViewAttached) {
            requestBoards(true);
        } else {
            mFirstViewAttached = true;
            requestBoards(false);
        }
    }

    @Override
    public void detachView(BoardsListView view) {
        super.detachView(view);
        Timber.v("detachView()");
        compositeDisposable.clear();
    }

    public void requestBoards(boolean localOnly) {
        Timber.v("requestBoards()");

        compositeDisposable.add(
                Observable.combineLatest(
                        mBoardsListModel.getBoards(localOnly).observeOn(AndroidSchedulers.mainThread()),
                        mBoardsListModel.getFavoriteBoards().observeOn(AndroidSchedulers.mainThread()),
                        (boards, favBoards) -> {

                            getViewState().clearBoards();
                            getViewState().setBoards(boards);
                            getViewState().setFavBoards(favBoards);

                            return boards;
                        })
                        .map((b) -> "Boards view updated")
                        .subscribe(Timber::d, Timber::e)
        );
    }

    public void onBoardClick(String boardCode) {
        Timber.v("onBoardClick(String)");

        if (validateBoardCode(boardCode) && mBoardsListModel.findBoardByCode(boardCode) != null) {
            onBoardClick(mBoardsListModel.findBoardByCode(boardCode));
        } else {
            getViewState().showBoardError(boardCode);
        }

    }

    public void onBoardClick(BoardModel boardModel) {
        Timber.v("onBoardClick(BoardModel)");
        getViewState().hideSoftKeyboard();

        mMainActivityPresenter.navigateBoard(Websites.getDefault().name(), boardModel.getId());

    }

    public boolean isFavoriteBoard(BoardModel boardModel) {
        Timber.v("isFavorite()");

        return mBoardsListModel.isFavorite(boardModel);
    }

    public void addToFavorites(BoardModel boardModel) {
        Timber.v("addToFavorites()");

        getViewState().addFavoriteBoard(boardModel);
        mBoardsListModel.addToFavorites(boardModel);
    }

    public void removeFromFavorites(BoardModel boardModel) {
        Timber.v("removeFromFavorites()");

        getViewState().removeFavoriteBoard(boardModel);
        mBoardsListModel.removeFromFavorites(boardModel);
    }

    private boolean validateBoardCode(String boardCode) {
        Timber.v("validateBoardCode()");
        boardCode = boardCode.replaceAll("/", "").toLowerCase(Locale.US);

        return !StringUtils.isEmpty(boardCode) && Constants.BOARD_CODE_PATTERN.matcher(boardCode).matches();

    }

}
