package com.vortexwolf.chan.models.presentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.res.Resources;
import android.content.res.Resources.Theme;

import com.vortexwolf.chan.interfaces.IURLSpanClickListener;
import com.vortexwolf.chan.interfaces.IWebsite;
import com.vortexwolf.chan.models.domain.PostModel;

public class PostsViewModel {

    private final HashMap<String, PostItemViewModel> mViewModels = new HashMap<String, PostItemViewModel>();
    private int mLastPostNumber = 0;

    private final IWebsite mWebsite;
    private final String mBoardName;
    private final String mThreadNumber;

    public PostsViewModel(IWebsite website, String boardName, String threadNumber) {
        this.mWebsite = website;
        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;
    }

    // Обновляет ссылки у других постов и добавляет модель в список
    private void processReferences(PostItemViewModel viewModel) {
        for (String refPostNumber : viewModel.getRefersTo()) {
            PostItemViewModel refModel = this.mViewModels.get(refPostNumber);
            if (refModel != null) {
                refModel.addReferenceFrom(viewModel.getNumber());
            }
        }
    }

    public PostItemViewModel getModel(String postNumber) {
        return this.mViewModels.get(postNumber);
    }

    private PostItemViewModel addModel(PostModel item, Theme theme, IURLSpanClickListener listener) {
        PostItemViewModel viewModel = new PostItemViewModel(this.mWebsite, this.mBoardName, this.mThreadNumber, this.mViewModels.size(), item, theme, listener);
        this.mViewModels.put(viewModel.getNumber(), viewModel);

        this.mLastPostNumber = Math.max(Integer.parseInt(viewModel.getNumber()), this.mLastPostNumber);

        this.processReferences(viewModel);

        return viewModel;
    }

    public List<PostItemViewModel> addModels(List<PostModel> items, Theme theme, IURLSpanClickListener listener, Resources resources) {
        List<PostItemViewModel> result = new ArrayList<PostItemViewModel>();
        for (PostModel item : items) {
            PostItemViewModel model = this.addModel(item, theme, listener);
            result.add(model);
        }

        return result;
    }

    /**
     * Возвращает номер последнего сообщения в треде
     */
    public int getLastPostNumber() {
        return this.mLastPostNumber;
    }
}
