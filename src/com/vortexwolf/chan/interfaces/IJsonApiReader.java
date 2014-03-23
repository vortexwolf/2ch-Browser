package com.vortexwolf.chan.interfaces;

import com.vortexwolf.chan.exceptions.JsonApiReaderException;
import com.vortexwolf.chan.models.domain.FoundPostsList;
import com.vortexwolf.chan.models.domain.PostsList;
import com.vortexwolf.chan.models.domain.ThreadsList;

public interface IJsonApiReader {
    ThreadsList readThreadsList(String boardName, int page, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException;

    PostsList readPostsList(String boardName, String threadNumber, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException;

    FoundPostsList searchPostsList(String boardName, String searchQuery, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException;
}
