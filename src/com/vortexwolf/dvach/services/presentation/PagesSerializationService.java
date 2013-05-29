package com.vortexwolf.dvach.services.presentation;

import java.io.File;

import com.vortexwolf.dvach.interfaces.ICacheDirectoryManager;
import com.vortexwolf.dvach.interfaces.IPagesSerializationService;
import com.vortexwolf.dvach.models.domain.PostInfo;
import com.vortexwolf.dvach.models.domain.ThreadInfo;
import com.vortexwolf.dvach.services.SerializationService;

public class PagesSerializationService implements IPagesSerializationService {

    private static final String sExtension = ".2ch";

    private final ICacheDirectoryManager mCacheManager;
    private final SerializationService mSerializationService;

    public PagesSerializationService(ICacheDirectoryManager cacheManager, SerializationService serializationService) {
        this.mCacheManager = cacheManager;
        this.mSerializationService = serializationService;
    }

    @Override
    public void serializeThreads(String boardName, int pageNumber, ThreadInfo[] threads) {
        File file = this.getBoardFilePath(boardName, pageNumber);

        this.mSerializationService.serializeObject(file, threads);
    }

    @Override
    public void serializePosts(String boardName, String threadNumber, PostInfo[] posts) {
        File file = this.getThreadFilePath(boardName, threadNumber);

        this.mSerializationService.serializeObject(file, posts);
    }

    @Override
    public ThreadInfo[] deserializeThreads(String boardName, int pageNumber) {
        File file = this.getBoardFilePath(boardName, pageNumber);

        ThreadInfo[] threads = (ThreadInfo[]) this.mSerializationService.deserializeObject(file);

        return threads;
    }

    @Override
    public PostInfo[] deserializePosts(String boardName, String threadNumber) {
        File file = this.getThreadFilePath(boardName, threadNumber);

        PostInfo[] posts = (PostInfo[]) this.mSerializationService.deserializeObject(file);

        return posts;
    }
    
    private File getBoardFilePath(String boardName, int pageNumber){
        return this.getFilePath(boardName + "_page" + pageNumber);
    }
    
    private File getThreadFilePath(String boardName, String threadNumber){
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
