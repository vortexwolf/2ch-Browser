package com.vortexwolf.dvach.common.utils;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.text.format.DateFormat;
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
    private static final Pattern dateTextPattern = Pattern.compile("^[а-я]+ (\\d+) ([а-я]+) (\\d+) (\\d{2}):(\\d{2}):(\\d{2})$", Pattern.CASE_INSENSITIVE);
    private static final String[] sMonthNames = new String[] { "Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек" };
    private static final IClickListenersFactory sThumbnailOnClickListenerFactory = new ClickListenersFactory();

    public static String getDateFromTimestamp(Context context, long timeInMiliseconds, TimeZone timeZone) {
        java.text.DateFormat dateFormat = DateFormat.getDateFormat(context);
        dateFormat.setTimeZone(timeZone);
        
        java.text.DateFormat timeFormat = DateFormat.getTimeFormat(context);
        timeFormat.setTimeZone(timeZone);
        
        Date date = new Date(timeInMiliseconds);
        return dateFormat.format(date) + ", " + timeFormat.format(date);
    }

    public static String getMoscowDateFromTimestamp(Context context, long timeInMiliseconds) {
        return getDateFromTimestamp(context, timeInMiliseconds, TimeZone.getTimeZone("GMT+4"));
    }

    public static String getLocalDateFromTimestamp(Context context, long timeInMiliseconds) {
        return getDateFromTimestamp(context, timeInMiliseconds, TimeZone.getDefault());
    }
    
    //text = "Птн 12 Апр 2013 12:37:12";
    public static long parseMoscowTextDate(String text) {
        if(StringUtils.isEmpty(text)) {
            return 0;
        }
        
        Matcher m = dateTextPattern.matcher(text);
        if(!m.find()) {
            return 0;
        }
        
        int day = Integer.valueOf(m.group(1));
        String monthStr = m.group(2);
        int monthIndex = Arrays.asList(sMonthNames).indexOf(monthStr);
        int month = monthIndex != -1 ? monthIndex : 0;
        int year = Integer.valueOf(m.group(3));
        int hour = Integer.valueOf(m.group(4));
        int minute = Integer.valueOf(m.group(5));
        int second = Integer.valueOf(m.group(6));

        Date date  = new Date(year - 1900, month, day, hour, minute, second);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, -4); // from GMT+4 to UTC
        
        long result = cal.getTimeInMillis();
        return result;
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
