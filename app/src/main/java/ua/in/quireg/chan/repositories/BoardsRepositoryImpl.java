package ua.in.quireg.chan.repositories;

import android.content.Context;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import timber.log.Timber;
import ua.in.quireg.chan.boards.makaba.MakabaModelsMapper;
import ua.in.quireg.chan.boards.makaba.models.MakabaBoardInfo;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.common.utils.IoUtils;
import ua.in.quireg.chan.domain.ApiReader;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.services.CacheDirectoryManager;
import ua.in.quireg.chan.services.SerializationService;

/**
 * Created by Arcturus Mengsk on 11/28/2017, 1:31 PM.
 * 2ch-Browser
 */

public class BoardsRepositoryImpl implements BoardsRepository {

    private Context mContext;
    private ApiReader mApiReader;
    private OkHttpClient mOkHttpClient;
    private CacheDirectoryManager mCacheManager;

    private static final String MAKABA_BOARDS = "MAKABA_BOARDS";

    private List<BoardModel> mBoards = null;

    public BoardsRepositoryImpl(ApiReader apiReader, OkHttpClient okHttpClient, Context context) {
        this.mApiReader = apiReader;
        this.mOkHttpClient = okHttpClient;
        this.mContext = context;
        this.mCacheManager = Factory.getContainer().resolve(CacheDirectoryManager.class);
    }

    //Get list from local storage
    public Observable<List<BoardModel>> getLocalBoards() {

        return getSavedBoards().switchIfEmpty(getDefaultBoards());
    }

    //Get list from server
    public Observable<List<BoardModel>> getRemoteBoards() {

        Request request = new Request.Builder()
                .url(Websites.getDefault().getUrlBuilder().getBoardsUrl())
                .build();

        return Observable.fromCallable(() -> mOkHttpClient.newCall(request))
                .map(Call::execute)
                .map((r) -> {

                    List<BoardModel> newBoards = mApiReader.readBoardsListResponse(r);

                    if (newBoards != null && !newBoards.isEmpty()) {
                        saveBoards(newBoards);
                    }

                    return newBoards;
                });
    }

    //Get boards list from boards.json file located in assets folder.
    private Observable<List<BoardModel>> getDefaultBoards() {

        return Observable.create(e -> {

            ArrayList<BoardModel> models = new ArrayList<>();

            try {
                String boardsJSON = IoUtils.convertStreamToString(mContext.getAssets().open("boards.json"));

                ObjectMapper mapper = new ObjectMapper()
                        .configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                JsonNode result = mapper.readValue(boardsJSON, JsonNode.class);

                Iterator iterator = result.getElements();

                while (iterator.hasNext()) {
                    JsonNode node = (JsonNode) iterator.next();

                    MakabaBoardInfo[] data = mapper.convertValue(node, MakabaBoardInfo[].class);

                    models.addAll(Arrays.asList(MakabaModelsMapper.mapBoardModels(data)));
                }

            } catch (IOException er) {
                e.onError(er);
            }

            Timber.w("Using pre-defined boards list");
            e.onNext(models);
            e.onComplete();
        });


    }

    //Boards list is saved to persistent storage upon each fetch from server.
    //This method returns last fetched list.
    @SuppressWarnings("unchecked")
    private Observable<List<BoardModel>> getSavedBoards() {

        return Observable.create(e -> {

            //try to return in-memory list
            if (mBoards != null && !mBoards.isEmpty()) {
                e.onNext(mBoards);
                e.onComplete();
                return;
            }

            //try to deserialize previously saved
            File file = new File(mCacheManager.getCurrentCacheDirectory(), MAKABA_BOARDS);

            mBoards = (ArrayList<BoardModel>) SerializationService.deserializeFromFile(file);

            if (mBoards != null) {
                e.onNext(mBoards);
                e.onComplete();
                return;
            }

            Timber.w("No saved boards available");
            e.onComplete();
        });

    }

    //Save list to persistent storage.
    private void saveBoards(List<BoardModel> boards) {

        mBoards = boards;

        File file = new File(mCacheManager.getCurrentCacheDirectory(), MAKABA_BOARDS);

        SerializationService.serializeToFile(file, (ArrayList) boards);

    }

}
