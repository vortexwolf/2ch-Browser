package com.vortexwolf.dvach.test;

import junit.framework.Assert;
import android.net.Uri;
import android.test.InstrumentationTestCase;

import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.test.R;


public class UriUtilsTest extends InstrumentationTestCase {

	private static final String imageUri = "http://2ch.so/b/src/12345.png";
	private static final String boardUriVg = "http://2ch.so/vg";
	private static final String boardUriBPage1 = "http://2ch.so/b/1.html";
	private static final String threadRelativeUriB = "/b/res/1000.html";
	private static final String threadUriB = "http://2ch.so/b/res/1000.html";
	private static final String postUriPr = "http://2ch.so/pr/res/2000.html#222";
	
	public void testIs2chHostNegative(){
		boolean isDvachHost = UriUtils.isDvachHost(Uri.parse(threadRelativeUriB));
		assertFalse(isDvachHost);
	}
	
	public void testIs2chHostPositive(){
		boolean isDvachHost = UriUtils.isDvachHost(Uri.parse(threadUriB));
		assertTrue(isDvachHost);
	}
	
	public void testCreate2chUrl(){
		
		String resultUrl = UriUtils.create2chURL("b", 0);
		assertEquals(resultUrl, "http://2ch.so/b");
		
		resultUrl = UriUtils.create2chURL("b", 1);
		assertEquals(resultUrl, "http://2ch.so/b/1.html");
		
		resultUrl = UriUtils.create2chThreadURL("b", "123");
		assertEquals(resultUrl, "http://2ch.so/b/res/123.html");
		
		resultUrl = UriUtils.create2chPostURL("b", "123", "456");
		assertEquals(resultUrl, "http://2ch.so/b/res/123.html#456");
		
		resultUrl = UriUtils.create2chURL("b", "thumb/12345.jpg").toString();
		assertEquals(resultUrl, "http://2ch.so/b/thumb/12345.jpg");
		
		resultUrl = UriUtils.create2chURL("b", "/thumb/12345.jpg").toString();
		assertEquals(resultUrl, "http://2ch.so/b/thumb/12345.jpg");
	}

	public void testGetBoardName(){
		
		Uri uri = Uri.parse(boardUriVg);
		String boardName = UriUtils.getBoardName(uri);
		assertEquals(boardName, "vg");
		
		uri = Uri.parse(boardUriVg + "/");
		boardName = UriUtils.getBoardName(uri);
		assertEquals(boardName, "vg");
		
		uri = Uri.parse(boardUriBPage1);
		boardName = UriUtils.getBoardName(uri);
		assertEquals(boardName, "b");
		
		uri = Uri.parse(threadUriB);
		boardName = UriUtils.getBoardName(uri);
		assertEquals(boardName, "b");
		
		uri = Uri.parse(threadRelativeUriB);
		boardName = UriUtils.getBoardName(uri);
		assertEquals(boardName, "b");
		
		uri = Uri.parse(postUriPr);
		boardName = UriUtils.getBoardName(uri);
		assertEquals(boardName, "pr");
	}
	
	public void testGetThreadNumber(){
		Uri uri = Uri.parse(threadUriB);
		String pageName = UriUtils.getThreadNumber(uri);
		assertEquals(pageName, "1000");
		
		uri = Uri.parse(threadRelativeUriB);
		pageName = UriUtils.getThreadNumber(uri);
		assertEquals(pageName, "1000");
		
		uri = Uri.parse(postUriPr);
		pageName = UriUtils.getThreadNumber(uri);
		assertEquals(pageName, "2000");
	}
	
	public void testGetBoardPageNumber(){
		Uri uri = Uri.parse(boardUriBPage1);
		int pageNumber = UriUtils.getBoardPageNumber(uri);
		assertEquals(pageNumber, 1);
		
		uri = Uri.parse(boardUriVg);
		pageNumber = UriUtils.getBoardPageNumber(uri);
		assertEquals(pageNumber, 0);
	}
	
	public void testAdjust2chRelativeUri(){
		Uri uri = Uri.parse(threadRelativeUriB);
		String resultUri = UriUtils.adjust2chRelativeUri(uri).toString();
		assertEquals(resultUri, threadUriB);
		
		uri = Uri.parse("/test/res/52916.html#52916");
		resultUri = UriUtils.adjust2chRelativeUri(uri).toString();
		assertEquals(resultUri, "http://2ch.so/test/res/52916.html#52916");
		
		uri = Uri.parse("test");
		resultUri = UriUtils.adjust2chRelativeUri(uri).toString();
		assertEquals(resultUri, "http://2ch.so/test");
	}
	
	public void testIsYoutubeUri(){
		Uri uri = Uri.parse("http://youtube.com/watch?v=F4hQ4J4BFOM");
		assertTrue(UriUtils.isYoutubeUri(uri));
	}
	
	public void testIsImageUri(){
		Uri uri = Uri.parse(imageUri);
		assertEquals(UriUtils.isImageUri(uri), true);
		
		uri = Uri.parse(threadUriB);
		assertEquals(UriUtils.isImageUri(uri), false);
	}
	
	public void testGetFileExtension(){
		Uri uri = Uri.parse("/test/1 2 3.pdf");
		String extension = UriUtils.getFileExtension(uri);
		assertEquals(extension, "pdf");
		
		uri = Uri.parse("src/test.flash.swf");
		extension = UriUtils.getFileExtension(uri);
		assertEquals(extension, "swf");
		
		uri = Uri.parse(imageUri);
		extension = UriUtils.getFileExtension(uri);
		assertEquals(extension, "png");

		uri = Uri.parse(threadUriB);
		extension = UriUtils.getFileExtension(uri);
		assertEquals(extension, "html");
		
		uri = Uri.parse(boardUriVg);
		extension = UriUtils.getFileExtension(uri);
		assertEquals(extension, null);
	}
	
	public void testIsThreadUri(){
		assertEquals(UriUtils.isThreadUri(Uri.parse(threadUriB)), true);

		assertEquals(UriUtils.isThreadUri(Uri.parse(postUriPr)), true);
		
		assertEquals(UriUtils.isThreadUri(Uri.parse(boardUriVg)), false);
		
		assertEquals(UriUtils.isThreadUri(Uri.parse(imageUri)), false);
	}
	
	public void testIsBoardUri(){

		assertEquals(UriUtils.isBoardUri(Uri.parse(boardUriVg)), true);

		assertEquals(UriUtils.isBoardUri(Uri.parse(boardUriBPage1)), true);
		
		assertEquals(UriUtils.isBoardUri(Uri.parse(threadUriB)), false);
		
		assertEquals(UriUtils.isBoardUri(Uri.parse(imageUri)), false);
	}
	
	public void testGetYoutubeCodeTest(){
		String testHtml = "<a href=\"http://www.youtube.com/v/F4hQ4J4BFOM\"/>";
		String code = UriUtils.getYouTubeCode(testHtml);
		
		assertEquals(code, "F4hQ4J4BFOM");
	}
}
