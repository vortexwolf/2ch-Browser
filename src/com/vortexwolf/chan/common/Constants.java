package com.vortexwolf.chan.common;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.protocol.HTTP;

import android.os.Build;

import com.vortexwolf.chan.common.utils.IoUtils;

public class Constants {

    public static final boolean DEBUG = true;

    // Нужно не забыть отключить логгирование при выпуске приложения
    public static final boolean LOGGING = DEBUG;

    public static final String ANALYTICS_KEY = DEBUG ? "UA-28782631-3" : "UA-28782631-2";

    public static final ArrayList<String> IMAGE_EXTENSIONS = new ArrayList<String>(Arrays.asList(new String[] { "jpg",
            "jpeg", "png", "gif" }));
    public static final String GIF_IMAGE = "gif";

    public static final long FILE_CACHE_THRESHOLD = IoUtils.convertMbToBytes(25);
    public static final long FILE_CACHE_TRIM_AMOUNT = IoUtils.convertMbToBytes(15);

    public static final int SDK_VERSION = Integer.parseInt(Build.VERSION.SDK);

    public static final String DEFAULT_DOWNLOAD_FOLDER = "/download/2ch Browser/";

    // Иногда меняется (когда-то был 2ch.so), поэтому добавил в настройки. 
    public static final String DEFAULT_DOMAIN = "2ch.hk";

    // Для http-запросов
    //public static final String USER_AGENT_STRING = "2ch browser (Android)";
    public static final String USER_AGENT_STRING = "Mozilla/5.0 (Windows NT 6.1; rv:24.0) Gecko/20100101 Firefox/24.0";
    public static final String SAGE_EMAIL = "sage";
    public static final Charset UTF8_CHARSET = Charset.forName(HTTP.UTF_8);
    
    public static final String CF_CLEARANCE_COOKIE = "cf_clearance";
    public static final String USERCODE_COOKIE = "usercode";

    // После этого числа порядковый номер поста становится красного цвета
    public static final int BUMP_LIMIT = 500;

    public static final int OP_POST_POSITION = 0;

    public static final int YOUTUBE_CODE_LENGTH = 11;

    // Вторая буква обязательно на кириллице, вот такой вот прикол
    public static final String ADD_POST_FORM_TASK = "pоst";
    // Если добавляем новый тред, а не пост
    public static final String ADD_THREAD_PARENT = "";

    // Request-коды для запуска метода startActivityForResult
    public static final int REQUEST_CODE_PICK_BOARD_ACTIVITY = 0;
    public static final int REQUEST_CODE_FILE_LIST_ACTIVITY = 1;
    public static final int REQUEST_CODE_ADD_POST_ACTIVITY = 2;
    public static final int REQUEST_CODE_GALLERY = 3;

    // Extra-параметры для передачи в объект Intent
    public static final String EXTRA_BOARD_NAME = "ExtraBoardName";
    public static final String EXTRA_THREAD_NUMBER = "ExtraThreadNumber";
    public static final String EXTRA_THREAD_SUBJECT = "ExtraThreadSubject";
    public static final String EXTRA_POST_NUMBER = "ExtraPostNumber";
    public static final String EXTRA_POST_COMMENT = "ExtraPostComment";
    public static final String EXTRA_SELECTED_FILE = "ExtraSelectedFile";
    public static final String EXTRA_REDIRECTED_THREAD_NUMBER = "ExtraRedirectedThreadNumber";
    public static final String EXTRA_CURRENT_URL = "ExtraCurrentUrl";
    public static final String EXTRA_PREFER_DESERIALIZED = "ExtraPreferDeserialized";
    public static final String EXTRA_THREAD_URL = "ExtraThreadUrl";

    // Идентификаторы для контекстного меню
    public static final int CONTEXT_MENU_ANSWER = 1001;
    public static final int CONTEXT_MENU_OPEN_ATTACHMENT = 1002;
    public static final int CONTEXT_MENU_REPLY_POST = 1003;
    public static final int CONTEXT_MENU_REPLY_POST_QUOTE = 1004;
    public static final int CONTEXT_MENU_DOWNLOAD_FILE = 1005;
    public static final int CONTEXT_MENU_COPY_TEXT = 1006;
    public static final int CONTEXT_MENU_COPY_URL = 1007;
    public static final int CONTEXT_MENU_VIEW_FULL_POST = 1008;
    public static final int CONTEXT_MENU_ADD_FAVORITES = 1009;
    public static final int CONTEXT_MENU_REMOVE_FAVORITES = 1010;
    public static final int CONTEXT_MENU_SEARCH_IMAGE = 1011;
    public static final int CONTEXT_MENU_HIDE_THREAD = 1012;
    public static final int CONTEXT_MENU_SHARE = 1013;
    public static final int CONTEXT_MENU_OPEN_THREAD = 1014;
    public static final int CONTEXT_MENU_SEARCH_IMAGE_GOOGLE = 1015;
}
