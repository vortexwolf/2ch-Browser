package ua.in.quireg.chan.common.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;

import com.squareup.haha.perflib.Main;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import ru.terrakok.cicerone.Router;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.models.domain.PostModel;
import ua.in.quireg.chan.models.presentation.AttachmentInfo;
import ua.in.quireg.chan.models.presentation.ThumbnailViewBag;
import ua.in.quireg.chan.mvp.routing.MainRouter;
import ua.in.quireg.chan.services.BitmapManager;
import ua.in.quireg.chan.services.BrowserLauncher;
import ua.in.quireg.chan.settings.ApplicationSettings;
import ua.in.quireg.chan.ui.activities.ImageGalleryActivity;

public class ThreadPostUtils {

    @Inject
    MainRouter mMainRouter;

    {
        MainApplication.getAppComponent().inject(this);
    }

    private static final Pattern dateTextPattern = Pattern.compile("^[а-я]+ (\\d+) ([а-я]+) (\\d+) (\\d{2}):(\\d{2}):(\\d{2})$", Pattern.CASE_INSENSITIVE);

    private static final String[] sMonthNames = new String[]
            {"Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"};

    private static final List<String> sExtendedBumpLimit = Arrays.asList("vg", "ukr", "wm", "mobi", "vn");

    public static String getDateFromTimestamp(Context context, long timeInMilliseconds, TimeZone timeZone) {
        java.text.DateFormat dateFormat = DateFormat.getDateFormat(context);
        dateFormat.setTimeZone(timeZone);

        java.text.DateFormat timeFormat = DateFormat.getTimeFormat(context);
        timeFormat.setTimeZone(timeZone);

        Date date = new Date(timeInMilliseconds);
        return dateFormat.format(date) + ", " + timeFormat.format(date);
    }

    public static String getMoscowDateFromTimestamp(Context context, long timeInMilliseconds) {
        return getDateFromTimestamp(context, timeInMilliseconds, TimeZone.getTimeZone("GMT+3"));
    }

    public static String getLocalDateFromTimestamp(Context context, long timeInMilliseconds) {
        return getDateFromTimestamp(context, timeInMilliseconds, TimeZone.getDefault());
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
        int offset = TimeZone.getDefault().getOffset(cal.getTimeInMillis());
        cal.add(Calendar.MILLISECOND, offset);

        return cal.getTimeInMillis();
    }

    /**
     * Удаляет ссылки на другие сообщения из поста, если они расположены вначале
     * строки
     */
    public static String removeLinksFromComment(String comment) {

        return comment.replaceAll("(^|\n)(>>\\d+(\n|\\s)?)+", "$1");
    }

    /**
     * Проверяет, прикреплен ли к посту какой-либо файл
     */
    public static boolean hasAttachment(PostModel item) {
        return item.getAttachments().size() > 0;
    }

    public static int getAttachmentsNumber(PostModel item) {
        return item.getAttachments().size();
    }

    public static void openExternalAttachment(final AttachmentInfo attachment, final Context context) {
        if (attachment == null) return;
        String url = attachment.getSourceUrl();
        BrowserLauncher.launchExternalBrowser(context, url);
    }

    public void openAttachment(final AttachmentInfo attachment, final Context context) {
        if (attachment == null) {
            return;
        }
        String url = attachment.getSourceUrl();

        Uri uri = Uri.parse(url);
        ApplicationSettings settings = Factory.resolve(ApplicationSettings.class);

        if (attachment.isVideo() && settings.getVideoPlayer() == Constants.VIDEO_PLAYER_EXTERNAL_1CLICK) {
            BrowserLauncher.launchExternalBrowser(context, url);
        } else {
            // open a gallery fragment
            //TODO not forget to clean this
            //NavigationService.getInstance().navigateGallery(uri, attachment.getThreadUrl());
            mMainRouter.navigateGallery(uri, attachment.getThreadUrl());

//            Intent imageGallery = new Intent(context, ImageGalleryActivity.class);
//            imageGallery.setData(uri);
//            imageGallery.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            imageGallery.putExtra(Constants.EXTRA_THREAD_URL, attachment.getThreadUrl());
//            context.startActivity(imageGallery);
        }
    }

    /**
     * Будет отображать другим цветом посты после бамплимита
     */
    public static int getBumpLimitNumber(String boardName) {
        if (isExtendedBumpLimit(boardName)) return Constants.BUMP_LIMIT_EXTENDED;
        return Constants.BUMP_LIMIT;
    }

    public static boolean isExtendedBumpLimit(String boardName) {
        return sExtendedBumpLimit.indexOf(boardName) != -1;
    }

    public static int getMaximumAttachments(String boardName) {
        return 4;
    }

    public static void refreshAttachmentView(boolean isBusy, AttachmentInfo attachment, ThumbnailViewBag thumbnailView) {
        if (attachment == null || attachment.isEmpty()) {
            thumbnailView.hide();
            return;
        }

        thumbnailView.container.setVisibility(View.VISIBLE);
        thumbnailView.info.setText(attachment.getDescription());
        loadAttachmentImage(isBusy, attachment, thumbnailView.image);
    }

    public static void setNonBusyAttachment(AttachmentInfo attachment, ImageView imageView) {
        if (ThreadPostUtils.shouldLoadFromWeb(attachment)) {
            ThreadPostUtils.loadAttachmentImage(false, attachment, imageView);
        }
    }

    private static void loadAttachmentImage(boolean isBusy, final AttachmentInfo attachment, ImageView imageView) {
        Uri thumbnailUrl = attachment.getThumbnailUrl() != null ? Uri.parse(attachment.getThumbnailUrl()) : null;
        // clear the image content
        imageView.setTag(thumbnailUrl);
        imageView.setImageResource(android.R.color.transparent);
        imageView.setOnClickListener(v -> {
            new ThreadPostUtils().openAttachment(attachment, v.getContext());
//            ThreadPostUtils.openAttachment(attachment, v.getContext());
        });

        if (isBusy && shouldLoadFromWeb(attachment)) {
            return;
        }

        // Также добавляем уменьшенное изображение, нажатие на которое
        // открывает файл в полном размере
        if (thumbnailUrl != null) {
            // Ничего не загружаем, если так установлено в настройках
            ApplicationSettings settings = Factory.resolve(ApplicationSettings.class);
            BitmapManager bitmapManager = Factory.resolve(BitmapManager.class);
            if (settings.isLoadThumbnails() || bitmapManager.isCached(thumbnailUrl.toString())) {
                bitmapManager.fetchBitmapOnThread(thumbnailUrl, imageView, true, null, R.drawable.error_image);
            } else if (!settings.isLoadThumbnails()) {
                imageView.setImageResource(R.drawable.empty_image);
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

    private static boolean shouldLoadFromWeb(AttachmentInfo attachment) {
        if (attachment == null || attachment.isEmpty()) {
            return false;
        }

        ApplicationSettings settings = Factory.resolve(ApplicationSettings.class);
        BitmapManager bitmapManager = Factory.resolve(BitmapManager.class);

        String thumbnailUrl = attachment.getThumbnailUrl();
        return thumbnailUrl != null && settings.isLoadThumbnails() && !bitmapManager.isCached(thumbnailUrl);
    }

    public static String getDefaultName(String board) {
        if (board.equals("fg")) return "уточка";
        if (board.equals("ukr")) return "Безосібний";
        if (board.equals("test")) return "Анонимчик";
        return "Аноним";
    }
}
