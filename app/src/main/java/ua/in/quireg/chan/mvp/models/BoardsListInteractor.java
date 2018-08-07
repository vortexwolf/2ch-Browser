package ua.in.quireg.chan.mvp.models;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.db.DvachRoomDatabase;
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

    @Inject BoardsRepository mBoardsRepository;
    @Inject FavoritesDataSource mFavoritesDataSource;
    @Inject protected ApplicationSettings mApplicationSettings;

    public BoardsListInteractor() {
        MainApplication.getAppComponent().inject(this);
    }

    public Observable<List<BoardEntity>> getBoards(boolean remote) {

        if (!remote) {
            return mBoardsRepository.getLocalBoards()
                    .map(this::mapBoardModelsToEntities)
                    .map(this::setHiddenBoards)
                    .map(this::setFavoriteBoards)
                    .subscribeOn(Schedulers.io());
        } else {
            return mBoardsRepository.getRemoteBoards()
                    .map(this::mapBoardModelsToEntities)
                    .map(this::setHiddenBoards)
                    .map(this::setFavoriteBoards)
                    .subscribeOn(Schedulers.io());
        }

    }

    public void addToFavorites(BoardEntity boardEntity) {
        mFavoritesDataSource.addToFavorites(Websites.getDefault().name(), boardEntity.id, null, null);
    }

    public void removeFromFavorites(BoardEntity boardEntity) {
        mFavoritesDataSource.removeFromFavorites(Websites.getDefault().name(), boardEntity.id, null);
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

    private List<BoardEntity> setHiddenBoards(List<BoardEntity> boards) {

        for (BoardEntity board : boards) {
            if (!isBoardVisible(board)) {
                board.isVisible = false;
            }
        }
        return boards;
    }

    private List<BoardEntity> setFavoriteBoards(List<BoardEntity> boards) {

        for (FavoritesEntity fe : mFavoritesDataSource.getFavoriteBoards()) {

            boolean matchFound = false;

            for (BoardEntity board : boards) {
                if (board.id.equals(fe.getBoard())) {

                    board.isFavorite = true;
                    matchFound = true;

                    break;
                }
            }

            if (!matchFound) {
                BoardEntity boardEntity = new BoardEntity();
                boardEntity.id = fe.getBoard();
                boardEntity.boardName = fe.getTitle();
                boardEntity.isFavorite = true;
                boards.add(boardEntity);
            }
        }

        return boards;
    }

    private boolean isBoardVisible(BoardEntity board) {
        return mApplicationSettings.isDisplayAllBoards() ||
                mApplicationSettings.getAllowedBoardsIds().contains(board.id) || board.isFavorite;
    }

}
