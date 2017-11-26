package ua.in.quireg.chan.mvp.presenters;

import android.content.Context;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;
import ua.in.quireg.chan.adapters.BoardsListAdapter;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.db.FavoritesEntity;
import ua.in.quireg.chan.models.BoardsListModel;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.models.presentation.BoardEntity;
import ua.in.quireg.chan.models.presentation.SectionEntity;
import ua.in.quireg.chan.mvp.views.BoardsListView;
import ua.in.quireg.chan.services.NavigationService;

/**
 * Created by Arcturus Mengsk on 11/21/2017, 5:06 PM.
 * 2ch-Browser
 */

@InjectViewState
@SuppressWarnings("WeakerAccess")
public class BoardsListPresenter extends MvpPresenter<BoardsListView> {

    @Inject protected Context mContext;
    @Inject protected FavoritesDataSource mFavoritesDatasource;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private BoardsListModel mBoardsListModel = new BoardsListModel();

    private BoardsListAdapter mAdapter;

    public BoardsListPresenter() {
        super();
        MainApplication.getComponent().inject(this);
        mAdapter = new BoardsListAdapter(mContext);
    }

    @Override protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        requestBoardsListFromServer();
    }

    @Override public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    public BoardsListAdapter getBoardListAdapter() {
        return mAdapter;
    }

    public void requestBoardsListFromServer() {
        Timber.v("requestBoardsListFromServer()");

        Disposable d = mBoardsListModel.getBoardsList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        boardModels -> {
                            Timber.d("Running on thread named: %s", Thread.currentThread().getName());
                            updateBoardsUIList(boardModels);
                        },
                        Timber::e
                );

        compositeDisposable.add(d);
    }

    public boolean isFavouriteBoard(BoardEntity item) {
        return !mFavoritesDatasource.hasFavorites(Websites.getDefault().name(), item.getCode(), null);
    }

    private boolean updateBoardsUIList(List<BoardModel> boardModels) {
        Timber.v("updateBoardsUIList()");

        mAdapter.clear();

        String[] categorySequenceToBeShown = new String[]{
                "Игры",
                "Политика",
                "Японская культура",
                "Разное",
                "Творчество",
                "Тематика",
                "Техника и софт",
                "Взрослым",
                "Пользовательские"
        };

        String currentCategory = null;

        for (String category : categorySequenceToBeShown) {
            for (BoardModel board : boardModels) {
                // ignore all boards except of matching category.
                if (!board.getCategory().equals(category)) {
                    continue;
                }
                // add group header
                if (board.getCategory() != null && !board.getCategory().equals(currentCategory)) {
                    currentCategory = board.getCategory();
                    mAdapter.add(new SectionEntity(currentCategory));
                }
                // add item
                mAdapter.add(new BoardEntity(board.getId(), board.getName(), board.getBumpLimit()));
            }
        }

        // add favorite boards
        List<FavoritesEntity> favoriteBoards = mFavoritesDatasource.getFavoriteBoards();
        for (FavoritesEntity f : favoriteBoards) {
            String boardName = f.getBoard();
            mAdapter.addItemToFavoritesSection(boardName, findBoardByCode(boardName));
        }
        mAdapter.notifyDataSetChanged();
        return true;
    }

    public void checkAndNavigateBoard(String boardCode) {
        boolean success = validateBoardCode(boardCode);
        if (!success) {
            getViewState().showUnrecognizedBoardError(boardCode);
        } else {
            NavigationService.getInstance().navigateBoard(Websites.getDefault().name(), boardCode);
        }
    }

    public void addToFavorites(String boardCode) {
        boolean success = validateBoardCode(boardCode);
        if (!success) {
            getViewState().showUnrecognizedBoardError(boardCode);
        } else {
            mFavoritesDatasource.addToFavorites(Websites.getDefault().name(), boardCode, null, null);
            mAdapter.addItemToFavoritesSection(boardCode, findBoardByCode(boardCode));
        }
    }

    public void removeFromFavorites(BoardEntity model) {
        mFavoritesDatasource.removeFromFavorites(Websites.getDefault().name(), model.getCode(), null);
        mAdapter.removeItemFromFavoritesSection(model);
    }

    private boolean validateBoardCode(String boardCode) {
        boardCode = boardCode.replaceAll("/", "").toLowerCase(Locale.US);

        return !StringUtils.isEmpty(boardCode) && Constants.BOARD_CODE_PATTERN.matcher(boardCode).matches();

    }

    private BoardModel findBoardByCode(String id) {
        return mBoardsListModel.findBoardByCode(id);
    }

}
