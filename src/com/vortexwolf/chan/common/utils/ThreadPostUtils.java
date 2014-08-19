package com.vortexwolf.chan.common.utils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Debug;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.activities.ImageGalleryActivity;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.interfaces.IBitmapManager;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.models.presentation.AttachmentInfo;
import com.vortexwolf.chan.services.BrowserLauncher;
import com.vortexwolf.chan.services.ThreadImagesService;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class ThreadPostUtils {
    private static final Pattern dateTextPattern = Pattern.compile("^[а-я]+ (\\d+) ([а-я]+) (\\d+) (\\d{2}):(\\d{2}):(\\d{2})$", Pattern.CASE_INSENSITIVE);
    private static final String[] sMonthNames = new String[] { "Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг",
            "Сен", "Окт", "Ноя", "Дек" };

    private static long sMaxVmHeap = Runtime.getRuntime().maxMemory() / 1024;
    private static long sHeapPad = 1024;
    
    private static final List<String> sMakabaBoards = Arrays.asList(new String[] { 
        "fag", "fg", "fur", "g", "ga", "h", "ho", "sex", "fet", "e", "hc", "mmo", "tes", "vg", 
        "moba", "b", "soc", "ftb", "po", "re", "tr", "wm", "au"
    });

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
        if (StringUtils.isEmpty(text)) {
            return 0;
        }

        Matcher m = dateTextPattern.matcher(text);
        if (!m.find()) {
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

        Date date = new Date(year - 1900, month, day, hour, minute, second);

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
    public static boolean hasAttachment(PostModel item) {
        return item.getAttachments().size() > 0;
    }

    public static void openAttachment(final AttachmentInfo attachment, final Context context, final ApplicationSettings settings, final String threadUrl) {
        if (attachment == null) {
            return;
        }

        int imageSize = attachment.getSize();
        String url = attachment.getSourceUrl(settings);

        long allocatedSize = Debug.getNativeHeapAllocatedSize() / 1024 + imageSize + sHeapPad;
        if (allocatedSize > sMaxVmHeap) {
            long freeSize = Math.max(0, imageSize - (allocatedSize - sMaxVmHeap));
            AppearanceUtils.showToastMessage(context, "Image is " + imageSize + "Kb. Available Memory is " + freeSize + "Kb");
            return;
        }

        Uri uri = Uri.parse(url);
        if (threadUrl != null && !settings.isLegacyImageViewer() && Factory.getContainer().resolve(ThreadImagesService.class).hasImage(threadUrl, url)) {
            // open a gallery activity
            Intent imageGallery = new Intent(context, ImageGalleryActivity.class);
            imageGallery.setData(uri);
            imageGallery.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            imageGallery.putExtra(Constants.EXTRA_THREAD_URL, threadUrl);
            context.startActivity(imageGallery);
        } else {
            BrowserLauncher.launchInternalBrowser(context, url);
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
    
    public static boolean isMakabaBoard(String boardName) {
        return sMakabaBoards.indexOf(boardName) != -1;
    }

    /**
     * Разбирается с прикрепленным файлом для треда или поста; перенес сюда,
     * чтобы не повторять код
     * 
     * @param attachment
     *            Модель прикрепленного к треду или посту файла
     * @param imageView
     *            Место для картинки
     * @param bitmapManager
     *            Для загрузки картинок с интернета
     * @param thumbnailOnClickListenerFactory
     *            Для обработки нажатия по картинке
     * @param activity
     */
    public static void handleAttachmentImage(boolean isBusy, final AttachmentInfo attachment, ImageView imageView, View fullThumbnailView, IBitmapManager bitmapManager, final ApplicationSettings settings, final Context context, final String threadUrl) {
        imageView.setImageResource(android.R.color.transparent); // clear the image content

        // Ищем прикрепленный файл, в случае наличия добавляем его как ссылку
        if (attachment == null || attachment.isEmpty()) {
            imageView.setVisibility(View.GONE);
            fullThumbnailView.setVisibility(View.GONE);
        } else {
            fullThumbnailView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);

            // Обработчик события нажатия на картинку
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ThreadPostUtils.openAttachment(attachment, context, settings, threadUrl);
                }
            });

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
                        bitmapManager.fetchBitmapOnThread(thumbnailUrl, imageView, null, R.drawable.error_image);
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
