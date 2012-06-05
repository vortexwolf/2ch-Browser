package com.vortexwolf.dvach.models.presentation;

import java.util.HashMap;
import android.content.res.Resources.Theme;

import com.vortexwolf.dvach.interfaces.IURLSpanClickListener;
import com.vortexwolf.dvach.models.domain.PostInfo;

public class PostsViewModel {

	private final HashMap<String, PostItemViewModel> mViewModels = new HashMap<String, PostItemViewModel>();
	private String mLastPostNumber = null;

	
	// Обновляет ссылки у других постов и добавляет модель в список
	private void ProcessPostItem(PostItemViewModel viewModel){
		for(String refPostNumber : viewModel.getRefersTo()){
			PostItemViewModel refModel = mViewModels.get(refPostNumber);
			if(refModel != null){
				refModel.addReferenceFrom(viewModel.getNumber());
			}
		}
				
		this.mViewModels.put(viewModel.getNumber(), viewModel);
	}
	
	public PostItemViewModel getModel(String postNumber){
		return mViewModels.get(postNumber);
	}
	
	/**
	 * Создает view model на основе модели
	 * @param item Модель какого-нибудь сообщения в треде
	 * @param theme Текущая тема приложения
	 * @param listener Обработчик события нажатия на ссылку в посте
	 * @return Созданная view model
	 */
	public PostItemViewModel createModel(PostInfo item, Theme theme, IURLSpanClickListener listener){
		PostItemViewModel viewModel = new PostItemViewModel(mViewModels.size(), item, theme, listener);
		this.mLastPostNumber = viewModel.getNumber();
		
		ProcessPostItem(viewModel);
		
		return viewModel;
	}

	/**
	 * Возвращает номер последнего сообщения в треде
	 */
	public String getLastPostNumber() {
		return mLastPostNumber;
	}
}
