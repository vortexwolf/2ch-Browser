package com.vortexwolf.dvach.interfaces;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;

public interface IBitmapManager {
	
	/** Загружает изображение из интернета в контрол и показывает индикатор загрузки
	 * @param urlString Ссылка на смищную картинку
	 * @param imageView Контрол, где все это будет отображаться
	 * @param indeterminateProgressBar Вращающийся индикатор загрузки
	 * @param act Это чтобы в ui-потоке запускать
	 * @param errorImageId что отображать, если ничего не получилось загрузить
	 */
	void fetchBitmapOnThread(final String urlString, final ImageView imageView, final View indeterminateProgressBar, final Activity act, final int errorImageId);
	
	/** Чистит кэш, чтобы картинки не пожирали и без того ограниченную память телефона */
	void clearCache();

	public abstract boolean isCached(String urlString);
}
