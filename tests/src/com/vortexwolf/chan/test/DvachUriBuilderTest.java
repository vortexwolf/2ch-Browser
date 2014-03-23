package com.vortexwolf.chan.test;

import android.net.Uri;
import android.test.InstrumentationTestCase;

import com.vortexwolf.chan.services.presentation.DvachUriBuilder;

public class DvachUriBuilderTest extends InstrumentationTestCase {

    private static final DvachUriBuilder sUriBuilder = new DvachUriBuilder(Uri.parse("http://2ch.hk"));

    private static final String threadRelativeUriB = "/b/res/1000.html";
    private static final String threadUriB = "http://2ch.hk/b/res/1000.html";

    public void testCreate2chUrl() {

        String resultUrl = sUriBuilder.create2chBoardUri("b", 0).toString();
        assertEquals(resultUrl, "http://2ch.hk/b");

        resultUrl = sUriBuilder.create2chBoardUri("b", 1).toString();
        assertEquals(resultUrl, "http://2ch.hk/b/1.html");

        resultUrl = sUriBuilder.create2chThreadUrl("b", "123");
        assertEquals(resultUrl, "http://2ch.hk/b/res/123.html");

        resultUrl = sUriBuilder.create2chPostUrl("b", "123", "456");
        assertEquals(resultUrl, "http://2ch.hk/b/res/123.html#456");

        resultUrl = sUriBuilder.create2chBoardUri("b", "thumb/12345.jpg").toString();
        assertEquals(resultUrl, "http://2ch.hk/b/thumb/12345.jpg");

        resultUrl = sUriBuilder.create2chBoardUri("b", "/thumb/12345.jpg").toString();
        assertEquals(resultUrl, "http://2ch.hk/b/thumb/12345.jpg");

        resultUrl = sUriBuilder.create2chUri("/wakaba").toString();
        assertEquals(resultUrl, "http://2ch.hk/wakaba");
    }

    public void testAdjust2chRelativeUri() {
        Uri uri = Uri.parse(threadRelativeUriB);
        String resultUri = sUriBuilder.adjust2chRelativeUri(uri).toString();
        assertEquals(resultUri, threadUriB);

        uri = Uri.parse("/test/res/52916.html#52916");
        resultUri = sUriBuilder.adjust2chRelativeUri(uri).toString();
        assertEquals(resultUri, "http://2ch.hk/test/res/52916.html#52916");

        uri = Uri.parse("test");
        resultUri = sUriBuilder.adjust2chRelativeUri(uri).toString();
        assertEquals(resultUri, "http://2ch.hk/test");

        uri = Uri.parse("///test");
        resultUri = sUriBuilder.adjust2chRelativeUri(uri).toString();
        assertEquals(resultUri, "http://2ch.hk/test");
    }
}
