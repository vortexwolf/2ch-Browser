package com.vortexwolf.dvach.interfaces;

import android.app.Activity;

import com.vortexwolf.dvach.exceptions.JsonApiReaderException;
import com.vortexwolf.dvach.models.domain.BoardSettings;
import com.vortexwolf.dvach.models.domain.CaptchaEntity;
import com.vortexwolf.dvach.models.domain.PostsList;
import com.vortexwolf.dvach.models.domain.ThreadsList;

public interface IJsonApiReader {
	ThreadsList readThreadsList(String boardName, int page, IProgressChangeListener listener, ICancellable task, Activity activity) throws JsonApiReaderException;
	PostsList readPostsList(String boardName, String threadNumber, String from, IProgressChangeListener listener, ICancellable task) throws JsonApiReaderException;
}
