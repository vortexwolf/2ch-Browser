package com.vortexwolf.dvach.interfaces;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;

public interface IBitmapManager {
	
	/** Загружает изображение из интернета в контрол и показывает индикатор загрузки
	 * @param uriString Ссылка на смищную картинку
	 * @param imageView Контрол, где все это будет отображаться
	 * @param indeterminateProgressBar Вращающийся индикатор загрузки
	 * @param act Это чтобы в ui-потоке запускать
	 * @param errorImageId что отображать, если ничего не получилось загрузить
	 */
	void fetchBitmapOnThread(final String uriString, final ImageView imageView, final View indeterminateProgressBar, final int errorImageId);

	public abstract boolean isCached(String urlString);
}
