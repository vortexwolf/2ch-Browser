package com.vortexwolf.chan.interfaces;

import com.vortexwolf.chan.models.domain.PostInfo;
import com.vortexwolf.chan.models.domain.ThreadInfo;

public interface IPagesSerializationService {

    public void serializeThreads(String boardName, int pageNumber, ThreadInfo[] threads);

    public void serializePosts(String boardName, String threadNumber, PostInfo[] posts);

    public ThreadInfo[] deserializeThreads(String boardName, int pageNumber);

    public PostInfo[] deserializePosts(String boardName, String threadNumber);

}