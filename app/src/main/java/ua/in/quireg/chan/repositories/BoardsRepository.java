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
import ua.in.quireg.chan.settings.ApplicationSettings;

/**
 * Created by Arcturus Mengsk on 11/28/2017, 1:31 PM.
 * 2ch-Browser
 */

public class BoardsRepository {

    private Context mContext;
    private ApplicationSettings mApplicationSettings;
    private ApiReader mApiReader;
    private OkHttpClient mOkHttpClient;
    private CacheDirectoryManager mCacheManager;

    private static final String MAKABA_BOARDS = "MAKABA_BOARDS";

    private List<BoardModel> mBoards = null;

    public BoardsRepository(ApplicationSettings mApplicationSettings,
                            ApiReader apiReader,
                            OkHttpClient okHttpClient,
                            Context context) {
        this.mApplicationSettings = mApplicationSettings;
        this.mApiReader = apiReader;
        this.mOkHttpClient = okHttpClient;
        this.mContext = context;
        this.mCacheManager = Factory.getContainer().resolve(CacheDirectoryManager.class);
    }

    public Observable<List<BoardModel>> getLocalBoards() {

        return Observable.zip(Observable.just(getSavedLocalBoards()), getDefaultLocalBoards(), (local, localDefault) -> {
            if(local == null || local.isEmpty()) {
                Timber.d("Returned boards list from assets");
                return localDefault;
            }
            Timber.d("Returned previously saved boards list");
            return local;
        });
    }

    public Observable<List<BoardModel>> getRemoteBoards() {

        Request request = new Request.Builder()
                .url(Websites.getDefault().getUrlBuilder().getBoardsUrl())
                .build();

        return Observable.fromCallable(() -> mOkHttpClient.newCall(request))
                .subscribeOn(Schedulers.io())
                .map(Call::execute)
                .map((r) -> mApiReader.readBoardsListResponse(r));
    }

    public void setLocalBoards(List<BoardModel> boards) {

        File file = new File(mCacheManager.getCurrentCacheDirectory(), MAKABA_BOARDS);

        SerializationService.serializeToFile(file, (ArrayList)boards);

    }

    private Observable<List<BoardModel>> getDefaultLocalBoards() {

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

        } catch (IOException e) {
            e.printStackTrace();
        }

        models.remove(2);

        return Observable.just(models);
    }

    @SuppressWarnings("unchecked")
    private List<BoardModel> getSavedLocalBoards() {

        //try to return in-memory list
        if (mBoards != null && !mBoards.isEmpty()) {
            return mBoards;
        }

        //try to deserialize previously saved
        File file = new File(mCacheManager.getCurrentCacheDirectory(), MAKABA_BOARDS);

        mBoards = (ArrayList<BoardModel>) SerializationService.deserializeFromFile(file);

        if (mBoards != null) {
            return mBoards;
        }

        //return new empty list
        return new ArrayList<>();
    }
}
