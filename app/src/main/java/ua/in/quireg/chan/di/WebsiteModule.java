package ua.in.quireg.chan.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ua.in.quireg.chan.domain.ApiReader;
import ua.in.quireg.chan.domain.ApiReaderImpl;

/**
 * Created by Arcturus Mengsk on 12/5/2017, 6:39 AM.
 * 2ch-Browser
 */

@Module
public class WebsiteModule {

    @Provides
    @AppScope
    ApiReader providesApiReader() {
        return new ApiReaderImpl();
    }
}
