package ua.in.quireg.chan.di;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import ua.in.quireg.chan.services.CacheDirectoryManager;
import ua.in.quireg.chan.services.presentation.PagesSerializationService;

/**
 * Created by Arcturus Mengsk on 17.05.18.
 * 2ch-Browser
 */

@Module
public class ServicesModule {

    @Provides
    @AppScope
    CacheDirectoryManager providesCacheDirectoryManager(Context context) {
        return new CacheDirectoryManager(context);
    }

    @Provides
    @AppScope
    PagesSerializationService providesPagesSerializationService(CacheDirectoryManager cacheDirectoryManager) {
        return new PagesSerializationService(cacheDirectoryManager);
    }
}
