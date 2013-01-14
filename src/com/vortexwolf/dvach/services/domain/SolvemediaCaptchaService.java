package com.vortexwolf.dvach.services.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vortexwolf.dvach.interfaces.IHttpStringReader;
import com.vortexwolf.dvach.models.domain.CaptchaEntity;

public class SolvemediaCaptchaService {
    
    public static final String DEFAULT_KEY = "oIzJ06xKCH-H6PKr8OLVMa26G06kK3qh";
    public static final String CHALLENGE_URI = "http://api.solvemedia.com/papi/_challenge.js?k=";
    public static final String IMAGE_URI = "http://api.solvemedia.com/papi/media?c=";
    
    private static final Pattern sChallengeIdPattern = Pattern.compile("\"chid\"\\s*:\\s*\"(.+)\",");

    public static CaptchaEntity loadCaptcha(IHttpStringReader httpStringReader, String key) {
        key = key != null ? key : DEFAULT_KEY;
        String uri = CHALLENGE_URI + key;
        String html = httpStringReader.fromUri(uri);

        CaptchaEntity captcha = getCaptcha(html);

        return captcha;
    }

    private static CaptchaEntity getCaptcha(String html) {
        try {
            // test for regex match
            Matcher chidMatch = sChallengeIdPattern.matcher(html);
            boolean chidExists = chidMatch.find();

            if (chidExists) {
                String key = chidMatch.group(1);
                String imageUrl = IMAGE_URI + key;

                // return a Captcha struct
                CaptchaEntity captcha = new CaptchaEntity();
                captcha.setKey(key);
                captcha.setUrl(imageUrl);
                return captcha;
            } else {
                // something didn't work.
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
