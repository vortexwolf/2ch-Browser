package ua.in.quireg.chan.models;

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
import io.reactivex.functions.BiFunction;
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
import ua.in.quireg.chan.common.utils.GeneralUtils;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.settings.ApplicationSettings;

/**
 * Created by Arcturus Mengsk on 11/21/2017, 5:06 PM.
 * 2ch-Browser
 */

@SuppressWarnings("WeakerAccess")
public class BoardsListModel {

    @Inject protected Context mContext;
    @Inject protected OkHttpClient mOkHttpClient;
    @Inject protected ApplicationSettings mSettings;
    @Inject protected Observable<List<BoardModel>> mLocalBoardsListObservable;

    protected List<String> visibleBoards;

    public BoardsListModel() {
        MainApplication.getComponent().inject(this);
        visibleBoards = Arrays.asList(mContext.getResources().getStringArray(R.array.allowed_boards));
    }

    public Observable<List<BoardModel>> getBoardsList() {

        Request request = new Request.Builder()
                .url(Websites.getDefault().getUrlBuilder().getBoardsUrl())
                .build();

        return Observable.fromCallable(() -> mOkHttpClient.newCall(request))
                .subscribeOn(Schedulers.io())
                .map(Call::execute)
                .flatMap(response ->
                        Observable.just(parseBoardsResponse(response))
                                .zipWith(mLocalBoardsListObservable, (remoteBoardModels, localBoardModels) -> {

                                    Timber.d(Thread.currentThread().getName());

                                    if (remoteBoardModels == null || remoteBoardModels.isEmpty()) {
                                        Timber.e("Received empty boards list!");
                                        return localBoardModels;
                                    } else if (GeneralUtils.equalLists(localBoardModels, remoteBoardModels)) {
                                        Timber.d("Boards list has not been modified since last check");
                                        return remoteBoardModels;
                                    } else {
                                        Timber.d("Boards list has been modified, updating...");
                                        mSettings.setBoards((ArrayList<BoardModel>) remoteBoardModels);
                                        return remoteBoardModels;
                                    }
                                })
                                .flatMap(Observable::fromIterable)
                                .filter(this::isBoardVisible)
                                .toList()
                                .toObservable()
                );

    }

    public BoardModel findBoardByCode(String id) {
        for (BoardModel board : mSettings.getBoards()) {
            if (board.getId().equals(id)) {
                return board;
            }
        }
        return null;
    }

    private List<BoardModel> parseBoardsResponse(Response response) throws IOException {
        Timber.v("parseBoardsResponse()");
        Timber.d(Thread.currentThread().getName());

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

    private boolean isBoardVisible(BoardModel board) {
        return !(!visibleBoards.contains(board.getId()) && !mSettings.isDisplayAllBoards());
    }


}
