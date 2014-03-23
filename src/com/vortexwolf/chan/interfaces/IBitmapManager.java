package com.vortexwolf.chan.interfaces;

import android.view.View;
import android.widget.ImageView;

public interface IBitmapManager {
    public abstract boolean isCached(String urlString);

    void fetchBitmapOnThread(String uriString, ImageView imageView, View indeterminateProgressBar, Integer errorImageId);
}
