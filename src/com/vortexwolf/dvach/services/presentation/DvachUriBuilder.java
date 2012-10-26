package com.vortexwolf.dvach.services.presentation;

import android.net.Uri;

import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.utils.StringUtils;

public class DvachUriBuilder {
	private final Uri mDvachHostUri;
	
	public DvachUriBuilder(Uri hostUri) {
		this.mDvachHostUri = hostUri;
	}
	
	public String create2chPostUrl(String board, String threadNumber, String postNumber) {
		return create2chThreadUrl(board, threadNumber) + "#" + postNumber;
	}
	
	public String create2chThreadUrl(String board, String threadNumber) {
		return create2chBoardUri(board, "res/"+threadNumber+".html").toString();
	}
	
	public Uri create2chBoardUri(String board, int pageNumber) {
		return create2chBoardUri(board, pageNumber == 0 ? null : pageNumber + ".html");
	}
	
	public Uri create2chBoardUri(String board, String path) {
		Uri boardUri = appendPath(mDvachHostUri, board);
		
		if(!StringUtils.isEmpty(path)){
			boardUri = appendPath(boardUri, path);
		}
		
		return boardUri;
	}
	
	public Uri create2chUri(String path){
		Uri uri = appendPath(mDvachHostUri, path);
		
		return uri;
	}
	
	public Uri adjust2chRelativeUri(Uri uri){
		return uri.isRelative() ? appendPath(mDvachHostUri, uri.toString()) : uri;
	}
	
	private Uri appendPath(Uri baseUri, String path){
		path = path.replaceFirst("^/*", "");
		return Uri.withAppendedPath(baseUri, path);
	}
}
