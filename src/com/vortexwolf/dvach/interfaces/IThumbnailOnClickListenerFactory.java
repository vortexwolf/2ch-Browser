package com.vortexwolf.dvach.interfaces;

import com.vortexwolf.dvach.presentation.models.AttachmentInfo;
import com.vortexwolf.dvach.settings.ApplicationSettings;

import android.content.Context;
import android.view.View.OnClickListener;

public interface IThumbnailOnClickListenerFactory {
	
	/** Обработчик нажатия на картинку с ссылкой, открывает ее в браузере, например
	 */
	OnClickListener getOnClickListener(final AttachmentInfo attachment, final Context context, final ApplicationSettings settings);
	
	/**
	 * Явный вызов функции
	 */
	void raiseClick(final AttachmentInfo attachment, final Context context, final ApplicationSettings settings);
}
