package com.vortexwolf.dvach.common;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import com.vortexwolf.dvach.common.utils.IoUtils;

public class Constants {
	
	public static final boolean DEBUG = true;
	
	// Нужно не забыть отключить логгирование при выпуске приложения
	public static final boolean LOGGING = DEBUG;
	
	public static final String ANALYTICS_KEY = DEBUG ? "UA-28782346-1" : "UA-28782631-1";
	
	public static final ArrayList<String> IMAGE_EXTENSIONS = new ArrayList<String>(Arrays.asList(new String[] { "jpg", "jpeg", "png", "gif" }));
		
	public static final long FILE_CACHE_THRESHOLD = IoUtils.convertMbToBytes(20);
	public static final long FILE_CACHE_TRIM_AMOUNT = IoUtils.convertMbToBytes(10);
	
	public static final String DVACH_URL = "http://2-ch.so/";
	// Доска по умолчанию
    public static final String DEFAULT_BOARD = "b";
    
    public static final String DEFAULT_DOWNLOAD_FOLDER = "/download/2ch Browser/";
    
	// Для http-запросов
    public static final String USER_AGENT_STRING = "2ch browser (Android)";
    public static final String SAGE_EMAIL = "sage";
    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    
    // После этого числа порядковый номер поста становится красного цвета
    public static final int BUMP_LIMIT = 500;
    
    public static final int OP_POST_POSITION = 0;
    
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
    public static final String EXTRA_SELECTED_BOARD = "ExtraSelectedBoard";
    public static final String EXTRA_SELECTED_FILE = ".ExtraSelectedFile";
    public static final String EXTRA_REDIRECTED_THREAD_NUMBER = "ExtraRedirectedThreadNumber";
    public static final String EXTRA_CURRENT_URL = "ExtraCurrentUrl";
    public static final String EXTRA_PREFER_DESERIALIZED = "ExtraPreferDeserialized";
    
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
}
