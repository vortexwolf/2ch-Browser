package ua.in.quireg.chan.repositories;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.domain.ApiReader;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.settings.ApplicationSettings;

/**
 * Created by Arcturus Mengsk on 11/28/2017, 1:31 PM.
 * 2ch-Browser
 */

public class BoardsRepository {

    private ApplicationSettings mApplicationSettings;
    private ApiReader mApiReader;
    private OkHttpClient mOkHttpClient;

    public BoardsRepository(ApplicationSettings mApplicationSettings,
                            ApiReader apiReader,
                            OkHttpClient okHttpClient) {
        this.mApplicationSettings = mApplicationSettings;
        this.mApiReader = apiReader;
        this.mOkHttpClient = okHttpClient;
    }

    public Observable<List<BoardModel>> getLocalBoards() {
        return Observable.just(mApplicationSettings.getBoards());
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
        mApplicationSettings.setBoards(boards);
    }

}
