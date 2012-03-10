package com.vortexwolf.dvach.presentation.models;

import java.util.ArrayList;
import java.util.HashMap;
import android.content.res.Resources.Theme;

import com.vortexwolf.dvach.api.entities.PostInfo;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.interfaces.IURLSpanClickListener;

public class PostsViewModel {

	private final HashMap<String, PostItemViewModel> mViewModels = new HashMap<String, PostItemViewModel>();
	private final HashMap<String, ArrayList<PostItemViewModel>> mSamePersons = new HashMap<String, ArrayList<PostItemViewModel>>();
	private final HashMap<String, Integer> mSamePersonIndices = new HashMap<String, Integer>();
	private int mLastSamePersonIndex = 0;
	private String mLastPostNumber = null;

	
	// Обновляет ссылки у других постов и добавляет модель в список
	private void ProcessPostItem(PostItemViewModel viewModel){
		for(String refPostNumber : viewModel.getRefersTo()){
			PostItemViewModel refModel = mViewModels.get(refPostNumber);
			if(refModel != null){
				refModel.addReferenceFrom(viewModel.getNumber());
			}
		}
		
		// детектируем семенов
		String postId = viewModel.getPostId();
		if(!StringUtils.isEmpty(postId) && !Constants.NAME_HEAVEN.equals(postId)){
			ArrayList<PostItemViewModel> samePersonPosts = this.mSamePersons.get(postId);
			if(samePersonPosts == null){
				samePersonPosts = new ArrayList<PostItemViewModel>();
				this.mSamePersons.put(postId, samePersonPosts);
			}
			
			for(PostItemViewModel samePersonModel : samePersonPosts){
				samePersonModel.addSamePersonReference(viewModel.getNumber());
				viewModel.addSamePersonReference(samePersonModel.getNumber());
			}
			
			// Если текущий пост второй - то считаем пользователя семеном
			if(samePersonPosts.size() == 1){
				mLastSamePersonIndex++;
				mSamePersonIndices.put(postId, mLastSamePersonIndex);
			}
				
			samePersonPosts.add(viewModel);
		}
		
		this.mViewModels.put(viewModel.getNumber(), viewModel);
	}
	
	public Integer getSamePersonIndex(String postId){
		return mSamePersonIndices.get(postId);
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
