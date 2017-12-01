package ua.in.quireg.chan.mvp.models;

import android.content.Context;

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
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.boards.makaba.MakabaModelsMapper;
import ua.in.quireg.chan.boards.makaba.models.MakabaBoardInfo;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.db.FavoritesEntity;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.settings.ApplicationSettings;

/**
 * Created by Arcturus Mengsk on 11/21/2017, 5:06 PM.
 * 2ch-Browser
 */

public class BoardsListModel {

    @Inject protected Context mContext;
    @Inject protected OkHttpClient mOkHttpClient;
    @Inject protected ApplicationSettings mSettings;
    @Inject protected Observable<List<BoardModel>> mLocalBoardsProvider;
    @Inject protected FavoritesDataSource mFavoritesDatasource;

    private List<String> mVisibleBoards;

    public BoardsListModel() {
        MainApplication.getAppComponent().inject(this);
        mVisibleBoards = Arrays.asList(mContext.getResources().getStringArray(R.array.allowed_boards));
    }

    public Observable<List<BoardModel>> getBoards(boolean localOnly) {

        if (localOnly) {
            return getVisibleLocalBoards();
        } else {
            return getLocalBoards()
                    .zipWith(getRemoteBoards(), (local, remote) -> {

                        if (remote == null || remote.isEmpty()) {
                            throw new IOException("Received empty boards list!");

                        } else if (areEqual(local, remote)) {
                            Timber.d("Boards list is up to date");
                            return new ArrayList<BoardModel>();
                        } else {
                            Timber.d("Boards list has expired, updating...");

                            //Update local storage and emit new boards list
                            mSettings.setBoards(remote);
                            return remote;
                        }
                    })
                    //Firstly emit local boards
                    .startWith(getVisibleLocalBoards())
                    //Complete the sequence if boards are up to date or emit updated boards.
                    .flatMap(boardModels -> {
                        if (boardModels.isEmpty()) {
                            return Observable.empty();
                        } else {
                            return Observable.just(boardModels)
                                    .map(this::removeHiddenBoards);
                        }
                    });

        }
    }

    public Observable<List<BoardModel>> getFavBoards() {

        List<FavoritesEntity> favoriteBoards = mFavoritesDatasource.getFavoriteBoards();

        return Observable.fromIterable(favoriteBoards)
                .subscribeOn(Schedulers.io())
                .map((b) -> findBoardByCode(b.getBoard()))
                .toList()
                .toObservable();
    }

    public BoardModel findBoardByCode(String boardCode) {

        for (BoardModel board : mSettings.getBoards()) {
            if (board.getId().equals(boardCode)) {
                return board;
            }
        }
        return null;
    }

    public boolean isFavoriteBoard(BoardModel boardModel) {
        return !mFavoritesDatasource.hasFavorites(Websites.getDefault().name(), boardModel.getId(), null);
    }

    public void addToFavorites(BoardModel boardModel) {
        mFavoritesDatasource.addToFavorites(Websites.getDefault().name(), boardModel.getId(), null, null);
    }

    public void removeFromFavorites(BoardModel boardModel) {
        mFavoritesDatasource.removeFromFavorites(Websites.getDefault().name(), boardModel.getId(), null);
    }

    private Observable<List<BoardModel>> getVisibleLocalBoards() {
        return getLocalBoards().map(this::removeHiddenBoards);
    }

    private Observable<List<BoardModel>> getLocalBoards() {
        return mLocalBoardsProvider.subscribeOn(Schedulers.io());
    }

    private Observable<List<BoardModel>> getRemoteBoards() {

        Request request = new Request.Builder()
                .url(Websites.getDefault().getUrlBuilder().getBoardsUrl())
                .build();

        return Observable.fromCallable(() -> mOkHttpClient.newCall(request))
                .subscribeOn(Schedulers.io())
                .map(Call::execute)
                .map(this::parseResponse);
    }

    private List<BoardModel> parseResponse(Response response) throws IOException {
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
        return mSettings.isDisplayAllBoards() || mVisibleBoards.contains(board.getId());
    }
}
