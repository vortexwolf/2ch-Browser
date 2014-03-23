package com.vortexwolf.chan.models.presentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.res.Resources;
import android.content.res.Resources.Theme;

import com.vortexwolf.chan.interfaces.IURLSpanClickListener;
import com.vortexwolf.chan.models.domain.PostInfo;
import com.vortexwolf.chan.services.presentation.DvachUriBuilder;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class PostsViewModel {

    private final HashMap<String, PostItemViewModel> mViewModels = new HashMap<String, PostItemViewModel>();
    private String mLastPostNumber = null;

    // Обновляет ссылки у других постов и добавляет модель в список
    private void processReferences(PostItemViewModel viewModel) {
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

        this.processReferences(viewModel);
        
        return viewModel;
    }
    
    public List<PostItemViewModel> addModels(List<PostInfo> items, Theme theme, ApplicationSettings settings, IURLSpanClickListener listener, DvachUriBuilder uriBuilder, Resources resources, String boardName, String threadNumber) {
        List<PostItemViewModel> result = new ArrayList<PostItemViewModel>();
        for (PostInfo item : items) {
            PostItemViewModel model = this.createModel(item, theme, settings, listener, uriBuilder);
            result.add(model);
        }
        
        for (PostItemViewModel model : result) {
            model.getReferencesFromAsSpannableString(resources, boardName, threadNumber);
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
