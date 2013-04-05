package com.vortexwolf.dvach.services.domain;

import org.apache.http.message.BasicHeader;

import android.net.Uri;

import com.vortexwolf.dvach.interfaces.IHtmlCaptchaChecker;
import com.vortexwolf.dvach.interfaces.IHttpStringReader;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class HtmlCaptchaChecker implements IHtmlCaptchaChecker {
    private final IHttpStringReader mHttpStringReader;
    private final DvachUriBuilder mDvachUriBuilder;
    private final ApplicationSettings mApplicationSettings;

    public HtmlCaptchaChecker(IHttpStringReader httpStringReader, DvachUriBuilder dvachUriBuilder, ApplicationSettings settings) {
        this.mHttpStringReader = httpStringReader;
        this.mDvachUriBuilder = dvachUriBuilder;
        this.mApplicationSettings = settings;
    }

    @Override
    public CaptchaResult canSkipCaptcha(Uri refererUri) {

        Uri uri = this.mDvachUriBuilder.create2chUri("makaba/captcha.fcgi?usercode=" + this.mApplicationSettings.getPasscode());

        // Add referer, because it always returns the incorrect value CHECK if not to set it
        org.apache.http.Header xRequest = new BasicHeader("Referer", refererUri.toString());

        org.apache.http.Header[] extraHeaders = new org.apache.http.Header[] { xRequest };
        String captchaBlock = this.mHttpStringReader.fromUri(uri.toString(), extraHeaders);

        return this.checkHtmlBlock(captchaBlock);
    }

    public CaptchaResult checkHtmlBlock(String captchaBlock) {
        CaptchaResult result = new CaptchaResult();
        result.canSkip = false;

        if (captchaBlock != null && (captchaBlock.startsWith("OK") || captchaBlock.startsWith("VIP"))) {
            result.canSkip = true;
        } else if (captchaBlock != null) {
            result.captchaKey = captchaBlock.substring(captchaBlock.indexOf('\n') + 1);
        }

        return result;
    }

    public class CaptchaResult {
        public boolean canSkip;
        public String captchaKey;
    }
}
