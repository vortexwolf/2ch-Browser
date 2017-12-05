package ua.in.quireg.chan.mvp.models;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;
import ua.in.quireg.chan.boards.makaba.MakabaModelsMapper;
import ua.in.quireg.chan.boards.makaba.models.MakabaBoardInfo;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.db.FavoritesDataSource;
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
                    mBoardsRepository.getLocalBoards(), mBoardsRepository.getRemoteBoards(), (local, remote) -> {

                        ArrayList<BoardModel> empty = new ArrayList<>();

                        if (remote == null || remote.isEmpty()) {
                            Timber.e("Received empty boards list!");

                            return empty;
                        } else if (!areEqual(local, remote) || local.isEmpty()) {
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

        return Observable.fromIterable(mFavoritesDataSource.getFavoriteBoards())
                .subscribeOn(Schedulers.io())
                .map((b) -> findBoardByCode(b.getBoard()))
                .toList()
                .toObservable();
    }

    public BoardModel findBoardByCode(String boardCode) {

        for (BoardModel board : mApplicationSettings.getBoards()) {
            if (board.getId().equals(boardCode)) {
                return board;
            }
        }
        return null;
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

    public List<BoardModel> parseBoardsListResponse(Response response) throws IOException {
        ArrayList<BoardModel> models = new ArrayList<>();

        if (!response.isSuccessful()) {
            Timber.e("Response was not successful");
            response.close();
            return models;
        }

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            Timber.e("responseBody is null");
            return models;
        }

        JsonNode result = mapper.readValue(responseBody.string(), JsonNode.class);
        response.close();

        Iterator iterator = result.getElements();

        while (iterator.hasNext()) {
            JsonNode node = (JsonNode) iterator.next();

            MakabaBoardInfo[] data = mapper.convertValue(node, MakabaBoardInfo[].class);

            models.addAll(Arrays.asList(MakabaModelsMapper.mapBoardModels(data)));
        }
        return models;
    }
}
