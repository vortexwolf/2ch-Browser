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
        
        CaptchaResult result;
        try {
            String captchaBlock = this.mHttpStringReader.fromUri(uri.toString(), extraHeaders);
            result = this.checkHtmlBlock(captchaBlock);
        } catch (Exception e) {
            result = this.createEmptyResult();
        }
        
        return result;
    }

    public CaptchaResult checkHtmlBlock(String captchaBlock) {
        if(captchaBlock == null){
            return this.createEmptyResult();
        }
        
        CaptchaResult result = new CaptchaResult();
        if (captchaBlock.startsWith("OK")) {
            result.canSkip = true;
        } else if (captchaBlock.startsWith("VIP")) {
            result.canSkip = true;
            result.passCode = true;
        } else if (captchaBlock.startsWith("CHECK")) {
            result.canSkip = false;
            result.captchaKey = captchaBlock.substring(captchaBlock.indexOf('\n') + 1);
        }

        return result;
    }
    
    private CaptchaResult createEmptyResult(){
        return new CaptchaResult();
    }

    public class CaptchaResult {
        public boolean canSkip;
        public boolean passCode;
        public String captchaKey;
    }
}
