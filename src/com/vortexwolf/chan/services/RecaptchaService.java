package com.vortexwolf.chan.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.Uri;

import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.utils.RegexUtils;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.interfaces.IHttpStringReader;
import com.vortexwolf.chan.models.domain.CaptchaEntity;
import com.vortexwolf.chan.services.http.HttpStringReader;

public class RecaptchaService {
    private static final Uri API_URI = Uri.parse("http://www.google.com/recaptcha/api/noscript?k=6LeT6gcAAAAAAAZ_yDmTMqPH57dJQZdQcu6VFqog");
    private static final Pattern imgReg = Pattern.compile("<img .*?src=\"(.+?)\"");
    private static final Pattern chalReg = Pattern.compile("id=\"recaptcha_challenge_field\" value=\"(.*?)\"");

    public static boolean isRecaptchaPage(String html) {
        return html.contains(API_URI.getPath() + "?" + API_URI.getQuery());
    }
    
    public static CaptchaEntity loadCaptcha() {
        try {
            String html = Factory.resolve(HttpStringReader.class).fromUri(API_URI.toString());
            CaptchaEntity captcha = getCaptcha(html);
            return captcha;
        } catch (HttpRequestException e) {
            return null;
        }
    }

    private static CaptchaEntity getCaptcha(String html) {
        String challenge = RegexUtils.getGroupValue(html, chalReg, 1);
        String imageUrl = RegexUtils.getGroupValue(html, imgReg, 1);
        if (challenge == null || imageUrl == null) {
            return null;
        }
        
        imageUrl = "http://google.com/recaptcha/api/" + imageUrl;

        CaptchaEntity captcha = new CaptchaEntity();
        captcha.setKey(challenge);
        captcha.setUrl(imageUrl);
        return captcha;
    }
}
