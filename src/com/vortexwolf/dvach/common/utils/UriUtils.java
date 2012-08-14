package com.vortexwolf.dvach.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vortexwolf.dvach.common.Constants;

import android.net.Uri;
import android.view.View;

public class UriUtils {
	
	private static final Uri dvachHostUri = Uri.parse("http://2ch.so/");
	public static final String DVACH_HOST = dvachHostUri.getHost();
		
	private static final Pattern threadUriPattern = Pattern.compile("/\\w+/res/\\d+\\.html"); // example: /b/res/12345.html
	private static final Pattern boardUriPattern = Pattern.compile("/\\w+/?(?:\\d+\\.html)?"); // example: /b or /b/1.html
	private static final Pattern groupsDvachUriPattern = Pattern.compile("^/(\\w+)/?(?:(?:(\\d+).html)|(?:res/(\\d+)\\.html))?$"); // 1: board name; 2: page number; 3: thread number
	private static final Pattern groupsFileExtensionPattern = Pattern.compile(".*\\.([a-zA-Z0-9]+)$"); // 1: file extension
	private static final Pattern groupsYoutubeCodePattern = Pattern.compile("\"http://www.youtube.com/v/(.*?)\""); // 1: video code
	
	public static boolean isDvachHost(Uri uri){
		String host = uri.getHost();
		return host != null && host.endsWith(DVACH_HOST);
	}
	
	public static String create2chURL(String board, int pageNumber) {
		return create2chURL(board, pageNumber == 0 ? null : pageNumber + ".html").toString();
	}
	
	public static String create2chThreadURL(String board, String threadNumber) {
		return create2chURL(board, "res/"+threadNumber+".html").toString();
	}
	
	public static String create2chPostURL(String board, String threadNumber, String postNumber) {
		return create2chThreadURL(board, threadNumber) + "#" + postNumber;
	}
	
	public static Uri create2chURL(String board, String path) {
		Uri boardUri = appendPath(dvachHostUri, board);
		
		if(!StringUtils.isEmpty(path)){
			boardUri = appendPath(boardUri, path);
		}
		
		return boardUri;
	}
	
	public static Uri adjust2chRelativeUri(Uri uri){
		if(uri.isRelative()){
			return appendPath(dvachHostUri, uri.toString());
		}
		
		return uri;
	}
	
	private static Uri appendPath(Uri baseUri, String path){
		path = path.replaceAll("^/*(.*)$", "$1");
		return Uri.withAppendedPath(baseUri, path);
	}
	
	public static boolean isThreadUri(Uri uri){
		return testUriPath(uri, threadUriPattern);
	}
	
	public static boolean isBoardUri(Uri uri){
		return testUriPath(uri, boardUriPattern);
	}
	
    public static boolean isImageUri(Uri uri) {
    	String extension = getFileExtension(uri);

    	return Constants.IMAGE_EXTENSIONS.contains(extension);
    }
	
	public static String getBoardName(Uri uri){
		String boardName = getGroupValue(uri, groupsDvachUriPattern, 1);
		return boardName;
	}

	public static int getBoardPageNumber(Uri uri){
		String pageNumber = getGroupValue(uri, groupsDvachUriPattern, 2);
		return pageNumber == null ? 0 : Integer.parseInt(pageNumber);
	}
	
	public static String getThreadNumber(Uri uri){
		String threadNumber = getGroupValue(uri, groupsDvachUriPattern, 3);
		return threadNumber;
	}
	
    public static String getFileExtension(Uri uri){
    	String extension = getGroupValue(uri, groupsFileExtensionPattern, 1);
    	return extension;
    }
	
    public static boolean isYoutubeUri(Uri uri) {
    	if (uri == null) return false;
    	String host = uri.getHost();
    	return host != null && host.endsWith("youtube.com");
    }
    
	public static String getYouTubeCode(String videoHtml) {
		if(StringUtils.isEmpty(videoHtml)) return null;
		
		String videoCode = getGroupValue(videoHtml, groupsYoutubeCodePattern, 1);

		return videoCode;
	}
	
	private static boolean testUriPath(Uri uri, Pattern pattern){
		if (uri == null) return false;
		String path = uri.getPath();
		
		Matcher m = pattern.matcher(path);
		boolean matches = m.matches();
		
		return matches;
	}
	
	private static String getGroupValue(Uri uri, Pattern pattern, int groupIndex){
		if (uri == null) return null;
    	String path = uri.getPath();
    	
		return getGroupValue(path, pattern, groupIndex);
	}
	
	private static String getGroupValue(String str, Pattern pattern, int groupIndex){
		if (str == null) return null;
		
		Matcher m = pattern.matcher(str);
		if(m.find() && m.groupCount() > 0){
			return m.group(groupIndex);
		}
		
		return null;
	}
}
