package com.vortexwolf.chan.services;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import android.net.Uri;

import com.vortexwolf.chan.common.Websites;
import com.vortexwolf.chan.common.utils.UriUtils;
import com.vortexwolf.chan.interfaces.IHttpStringReader;
import com.vortexwolf.chan.interfaces.IUrlBuilder;
import com.vortexwolf.chan.interfaces.IWebsite;
import com.vortexwolf.chan.models.domain.CaptchaType;
import com.vortexwolf.chan.settings.ApplicationSettings;
import com.wildflyforcer.utils.CaptchaResultNew;

public class HtmlCaptchaChecker {
    private final IHttpStringReader mHttpStringReader;
    private final ApplicationSettings mApplicationSettings;

    public HtmlCaptchaChecker(IHttpStringReader httpStringReader, ApplicationSettings settings) {
        this.mHttpStringReader = httpStringReader;
        this.mApplicationSettings = settings;
    }

    public CaptchaResult canSkipCaptcha(IWebsite website, CaptchaType captchaType, String referer) {
        IUrlBuilder urlBuilder = website.getUrlBuilder();
        String checkUrl = urlBuilder.getPasscodeCookieCheckUrl("", captchaType);

        CaptchaResult result;
        try {
            Header[] extraHeaders = new Header[] { new BasicHeader("Referer", referer) };
            String captchaBlock = this.mHttpStringReader.fromUri(checkUrl, extraHeaders);
            result = this.checkHtmlBlock(captchaBlock);
        } catch (Exception e) {
            result = this.createEmptyResult();
        }

        return result;
    }
    public CaptchaResult canSkipCaptchaNew(IWebsite website, CaptchaType captchaType, String referer) {
        IUrlBuilder urlBuilder = website.getUrlBuilder();

        String checkUrl = urlBuilder.getPasscodeCookieCheckUrlNew();

        CaptchaResult result;
        try {
            Header[] extraHeaders = new Header[] { new BasicHeader("Referer", referer) };
            String captchaBlock = this.mHttpStringReader.fromUri(checkUrl);
            result = this.checkHtmlBlockNew(captchaBlock);
        } catch (Exception e) {
            result = this.createEmptyResult();
        }

        return result;
    }

    public CaptchaResult checkHtmlBlock(String captchaBlock) {
        if (captchaBlock == null) {
            return this.createEmptyResult();
        }

        CaptchaResult result = new CaptchaResult();
        if (captchaBlock.startsWith("OK")) {
            result.canSkip = true;
        } else if (captchaBlock.startsWith("VIP") && !captchaBlock.startsWith("VIPFAIL")) {
            result.canSkip = true;
            result.successPassCode = true;
        } else if (captchaBlock.startsWith("CHECK")) {
            result.canSkip = false;
            result.captchaKey = captchaBlock.substring(captchaBlock.indexOf('\n') + 1);
        } else if (captchaBlock.startsWith("VIPFAIL")) {
            result.canSkip = true;
            result.failPassCode = true;
        }

        return result;
    }
    public CaptchaResult checkHtmlBlockNew(String captchaBlock) {
        if (captchaBlock == null) {
            return this.createEmptyResult();
        }
        ObjectMapper mapper = new ObjectMapper();
        CaptchaResult result = new CaptchaResult();
        try {
            CaptchaResultNew fromJson = mapper.readValue(captchaBlock,CaptchaResultNew.class);
               result.captchaKey = fromJson.getId();
               result.canSkip = false;
               result.failPassCode = false;
               result.successPassCode = false;
        }     catch (Exception e) {
            System.out.println(e.getStackTrace());
            result = this.createEmptyResult();    }
        return result;
    }

    public String stripJson(String str) {
        return str.substring(0,str.length()-1);
    }
    private CaptchaResult createEmptyResult() {
        return new CaptchaResult();
    }

    public class CaptchaResult {
        public boolean canSkip;
        public boolean successPassCode;
        public boolean failPassCode;
        public String captchaKey;
    }



}
