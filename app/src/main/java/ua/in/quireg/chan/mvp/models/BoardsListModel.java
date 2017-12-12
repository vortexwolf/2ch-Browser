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
import ua.in.quireg.chan.repositories.BoardsRepository;
import ua.in.quireg.chan.settings.ApplicationSettings;

/**
 * Created by Arcturus Mengsk on 11/21/2017, 5:06 PM.
 * 2ch-Browser
 */

public class BoardsListModel {

    @Inject protected BoardsRepository mBoardsRepository;
    @Inject protected FavoritesDataSource mFavoritesDataSource;
    @Inject protected ApplicationSettings mApplicationSettings;

    public BoardsListModel() {
        MainApplication.getAppComponent().inject(this);
    }

    public Observable<List<BoardModel>> getBoards(boolean localOnly) {

        if (localOnly) {
            return getLocalVisibleBoards();
        } else {
            return Observable.zip(
                    mBoardsRepository.getLocalBoards(),
                    mBoardsRepository.getRemoteBoards(),
                    (local, remote) -> {

                        ArrayList<BoardModel> empty = new ArrayList<>();

                        if (remote == null || remote.isEmpty()) {
                            Timber.e("Received empty boards list!");

                            return empty;
                        } else if (!areEqual(remote, local) || local.isEmpty()) {
                            Timber.d("Boards list have expired, updating...");

                            mBoardsRepository.setLocalBoards(remote);
                            return remote;
                        } else {
                            Timber.d("Boards are up to date ^_^");

                            return empty;
                        }
                    })
                    .startWith(getLocalVisibleBoards())
                    .filter(boardModels -> !boardModels.isEmpty());
        }
    }

    public Observable<List<BoardModel>> getFavoriteBoards() {

        return Observable.just(mFavoritesDataSource.getFavoriteBoards())
                .subscribeOn(Schedulers.io())
                .zipWith(mBoardsRepository.getLocalBoards(), (favoriteBoardsEntities, localBoards) -> {

                    ArrayList<BoardModel> favoriteBoards = new ArrayList<>();

                    for (FavoritesEntity fe: favoriteBoardsEntities) {
                        boolean matchFound = false;

                        for (BoardModel board : localBoards) {
                            if (board.getId().equals(fe.getBoard())) {
                                favoriteBoards.add(board);
                                matchFound = true;
                            }
                        }

                        if(!matchFound) {
                            BoardModel boardModel = new BoardModel();

                            if (fe.getBoard() != null) {
                                boardModel.setId(fe.getBoard());
                            }
                            if (fe.getTitle() != null) {
                                boardModel.setName(fe.getTitle());
                            }
                        }
                    }
                    return favoriteBoards;

                });
    }

    public void addToFavorites(BoardModel boardModel) {
        mFavoritesDataSource.addToFavorites(Websites.getDefault().name(), boardModel.getId(), null, null);
    }

    public void removeFromFavorites(BoardModel boardModel) {
        mFavoritesDataSource.removeFromFavorites(Websites.getDefault().name(), boardModel.getId(), null);
    }

    public boolean isFavorite(BoardModel boardModel) {
        return !mFavoritesDataSource.hasFavorites(Websites.getDefault().name(), boardModel.getId(), null);
    }

    private Observable<List<BoardModel>> getLocalVisibleBoards() {
        return mBoardsRepository.getLocalBoards().map(this::removeHiddenBoards);
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
