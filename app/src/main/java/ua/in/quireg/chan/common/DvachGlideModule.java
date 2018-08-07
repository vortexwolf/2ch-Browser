package ua.in.quireg.chan.common;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;

import javax.inject.Inject;

import okhttp3.OkHttpClient;

/**
 * Created by Arcturus Mengsk on 07.08.18.
 * 2ch-Browser
 */

@GlideModule
public final class DvachGlideModule extends AppGlideModule {

    @Inject
    OkHttpClient mOkHttpClient;

    public DvachGlideModule() {
        super();
        MainApplication.getAppComponent().inject(this);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, Registry registry) {
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(mOkHttpClient);
        registry.replace(GlideUrl.class, InputStream.class, factory);
    }

}
