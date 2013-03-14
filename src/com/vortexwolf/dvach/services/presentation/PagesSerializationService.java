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
        File file = this.getFilePath(boardName + "-" + pageNumber);

        this.mSerializationService.serializeObject(file, threads);
    }

    @Override
    public void serializePosts(String threadNumber, PostInfo[] posts) {
        File file = this.getFilePath(threadNumber);

        this.mSerializationService.serializeObject(file, posts);
    }

    @Override
    public ThreadInfo[] deserializeThreads(String boardName, int pageNumber) {
        File file = this.getFilePath(boardName + "-" + pageNumber);

        ThreadInfo[] threads = (ThreadInfo[]) this.mSerializationService.deserializeObject(file);

        return threads;
    }

    @Override
    public PostInfo[] deserializePosts(String threadNumber) {
        File file = this.getFilePath(threadNumber);

        PostInfo[] posts = (PostInfo[]) this.mSerializationService.deserializeObject(file);

        return posts;
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
