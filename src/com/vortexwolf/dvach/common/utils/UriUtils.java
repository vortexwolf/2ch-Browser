package com.vortexwolf.dvach.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.Uri;

public class UriUtils {
	
	public static final String DVACH_HOST = "2ch.so";
	private static final Uri dvachHostUri = Uri.parse("http://2ch.so/");
	
	public static final ArrayList<String> IMAGE_EXTENSIONS = new ArrayList<String>(Arrays.asList(new String[] { "jpg", "jpeg", "png", "gif" }));
	//String[] extensions = new String[] { ".jpg", ".jpeg", ".png" };
	// extensionsList = new ArrayList<String>(Arrays.asList(extensions));
	
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
		return create2chURL(board, "res/"+threadNumber+".html#" + postNumber).toString();
	}
	
	public static Uri create2chURL(String board, String path) {
		Uri boardUri = appendPath(dvachHostUri, board + "/");
		if(!StringUtils.isEmpty(path)){
			return appendPath(boardUri, path);
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
		path = path.replaceAll("^/*(.*)", "$1");
		return Uri.withAppendedPath(baseUri, path);
	}
	
	public static String getBoardName(Uri uri){
		if (uri == null) return null;
		
		String path = uri.getPath();
		String boardName = path.replaceFirst("^/([^/]*).*", "$1");
		return boardName;
	}

	public static int getBoardPageNumber(Uri uri){
		if (uri == null) return 0;
		
		String path = uri.getPath();
		Matcher m = Pattern.compile("^/[^/]*/([0-9]+)\\.html").matcher(path);
		if(m.find() && m.groupCount() > 0){
			return Integer.parseInt(m.group(1));
		}
		else return 0;
	}
	
	public static String getPageName(Uri uri){
		if (uri == null) return null;
    	String path = uri.getPath();
    	
		Matcher m = Pattern.compile("^.*?([^/]*)\\.[^\\.]*").matcher(path);
		if(m.find() && m.groupCount() > 0){
			return m.group(1);
		}
		else return null;
	}
	
    public static boolean isYoutubeUri(Uri uri) {
    	if (uri == null) return false;
    	String host = uri.getHost();
    	return host != null && host.endsWith("youtube.com");
    }
    
    public static boolean isImageUri(Uri uri) {
    	String extension = getFileExtension(uri);

    	return IMAGE_EXTENSIONS.contains(extension);
    }
    
    public static String getFileExtension(Uri uri){
    	if (uri == null) return null;
    	String path = uri.getPath();
    	//String extension = path.replaceFirst("^.*/[^/]*\\.([^\\./]*)$", "$1");
		Pattern p = Pattern.compile("^.*\\.([a-zA-Z0-9]+)");
		Matcher m = p.matcher(path);
		if(m.find() && m.groupCount() > 0){
			return m.group(1);
		}
		else return null;
    	//String extension = path.replaceFirst("^.*\\.([a-zA-Z0-9]+)", "$1");
    	//return extension;
    }
    
	public static String parseYouTubeCode(String videoHtml) {
		if(StringUtils.isEmpty(videoHtml)){
			return null;
		}
		
		String videoCode = null;
		Pattern p = Pattern.compile("\"http://www.youtube.com/v/(.*?)\"");
		Matcher m = p.matcher(videoHtml);
		if(m.find() && m.groupCount() > 0)
		{
			// videoUrl = m.group(0); //Включает кавычки
			videoCode = m.group(1);
		}
		return videoCode;
	}
}
