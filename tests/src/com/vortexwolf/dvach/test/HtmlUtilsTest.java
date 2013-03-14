package com.vortexwolf.dvach.test;

import android.test.InstrumentationTestCase;

import com.vortexwolf.dvach.common.utils.HtmlUtils;
import com.vortexwolf.dvach.common.utils.ThreadPostUtils;

public class HtmlUtilsTest extends InstrumentationTestCase {

    public void testGetStringFontColor() {
        String style = "color: rgb(255, 0, 0);";
        String color = HtmlUtils.getStringFontColor(style);
        assertEquals(color, "#FF0000");

        style = null;
        color = HtmlUtils.getStringFontColor(style);
        assertEquals(color, null);
    }

    public void testRemoveLinksFromComment() {
        String comment = ">>12345";
        String result = ThreadPostUtils.removeLinksFromComment(comment);
        assertEquals(result, "");

        comment = ">>12345z";
        result = ThreadPostUtils.removeLinksFromComment(comment);
        assertEquals(result, "z");

        comment = ">>12345>>23456";
        result = ThreadPostUtils.removeLinksFromComment(comment);
        assertEquals(result, "");

        comment = ">>12345>123";
        result = ThreadPostUtils.removeLinksFromComment(comment);
        assertEquals(result, ">123");

        comment = ">>12345 z";
        result = ThreadPostUtils.removeLinksFromComment(comment);
        assertEquals(result, "z");

        comment = ">>12345\nz\n>>2345";
        result = ThreadPostUtils.removeLinksFromComment(comment);
        assertEquals(result, "z\n");

        comment = "a >>123 z";
        result = ThreadPostUtils.removeLinksFromComment(comment);
        assertEquals(result, "a >>123 z");
    }

    public void testTrimBr() {
        String text = "test<br /><br />";
        String result = HtmlUtils.trimBr(text);
        assertEquals(result, "test");

        text = "<br />test2<br />";
        result = HtmlUtils.trimBr(text);
        assertEquals(result, "test2");

        text = "test<br />3";
        result = HtmlUtils.trimBr(text);
        assertEquals(result, "test<br/>3");
    }
}
