package ua.in.quireg.chan.mvp.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.models.presentation.BoardEntity;
import ua.in.quireg.chan.mvp.models.BoardsListInteractor;
import ua.in.quireg.chan.mvp.routing.MainRouter;
import ua.in.quireg.chan.mvp.views.BoardsListView;

/**
 * Created by Arcturus Mengsk on 11/21/2017, 5:06 PM.
 * 2ch-Browser
 */

@InjectViewState
public class BoardsListPresenter extends MvpPresenter<BoardsListView> {

    @Inject MainRouter mMainRouter;

    private BoardsListInteractor mBoardsListInteractor = new BoardsListInteractor();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private boolean updateFromServer = true;

    public BoardsListPresenter() {
        MainApplication.getAppComponent().inject(this);
    }

    @Override
    public void attachView(BoardsListView view) {
        super.attachView(view);

        Timber.v("attachView()");

        updateBoardsList(false);

        if (updateFromServer) {

            updateBoardsList(true);

            updateFromServer = false;
        }

    }

    @Override
    public void detachView(BoardsListView view) {
        super.detachView(view);
        Timber.v("detachView()");

        compositeDisposable.clear();
    }

    public void updateBoardsList(boolean remote) {
        Timber.v("updateBoardsList() %s", remote ? "remote" : "local");

        Disposable d = mBoardsListInteractor.getBoards(remote)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                            getViewState().setBoards(list);
                        },
                        Timber::e,
                        () -> Timber.d("updateBoardsList() %s completed", remote ? "remote" : "local"));

        compositeDisposable.add(d);

    }

    public void onBoardClick(String boardCode) {
        Timber.v("onBoardClick(String)");

        if (validateBoardCode(boardCode)) {
            BoardEntity boardEntity = new BoardEntity();
            boardEntity.id = boardCode;
            onBoardClick(boardEntity);
        } else {
            mMainRouter.showSystemMessage(R.string.warning_enter_board);
        }

    }

    public void onBoardClick(BoardEntity boardEntity) {
        Timber.v("onBoardClick(BoardModel)");

        mMainRouter.navigateBoard(Websites.getDefault().name(), boardEntity.id, true);

    }

    public void addToFavorites(BoardEntity boardEntity) {
        Timber.v("addToFavorites()");

        mBoardsListInteractor.addToFavorites(boardEntity);
        updateBoardsList(false);
    }

    public void removeFromFavorites(BoardEntity boardEntity) {
        Timber.v("removeFromFavorites()");

        mBoardsListInteractor.removeFromFavorites(boardEntity);
        updateBoardsList(false);
    }

    private boolean validateBoardCode(String boardCode) {
        Timber.v("validateBoardCode()");
        boardCode = boardCode.replaceAll("/", "").toLowerCase(Locale.US);

        return !StringUtils.isEmpty(boardCode) && Constants.BOARD_CODE_PATTERN.matcher(boardCode).matches();

    }

}
