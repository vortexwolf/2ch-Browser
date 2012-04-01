package com.vortexwolf.dvach.common.library;

import java.util.ArrayList;

import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import android.graphics.Bitmap;
import android.httpimage.HttpImageManager;
import android.httpimage.HttpImageManager.LoadRequest;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

public class BitmapManager implements IBitmapManager {
	private final HttpImageManager mImageManager;
    
	public BitmapManager(HttpImageManager imageManager) {
		this.mImageManager = imageManager;
	}

	@Override
	public boolean isCached(String uriString) {
		return mImageManager.isCached(uriString);
	}

	@Override
	public void fetchBitmapOnThread(final String uriString, final ImageView imageView, final View indeterminateProgressBar, final int errorImageId) {

		Uri uri = Uri.parse(uriString);
		imageView.setTag(uri);

		LoadRequest r = new LoadRequest(uri, null, new HttpImageManager.OnLoadResponseListener() {
			@Override
			public void beforeLoad(final LoadRequest r) {
            	if(imageView.getTag() == r.getUri()){
            		AppearanceUtils.showImageProgressBar(indeterminateProgressBar, imageView);
            	}	
			}
			
			@Override
			public void onLoadResponse(final LoadRequest r, final Bitmap data) {
            	if(imageView.getTag() == r.getUri()){
            		imageView.setImageBitmap(data);
            		AppearanceUtils.hideImageProgressBar(indeterminateProgressBar, imageView);
            	}
			}
			
			@Override
			public void onLoadError(final LoadRequest r, Throwable e) {
            	if(imageView.getTag() == r.getUri()){
            		imageView.setImageResource(errorImageId);
            		AppearanceUtils.hideImageProgressBar(indeterminateProgressBar, imageView);
            	}
			}
		});
			
		// Запрос
		Bitmap b = mImageManager.loadImage(r);
		// Если удалось взять из кэша, отображаем сразу
		if(b != null){
			imageView.setImageBitmap(b);
		}
	}
}
