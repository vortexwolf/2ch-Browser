package com.vortexwolf.chan.test;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.test.InstrumentationTestCase;

import com.vortexwolf.chan.boards.makaba.MakabaWebsite;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.interfaces.IHttpStringReader;
import com.vortexwolf.chan.interfaces.IWebsite;
import com.vortexwolf.chan.models.domain.CaptchaType;
import com.vortexwolf.chan.services.HtmlCaptchaChecker;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class HtmlCaptchaCheckerTest extends InstrumentationTestCase {

    private final IWebsite mWebsite = new MakabaWebsite();

    public void testCanSkip() {
        String responseText = "OK";

        HtmlCaptchaChecker checker = new HtmlCaptchaChecker(new FakeHttpStringReader(responseText), Factory.resolve(ApplicationSettings.class));
        HtmlCaptchaChecker.CaptchaResult result = checker.canSkipCaptcha(mWebsite, CaptchaType.MAILRU, "");

        assertTrue(result.canSkip);
    }

    public void testMustEnter() {
        String responseText = "CHECK\nSomeKey";

        HtmlCaptchaChecker checker = new HtmlCaptchaChecker(new FakeHttpStringReader(responseText), Factory.resolve(ApplicationSettings.class));
        HtmlCaptchaChecker.CaptchaResult result = checker.canSkipCaptcha(mWebsite, CaptchaType.MAILRU, "");

        assertFalse(result.canSkip);
        assertEquals("SomeKey", result.captchaKey);
    }

    private class FakeHttpStringReader implements IHttpStringReader {

        private final String mResponse;

        public FakeHttpStringReader(String response) {
            this.mResponse = response;
        }

        @Override
        public String fromUri(String uri) {
            return this.mResponse;
        }

        @Override
        public String fromUri(String uri, Header[] customHeaders) {
            return this.mResponse;
        }
    }
}
