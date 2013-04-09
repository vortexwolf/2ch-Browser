package com.vortexwolf.dvach.test;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import android.content.Context;
import android.net.Uri;
import android.test.InstrumentationTestCase;

import com.vortexwolf.dvach.interfaces.IHtmlCaptchaChecker;
import com.vortexwolf.dvach.interfaces.IHttpStringReader;
import com.vortexwolf.dvach.services.domain.HtmlCaptchaChecker;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class HtmlCaptchaCheckerTest extends InstrumentationTestCase {

    private final DvachUriBuilder mDvachUriBuilder = new DvachUriBuilder(Uri.parse("http://2ch.hk"));

    private ApplicationSettings createSettings() {
        Context context = this.getInstrumentation().getContext();
        ApplicationSettings settings = new ApplicationSettings(context, context.getResources());

        return settings;
    }

    public void testCanSkip() {
        String responseText = "OK";

        IHtmlCaptchaChecker checker = new HtmlCaptchaChecker(new FakeHttpStringReader(responseText), this.mDvachUriBuilder, this.createSettings());
        HtmlCaptchaChecker.CaptchaResult result = checker.canSkipCaptcha(null);

        assertTrue(result.canSkip);
    }

    public void testMustEnter() {
        String responseText = "CHECK\nSomeKey";

        IHtmlCaptchaChecker checker = new HtmlCaptchaChecker(new FakeHttpStringReader(responseText), this.mDvachUriBuilder, this.createSettings());
        HtmlCaptchaChecker.CaptchaResult result = checker.canSkipCaptcha(null);

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
            return null;
        }
    }
}
