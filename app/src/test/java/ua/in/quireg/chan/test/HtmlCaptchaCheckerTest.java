package ua.in.quireg.chan.test;

import org.apache.http.Header;

import android.test.InstrumentationTestCase;

import ua.in.quireg.chan.boards.makaba.MakabaWebsite;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.interfaces.IHttpStringReader;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.CaptchaType;
import ua.in.quireg.chan.services.HtmlCaptchaChecker;
import ua.in.quireg.chan.settings.ApplicationSettings;

public class HtmlCaptchaCheckerTest extends InstrumentationTestCase {

    private final IWebsite mWebsite = new MakabaWebsite();

    public void testCanSkip() {
        String responseText = "OK";

        HtmlCaptchaChecker checker = new HtmlCaptchaChecker(new FakeHttpStringReader(responseText));
        HtmlCaptchaChecker.CaptchaResult result = checker.canSkipCaptcha(mWebsite, CaptchaType.MAILRU, "", "");

        assertTrue(result.canSkip);
    }

    public void testMustEnter() {
        String responseText = "CHECK\nSomeKey";

        HtmlCaptchaChecker checker = new HtmlCaptchaChecker(new FakeHttpStringReader(responseText));
        HtmlCaptchaChecker.CaptchaResult result = checker.canSkipCaptcha(mWebsite, CaptchaType.MAILRU, "", "");

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
