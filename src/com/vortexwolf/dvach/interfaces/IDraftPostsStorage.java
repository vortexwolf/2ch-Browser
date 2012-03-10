package com.vortexwolf.dvach.interfaces;

import com.vortexwolf.dvach.presentation.models.DraftPostModel;

public interface IDraftPostsStorage {

	public void saveDraft(String boardName, String threadNumber, DraftPostModel draft);

	public DraftPostModel getDraft(String boardName, String threadNumber);

	public void clearDraft(String boardName, String threadNumber);

}