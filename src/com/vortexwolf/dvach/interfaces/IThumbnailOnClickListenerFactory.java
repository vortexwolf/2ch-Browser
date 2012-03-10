package com.vortexwolf.dvach.interfaces;

import android.content.Context;
import android.view.View.OnClickListener;

public interface IThumbnailOnClickListenerFactory {
	
	/** Обработчик нажатия на картинку с ссылкой, открывает ее в браузере, например
	 * @param url
	 * @param context
	 * @return
	 */
	OnClickListener getOnClickListener(final String url, final Context context, final int imageSize);
	
	/**
	 * Явный вызов функции
	 * @param url
	 * @param context
	 */
	void raiseClick(final String url, final Context context, final int imageSize);
}
