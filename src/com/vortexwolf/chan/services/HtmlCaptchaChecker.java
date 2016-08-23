package com.vortexwolf.chan.services;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import android.net.Uri;

import com.vortexwolf.chan.common.Websites;
import com.vortexwolf.chan.common.utils.UriUtils;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.interfaces.IHttpStringReader;
import com.vortexwolf.chan.interfaces.IUrlBuilder;
import com.vortexwolf.chan.interfaces.IWebsite;
import com.vortexwolf.chan.models.domain.CaptchaType;
import com.vortexwolf.chan.settings.ApplicationSettings;
import com.wildflyforcer.utils.CaptchaResultNew;
import com.wildflyforcer.utils.Constants;

public class HtmlCaptchaChecker {
    private final IHttpStringReader mHttpStringReader;
    private final ApplicationSettings mApplicationSettings;

    public HtmlCaptchaChecker(IHttpStringReader httpStringReader, ApplicationSettings settings) {
        this.mHttpStringReader = httpStringReader;
        this.mApplicationSettings = settings;
    }

    public CaptchaResult canSkipCaptcha(IWebsite website, CaptchaType captchaType, String referer) {
        IUrlBuilder urlBuilder = website.getUrlBuilder();
        if (captchaType == CaptchaType.APP) {
            String appcaptchaUrl = urlBuilder.getAppCaptchaCheckUrl(Constants.PUBLIC_API_KEY);
            CaptchaResult resultapp;
        try {
            String appcaptchaBlock = this.mHttpStringReader.fromUri(appcaptchaUrl);
            resultapp = this.checkHtmlBlock(appcaptchaBlock);
            resultapp.appCaptcha = true;

        } catch (HttpRequestException e) {
            resultapp = this.createEmptyResult();
        }
            return resultapp;
        } else {
            String checkUrl = urlBuilder.getPasscodeCookieCheckUrl();

        CaptchaResult result;
        try {
            Header[] extraHeaders = new Header[] { new BasicHeader("Referer", referer) };
            String captchaBlock = this.mHttpStringReader.fromUri(checkUrl);
            result = this.checkHtmlBlock(captchaBlock);
        } catch (Exception e) {
            result = this.createEmptyResult();
        }

            return result;
        }
    }

    public CaptchaResult checkHtmlBlock(String captchaBlock) {
        if (captchaBlock == null) {
            return this.createEmptyResult();
        }
        ObjectMapper mapper = new ObjectMapper();
        CaptchaResult result = new CaptchaResult();
        try {
            CaptchaResultNew fromJson = mapper.readValue(captchaBlock,CaptchaResultNew.class);
               switch (fromJson.getResult()){
                  case "3":{
                      result.canSkip = true;
                 }
                   case "2":{
                       result.canSkip = true;
                       result.successPassCode = true;
                   }
                   case "1":{
                       result.canSkip = false;
                       result.captchaKey = fromJson.getId();
                       if (fromJson.getType() == "app") {
                           result.appCaptcha = true;
                       } else {
                           result.appCaptcha = false;
                       }
                   }

               }


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
        public boolean appCaptcha;
    }



}
