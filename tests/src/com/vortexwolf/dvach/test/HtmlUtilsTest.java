package com.vortexwolf.dvach.test;

import com.vortexwolf.dvach.common.utils.HtmlUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;

import android.net.Uri;
import android.test.InstrumentationTestCase;

public class HtmlUtilsTest extends InstrumentationTestCase {
	public void testParseId(){
		
		String name = "Аноним ID:&nbsp;<span class=postertrip>57aGrU6n</span>";
		String postId = HtmlUtils.parseIdFromName(name);
		assertEquals(postId, "57aGrU6n");
		
		name = "Аноним ID:&nbsp;<span id=\"qzL+/X8r\" onmouseover='this.innerHTML=\"<span class=postertrip>qzL+/X8r</span>\";'>Heaven</span>";
		postId = HtmlUtils.parseIdFromName(name);
		assertEquals(postId, "qzL+/X8r");
		
		name = "Аноним ID:&nbsp;Heaven";
		postId = HtmlUtils.parseIdFromName(name);
		assertEquals(postId, "Heaven");

		name = "Аноним ID:&nbsp;<span class=\"postertripid\">s+xw3L0X</span>";
		postId = HtmlUtils.parseIdFromName(name);
		assertEquals(postId, "s+xw3L0X");
		
		name = "Аноним";
		postId = HtmlUtils.parseIdFromName(name);
		assertEquals(postId, null);
	}
}
