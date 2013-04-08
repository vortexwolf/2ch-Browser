package com.vortexwolf.dvach.common.utils;

import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.interfaces.IClickListenersFactory;
import com.vortexwolf.dvach.models.domain.IAttachmentEntity;
import com.vortexwolf.dvach.models.presentation.AttachmentInfo;
import com.vortexwolf.dvach.services.presentation.ClickListenersFactory;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class ThreadPostUtils {

    private static final IClickListenersFactory sThumbnailOnClickListenerFactory = new ClickListenersFactory();

    public static Date getDateFromTimestamp(long timeInMiliseconds, TimeZone timeZone) {
        long localOffset = TimeZone.getDefault().getOffset(timeInMiliseconds);
        Date gmtTime = new Date(timeInMiliseconds - localOffset);

        long timeZoneOffset = timeZone.getOffset(timeInMiliseconds);
        Date timeZoneTime = new Date(gmtTime.getTime() + timeZoneOffset);

        return timeZoneTime;
    }

    public static Date getMoscowDateFromTimestamp(long timeInMiliseconds) {
        return getDateFromTimestamp(timeInMiliseconds, TimeZone.getTimeZone("GMT+4"));
    }

    public static Date getLocalDateFromTimestamp(long timeInMiliseconds) {
        return getDateFromTimestamp(timeInMiliseconds, TimeZone.getDefault());
    }

    /**
     * Удаляет ссылки на другие сообщения из поста, если они расположены вначале
     * строки
     */
    public static String removeLinksFromComment(String comment) {
        String result = comment.replaceAll("(^|\n)(>>\\d+(\n|\\s)?)+", "$1");

        return result;
    }

    /** Проверяет, прикреплен ли к посту какой-либо файл */
    public static boolean hasAttachment(IAttachmentEntity item) {
        return !StringUtils.isEmpty(item.getImage()) || !StringUtils.isEmpty(item.getVideo());
    }

    public static void openAttachment(final AttachmentInfo attachment, final Context context, final ApplicationSettings settings) {
        if (attachment != null) {
            sThumbnailOnClickListenerFactory.raiseThumbnailClick(attachment, context, settings);
        }
    }

    /** Отображение комбобокса с политическими взглядами */
    public static boolean isPoliticsBoard(String boardName) {
        boolean hasPoliticsView = boardName.equals("po");

        return hasPoliticsView;
    }

    /** Будет отображать другим цветом посты после бамплимита */
    public static int getBumpLimitNumber(String boardName) {
        if (boardName.equals("vg")) {
            return 1000;
        }

        return 500;
    }

    /**
     * Разбирается с прикрепленным файлом для треда или поста; перенес сюда,
     * чтобы не повторять код
     * 
     * @param attachment
     *            Модель прикрепленного к треду или посту файла
     * @param imageView
     *            Место для картинки
     * @param indeterminateProgressBar
     *            Индикатор загрузки
     * @param bitmapManager
     *            Для загрузки картинок с интернета
     * @param thumbnailOnClickListenerFactory
     *            Для обработки нажатия по картинке
     * @param activity
     */
    public static void handleAttachmentImage(boolean isBusy, AttachmentInfo attachment, ImageView imageView, ProgressBar indeterminateProgressBar, View fullThumbnailView, IBitmapManager bitmapManager, ApplicationSettings settings, Context context) {

        if(indeterminateProgressBar != null) {
            indeterminateProgressBar.setVisibility(View.GONE);
        }
        
        imageView.setImageResource(android.R.color.transparent); // clear the
                                                                 // image
                                                                 // content

        // Ищем прикрепленный файл, в случае наличия добавляем его как ссылку
        if (attachment == null || attachment.isEmpty()) {
            imageView.setVisibility(View.GONE);
            fullThumbnailView.setVisibility(View.GONE);
        } else {
            fullThumbnailView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);

            // Обработчик события нажатия на картинку
            OnClickListener thumbnailOnClickListener = sThumbnailOnClickListenerFactory.getThumbnailOnClickListener(attachment, context, settings);
            if (thumbnailOnClickListener != null) {
                imageView.setOnClickListener(thumbnailOnClickListener);
                if(indeterminateProgressBar != null) {
                    indeterminateProgressBar.setOnClickListener(thumbnailOnClickListener);
                }
            }

            String thumbnailUrl = attachment.getThumbnailUrl();
            // Также добавляем уменьшенное изображение, нажатие на которое
            // открывает файл в полном размере
            if (thumbnailUrl != null) {
                // Ничего не загружаем, если так установлено в настройках
                if (settings.isLoadThumbnails() == false && !bitmapManager.isCached(thumbnailUrl)) {
                    imageView.setImageResource(R.drawable.empty_image);
                } else {
                    imageView.setTag(Uri.parse(thumbnailUrl));

                    if (!isBusy || bitmapManager.isCached(thumbnailUrl)) {
                        bitmapManager.fetchBitmapOnThread(thumbnailUrl, imageView, indeterminateProgressBar, R.drawable.error_image);
                    }
                }
            } else {
                // Иногда можно прикреплять файлы с типом mp3, swf и пр., у
                // которых thumbnail=null. Нужно нарисовать другую картинку в
                // таких случаях
                if (attachment.isFile()) {
                    imageView.setImageResource(attachment.getDefaultThumbnail());
                } else {
                    imageView.setImageResource(R.drawable.error_image);
                }
            }
        }
    }

    public static boolean isImageHandledWhenWasBusy(AttachmentInfo attachment, ApplicationSettings settings, IBitmapManager bitmapManager) {
        if (attachment == null || attachment.isEmpty()) {
            return true;
        }

        String thumbnailUrl = attachment.getThumbnailUrl();
        return thumbnailUrl == null || !settings.isLoadThumbnails() || bitmapManager.isCached(thumbnailUrl);
    }

    public static void handleAttachmentDescription(AttachmentInfo attachment, Resources res, TextView attachmentInfoView) {
        String attachmentInfo;
        if (attachment == null || attachment.isEmpty()) {
            attachmentInfo = null;
        } else {
            attachmentInfo = attachment.getDescription(res.getString(R.string.data_file_size_measure));
        }

        if (attachmentInfo != null) {
            attachmentInfoView.setText(attachmentInfo);
            attachmentInfoView.setVisibility(View.VISIBLE);
        } else {
            attachmentInfoView.setVisibility(View.GONE);
        }
    }
}
