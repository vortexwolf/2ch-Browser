package com.vortexwolf.chan.test;

import android.test.InstrumentationTestCase;

import com.vortexwolf.chan.exceptions.SendPostException;
import com.vortexwolf.chan.models.domain.SendPostResult;
import com.vortexwolf.chan.services.PostResponseParser;

// TODO: remove and write tests for Makaba responses end errors
public class PostResponseParserTest extends InstrumentationTestCase {

    private final PostResponseParser mParser = new PostResponseParser();

    public void testAddPostSuccess() throws SendPostException {
        String response = "Reload the page to get source for: http://2ch.so/test/wakaba.pl";
        SendPostResult result = this.mParser.isPostSuccessful("f", response);

        assertTrue(result.isSuccess);
    }

    public void testAddWithoutComment() {
        String response = "<hr style=\"clear: left;\">" + "<center>" + "<strong><font size=\"5\">Some test error.</strong></font><br />" + "<h2 style=\"text-align: center\">" + "<a href=\"http://2ch.so/test/res/52984.html\">Назад</a><br />" + "</h2>" + "</center>";

        SendPostResult result = this.mParser.isPostSuccessful("f", response);

        assertFalse(result.isSuccess);
        assertEquals(result.error, "Some test error.");
    }
}
