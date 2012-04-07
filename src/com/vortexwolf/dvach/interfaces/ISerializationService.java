package com.vortexwolf.dvach.interfaces;

import com.vortexwolf.dvach.api.entities.PostInfo;
import com.vortexwolf.dvach.api.entities.ThreadInfo;

public interface ISerializationService {

	public void serializeThreads(String boardName, int pageNumber,
			ThreadInfo[] threads);

	public void serializePosts(String threadNumber, PostInfo[] posts);

	public ThreadInfo[] deserializeThreads(String boardName, int pageNumber);

	public PostInfo[] deserializePosts(String threadNumber);

}