package com.vortexwolf.dvach.models.presentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.res.Resources.Theme;

import com.vortexwolf.dvach.interfaces.IURLSpanClickListener;
import com.vortexwolf.dvach.models.domain.PostInfo;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class PostsViewModel {

    private final HashMap<String, PostItemViewModel> mViewModels = new HashMap<String, PostItemViewModel>();
    private String mLastPostNumber = null;

    // Обновляет ссылки у других постов и добавляет модель в список
    private void processPostItem(PostItemViewModel viewModel) {
        for (String refPostNumber : viewModel.getRefersTo()) {
            PostItemViewModel refModel = this.mViewModels.get(refPostNumber);
            if (refModel != null) {
                refModel.addReferenceFrom(viewModel.getNumber());
            }
        }

        this.mViewModels.put(viewModel.getNumber(), viewModel);
    }

    public PostItemViewModel getModel(String postNumber) {
        return this.mViewModels.get(postNumber);
    }

    private PostItemViewModel createModel(PostInfo item, Theme theme, ApplicationSettings settings, IURLSpanClickListener listener, DvachUriBuilder uriBuilder) {
        PostItemViewModel viewModel = new PostItemViewModel(this.mViewModels.size(), item, theme, settings, listener, uriBuilder);
        this.mLastPostNumber = viewModel.getNumber();

        this.processPostItem(viewModel);

        return viewModel;
    }
    
    public List<PostItemViewModel> addModels(List<PostInfo> items, Theme theme, ApplicationSettings settings, IURLSpanClickListener listener, DvachUriBuilder uriBuilder) {
        List<PostItemViewModel> result = new ArrayList<PostItemViewModel>();
        for (PostInfo item : items) {
            PostItemViewModel model = this.createModel(item, theme, settings, listener, uriBuilder);
            result.add(model);
        }
        
        return result;
    }

    /**
     * Возвращает номер последнего сообщения в треде
     */
    public String getLastPostNumber() {
        return this.mLastPostNumber;
    }
}
