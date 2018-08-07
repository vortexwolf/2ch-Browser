package ua.in.quireg.chan.repositories;

import android.content.Context;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import timber.log.Timber;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.db.DvachRoomDatabase;
import ua.in.quireg.chan.domain.ApiReader;
import ua.in.quireg.chan.models.domain.AttachmentModel;
import ua.in.quireg.chan.models.domain.BadgeModel;
import ua.in.quireg.chan.models.domain.PostModel;
import ua.in.quireg.chan.models.domain.ThreadModel;

public class ThreadsRepositoryImpl implements ThreadsRepository {

    Context mContext;
    ApiReader mApiReader;
    OkHttpClient mOkHttpClient;
    DvachRoomDatabase mDvachRoomDatabase;

    public ThreadsRepositoryImpl(ApiReader apiReader, OkHttpClient okHttpClient, Context context) {
        mDvachRoomDatabase = DvachRoomDatabase.getDatabase(context);
        mApiReader = apiReader;
        mOkHttpClient = okHttpClient;
        mContext = context;
    }

    @Override
    public Observable<List<ThreadModel>> getLocalThreads(String board) {
        return Observable.just(mDvachRoomDatabase.makabaDao().getThreadModelsForBoard(board));
    }

    @Override
    public Observable<List<ThreadModel>> getRemoteThreads(String board, int page) {

        String fetchUrl = Websites.getDefault().getUrlBuilder().getPageUrlApi(board, page);

        Request request = new Request.Builder()
                .url(fetchUrl)
                .build();

        Timber.i("Fetching %s", fetchUrl);

        return Observable.fromCallable(() -> mOkHttpClient.newCall(request))
                .map(Call::execute)
                .map((r) -> {
                    List<ThreadModel> threadModels = mApiReader.readThreadsListResponse(r);

                    if (threadModels == null || threadModels.isEmpty()) {
                        Timber.e("Received empty response");
                    } else {
                        for (ThreadModel threadModel : threadModels) {
                            saveThreadModel(threadModel);

                            for (PostModel postModel : threadModel.getPosts()) {
                                if (postModel == null) {
                                    continue;
                                }
                                savePostModel(postModel);
                                saveBadgeModel(postModel.getBadge());

                                for (AttachmentModel attachmentModel : postModel.getAttachments()) {
                                    saveAttachmentModel(attachmentModel);
                                }
                            }
                        }
                    }

                    return threadModels;
                });
    }


    private void saveThreadModel(ThreadModel model) {
        if (model == null) return;
        mDvachRoomDatabase.makabaDao().insert(model);
    }

    private void savePostModel(PostModel model) {
        if (model == null) return;
        mDvachRoomDatabase.makabaDao().insert(model);
    }

    private void saveBadgeModel(BadgeModel model) {
        if (model == null) return;
        mDvachRoomDatabase.makabaDao().insert(model);
    }

    private void saveAttachmentModel(AttachmentModel model) {
        if (model == null) return;
        mDvachRoomDatabase.makabaDao().insert(model);
    }
}
