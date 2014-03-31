package com.vortexwolf.chan.services.presentation;

import java.io.File;

import com.vortexwolf.chan.boards.dvach.models.DvachPostInfo;
import com.vortexwolf.chan.boards.dvach.models.DvachThreadInfo;
import com.vortexwolf.chan.interfaces.ICacheDirectoryManager;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.models.domain.ThreadModel;
import com.vortexwolf.chan.services.SerializationService;

public class PagesSerializationService {

    private static final String sExtension = ".2ch";

    private final ICacheDirectoryManager mCacheManager;
    private final SerializationService mSerializationService;

    public PagesSerializationService(ICacheDirectoryManager cacheManager, SerializationService serializationService) {
        this.mCacheManager = cacheManager;
        this.mSerializationService = serializationService;
    }

    public void serializeThreads(String boardName, int pageNumber, ThreadModel[] threads) {
        File file = this.getBoardFilePath(boardName, pageNumber);

        this.mSerializationService.serializeObject(file, threads);
    }

    public void serializePosts(String boardName, String threadNumber, PostModel[] posts) {
        File file = this.getThreadFilePath(boardName, threadNumber);

        this.mSerializationService.serializeObject(file, posts);
    }

    public ThreadModel[] deserializeThreads(String boardName, int pageNumber) {
        File file = this.getBoardFilePath(boardName, pageNumber);

        ThreadModel[] threads = (ThreadModel[]) this.mSerializationService.deserializeObject(file);

        return threads;
    }
    
    public PostModel[] deserializePosts(String boardName, String threadNumber) {
        File file = this.getThreadFilePath(boardName, threadNumber);

        PostModel[] posts = (PostModel[]) this.mSerializationService.deserializeObject(file);

        return posts;
    }

    private File getBoardFilePath(String boardName, int pageNumber) {
        return this.getFilePath(boardName + "_page" + pageNumber);
    }

    private File getThreadFilePath(String boardName, String threadNumber) {
        return this.getFilePath(boardName + "_thread" + threadNumber);
    }

    private File getFilePath(String fileName) {
        File folder = this.mCacheManager.getPagesCacheDirectory();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, fileName + sExtension);

        return file;
    }
}
