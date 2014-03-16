package com.vortexwolf.dvach.services;

import com.vortexwolf.chan.R;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.httpimage.HttpImageManager;
import android.httpimage.HttpImageManager.LoadRequest;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.interfaces.IBitmapManager;

public class BitmapManager implements IBitmapManager {
    private final HttpImageManager mImageManager;

    public BitmapManager(HttpImageManager imageManager) {
        this.mImageManager = imageManager;
    }

    @Override
    public boolean isCached(String uriString) {
        return this.mImageManager.isCached(uriString);
    }

    @Override
    public void fetchBitmapOnThread(final String uriString, final ImageView imageView, final View indeterminateProgressBar, final Integer errorImageId) {

        Uri uri = Uri.parse(uriString);
        /*if (imageView.getTag() == uri) {
            return;
        }*/
        
        imageView.setTag(uri);

        LoadRequest r = new LoadRequest(uri, new HttpImageManager.OnLoadResponseListener() {
            @Override
            public void beforeLoad(final LoadRequest r) {
                if (imageView.getTag() == r.getUri()) {
                    imageView.setImageResource(android.R.color.transparent);
                    AppearanceUtils.showImageProgressBar(indeterminateProgressBar, imageView);
                }
            }

            @Override
            public void onLoadResponse(final LoadRequest r, final Bitmap data) {
                if (imageView.getTag() == r.getUri()) {
                    imageView.setImageBitmap(data);
                    AppearanceUtils.hideImageProgressBar(indeterminateProgressBar, imageView);
                }
            }

            @Override
            public void onLoadError(final LoadRequest r, Throwable e) {
                if (imageView.getTag() == r.getUri()) {
                    if (errorImageId != null) {
                        imageView.setImageResource(errorImageId.intValue());
                    }
                    AppearanceUtils.hideImageProgressBar(indeterminateProgressBar, imageView);
                }
            }
        });

        // Запрос
        Bitmap b = this.mImageManager.loadImage(r);
        // Если удалось взять из кэша, отображаем сразу
        if (b != null) {
            imageView.setImageBitmap(b);
        }
    }
}
