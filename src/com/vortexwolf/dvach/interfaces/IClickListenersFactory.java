package com.vortexwolf.dvach.interfaces;

import android.content.Context;
import android.view.View.OnClickListener;

import com.vortexwolf.dvach.models.presentation.AttachmentInfo;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public interface IClickListenersFactory {

    /**
     * Обработчик нажатия на картинку с ссылкой, открывает ее в браузере,
     * например
     */
    OnClickListener getThumbnailOnClickListener(final AttachmentInfo attachment, final Context context, final ApplicationSettings settings);

    /**
     * Явный вызов функции
     */
    void raiseThumbnailClick(final AttachmentInfo attachment, final Context context, final ApplicationSettings settings);
}
