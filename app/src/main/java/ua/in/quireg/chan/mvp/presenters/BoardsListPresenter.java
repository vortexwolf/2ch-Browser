package ua.in.quireg.chan.mvp.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.mvp.models.BoardsListModel;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.mvp.views.BoardsListView;
import ua.in.quireg.chan.services.NavigationService;

/**
 * Created by Arcturus Mengsk on 11/21/2017, 5:06 PM.
 * 2ch-Browser
 */

@InjectViewState
public class BoardsListPresenter extends MvpPresenter<BoardsListView> {

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
    public void onDestroy() {
        Timber.v("onDestroy()");
        compositeDisposable.clear();
        super.onDestroy();
    }

    @SuppressWarnings("Convert2MethodRef")
    public void requestBoards(boolean localOnly) {
        Timber.v("requestBoards()");

        compositeDisposable.clear();

        compositeDisposable.add(
                mBoardsListModel.getBoards(localOnly)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                (boards) -> {
                                    getViewState().clearBoards();
                                    getViewState().setBoards(boards);
                                },
                                (ex) -> Timber.e(ex),
                                () -> updateFavoriteBoards()
                        )
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

        NavigationService.getInstance().navigateBoard(Websites.getDefault().name(), boardModel.getId());
    }

    public boolean isFavoriteBoard(BoardModel boardModel) {
        Timber.v("isFavoriteBoard()");

        return mBoardsListModel.isFavoriteBoard(boardModel);
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

    private void updateFavoriteBoards() {
        compositeDisposable.add(
                mBoardsListModel.getFavBoards()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                (favBoards) -> getViewState().setFavBoards(favBoards)
                        )
        );
    }
}
