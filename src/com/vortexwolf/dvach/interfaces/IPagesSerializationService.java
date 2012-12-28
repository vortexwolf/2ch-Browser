package com.vortexwolf.dvach.interfaces;

import com.vortexwolf.dvach.models.domain.PostInfo;
import com.vortexwolf.dvach.models.domain.ThreadInfo;

public interface IPagesSerializationService {

    public void serializeThreads(String boardName, int pageNumber, ThreadInfo[] threads);

    public void serializePosts(String threadNumber, PostInfo[] posts);

    public ThreadInfo[] deserializeThreads(String boardName, int pageNumber);

    public PostInfo[] deserializePosts(String threadNumber);

}