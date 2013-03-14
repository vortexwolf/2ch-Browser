package com.vortexwolf.dvach.interfaces;

import com.vortexwolf.dvach.exceptions.JsonApiReaderException;
import com.vortexwolf.dvach.models.domain.PostsList;
import com.vortexwolf.dvach.models.domain.ThreadsList;

public interface IJsonApiReader {
    ThreadsList readThreadsList(String boardName, int page, boolean checkModified, IProgressChangeListener listener, ICancelled task) throws JsonApiReaderException;

    PostsList readPostsList(String boardName, String threadNumber, boolean checkModified, IProgressChangeListener listener, ICancelled task) throws JsonApiReaderException;
}
