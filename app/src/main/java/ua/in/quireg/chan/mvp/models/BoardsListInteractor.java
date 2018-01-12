package ua.in.quireg.chan.mvp.models;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.db.FavoritesEntity;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.models.presentation.BoardEntity;
import ua.in.quireg.chan.repositories.BoardsRepository;
import ua.in.quireg.chan.settings.ApplicationSettings;

/**
 * Created by Arcturus Mengsk on 11/21/2017, 5:06 PM.
 * 2ch-Browser
 */

public class BoardsListInteractor {

    @Inject protected BoardsRepository mBoardsRepository;
    @Inject protected FavoritesDataSource mFavoritesDataSource;
    @Inject protected ApplicationSettings mApplicationSettings;

    public BoardsListInteractor() {
        MainApplication.getAppComponent().inject(this);
    }

    public Observable<List<BoardEntity>> getBoards(boolean localOnly) {

        if (localOnly) {
            return getLocalVisibleBoards();
        }

        return Observable.combineLatest(
                mBoardsRepository.getLocalBoards(),
                mBoardsRepository.getRemoteBoards(),
                (local, remote) -> {

                    if (remote == null || remote.isEmpty()) {
                        Timber.e("Received empty boards list!");

                        return local;
                    } else if (!areEqual(remote, local) || local.isEmpty()) {
                        Timber.d("Boards list have expired, updating...");

                        mBoardsRepository.setLocalBoards(remote);
                        return remote;
                    } else {
                        Timber.d("Boards are up to date ^_^");

                        return local;
                    }
                })
                .startWith(mBoardsRepository.getLocalBoards())
                .filter(boards -> !boards.isEmpty())
                .map(this::removeHiddenBoards)
                .map(this::mapBoardModelsToEntities);

    }

    public Observable<List<BoardEntity>> getFavoriteBoards() {

        return Observable.just(mFavoritesDataSource.getFavoriteBoards())
                .subscribeOn(Schedulers.io())
                .zipWith(mBoardsRepository.getLocalBoards(), (favoriteBoardsEntities, localBoards) -> {

                    ArrayList<BoardModel> favoriteBoards = new ArrayList<>();

                    for (FavoritesEntity fe : favoriteBoardsEntities) {
                        boolean matchFound = false;

                        for (BoardModel board : localBoards) {
                            if (board.getId().equals(fe.getBoard())) {
                                favoriteBoards.add(board);
                                matchFound = true;
                            }
                        }

                        if (!matchFound) {
                            BoardEntity boardEntity = new BoardEntity();

                            if (fe.getBoard() != null) {
                                boardEntity.id = fe.getBoard();
                            }
                            if (fe.getTitle() != null) {
                                boardEntity.boardName = fe.getTitle();
                            }
                        }
                    }
                    return favoriteBoards;

                })
                .map(this::mapBoardModelsToEntities);
    }

    public void addToFavorites(BoardEntity boardEntity) {
        mFavoritesDataSource.addToFavorites(Websites.getDefault().name(), boardEntity.id, null, null);
    }

    public void removeFromFavorites(BoardEntity boardEntity) {
        mFavoritesDataSource.removeFromFavorites(Websites.getDefault().name(), boardEntity.id, null);
    }

    public boolean isFavorite(BoardEntity boardEntity) {
        return !mFavoritesDataSource.hasFavorites(Websites.getDefault().name(), boardEntity.id, null);
    }

    private Observable<List<BoardEntity>> getLocalVisibleBoards() {
        return mBoardsRepository.getLocalBoards()
                .map(this::removeHiddenBoards)
                .map(this::mapBoardModelsToEntities);

    }

    private List<BoardEntity> mapBoardModelsToEntities(List<BoardModel> models) {
        List<BoardEntity> boardEntities = new ArrayList<>();

        for (BoardModel model : models) {
            boardEntities.add(mapBoardModelToEntity(model));
        }
        return boardEntities;
    }

    private BoardEntity mapBoardModelToEntity(BoardModel model) {
        BoardEntity e = new BoardEntity();

        e.id = model.getId();
        e.boardName = model.getBoardName();
        e.bumpLimit = model.getBumpLimit();
        e.category = model.getCategory();

        return e;
    }

    private List<BoardModel> removeHiddenBoards(List<BoardModel> boards) {

        List<BoardModel> filteredBoards = new ArrayList<>(boards);
        for (BoardModel board : boards) {
            if (!isBoardVisible(board)) {
                filteredBoards.remove(board);
            }
        }
        return filteredBoards;
    }

    private boolean areEqual(List<BoardModel> first, List<BoardModel> second) {

        if (first == null || second == null || first.isEmpty() || second.isEmpty()) {
            return false;
        }

        for (BoardModel modelA : first) {
            boolean matchFound = false;
            for (BoardModel modelB : second) {
                if (modelA.getId().equals(modelB.getId())) {
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) return false;
        }
        return true;
    }

    private boolean isBoardVisible(BoardModel board) {
        return mApplicationSettings.isDisplayAllBoards() || mApplicationSettings.getAllowedBoardsIds().contains(board.getId());
    }

}
