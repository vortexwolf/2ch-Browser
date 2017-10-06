package ua.in.quireg.chan.services.presentation;

import ua.in.quireg.chan.models.domain.PostModel;
import ua.in.quireg.chan.models.domain.ThreadModel;
import ua.in.quireg.chan.services.CacheDirectoryManager;
import ua.in.quireg.chan.services.SerializationService;

import java.io.File;

public class PagesSerializationService {

    private static final String sExtension = ".2ch";

    private final CacheDirectoryManager mCacheManager;
    private final SerializationService mSerializationService;

    public PagesSerializationService(CacheDirectoryManager cacheManager, SerializationService serializationService) {
        this.mCacheManager = cacheManager;
        this.mSerializationService = serializationService;
    }

    public void serializeThreads(String website, String boardName, int pageNumber, ThreadModel[] threads) {
        File file = this.getBoardFilePath(website, boardName, pageNumber);

        this.mSerializationService.serializeObject(file, threads);
    }

    public void serializePosts(String website, String boardName, String threadNumber, PostModel[] posts) {
        File file = this.getThreadFilePath(website, boardName, threadNumber);

        this.mSerializationService.serializeObject(file, posts);
    }

    public ThreadModel[] deserializeThreads(String website, String boardName, int pageNumber) {
        File file = this.getBoardFilePath(website, boardName, pageNumber);

        ThreadModel[] threads = (ThreadModel[]) this.mSerializationService.deserializeObject(file);

        return threads;
    }
    
    public PostModel[] deserializePosts(String website, String boardName, String threadNumber) {
        File file = this.getThreadFilePath(website, boardName, threadNumber);

        PostModel[] posts = (PostModel[]) this.mSerializationService.deserializeObject(file);

        return posts;
    }

    private File getBoardFilePath(String website, String boardName, int pageNumber) {
        return this.getFilePath(website + "_" + boardName + "_page" + pageNumber);
    }

    private File getThreadFilePath(String website, String boardName, String threadNumber) {
        return this.getFilePath(website + "_" + boardName + "_thread" + threadNumber);
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
