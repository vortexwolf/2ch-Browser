package com.vortexwolf.chan.services;

import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.utils.RegexUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.UriUtils;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.models.domain.CaptchaEntity;
import com.vortexwolf.chan.models.domain.CaptchaType;
import com.vortexwolf.chan.services.http.HttpStringReader;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.regex.Pattern;

public class MailruCaptchaService {
    public static final String DATA_URI = "https://api-nocaptcha.mail.ru/captcha?public_key=";
    public static final String IMAGE_URI = "https://api-nocaptcha.mail.ru/c/1";

    private static final Pattern dataPattern = Pattern.compile("var data = \\{(.*?)\\}", Pattern.DOTALL);
    private static final Pattern dataIdPattern = Pattern.compile("id: \"(.*?)\"");
    private static final Pattern dataIsVerifiedPattern = Pattern.compile("is_verified: \"(.*?)\"");
    private static final Pattern dataStatusPattern = Pattern.compile("status: \"(.*?)\"");
    private static final Pattern dataDescriptionPattern = Pattern.compile("desc: \"(.*?)\"");


    private final HttpStringReader mHttpStringReader;

    public MailruCaptchaService(HttpStringReader httpStringReader) {
        this.mHttpStringReader = httpStringReader;
    }

    public CaptchaEntity loadCaptcha(String key, String referer) {
        try {
            Header[] extraHeaders = new Header[] { new BasicHeader("Referer", referer) };
            String response = this.mHttpStringReader.fromUri(DATA_URI + key, extraHeaders);
            CaptchaEntity captcha = getCaptchaFromJavascript(response);
            return captcha;
        } catch (HttpRequestException e) {
            return null;
        }
    }

    private CaptchaEntity getCaptchaFromJavascript(String js) {
        String dataObjectStr = RegexUtils.getGroupValue(js, dataPattern, 1);
        if (dataObjectStr == null) {
            return null;
        }

        String id = RegexUtils.getGroupValue(dataObjectStr, dataIdPattern, 1);
        boolean isVerified = StringUtils.areEqual("1", RegexUtils.getGroupValue(dataObjectStr, dataIsVerifiedPattern, 1));
        String status = RegexUtils.getGroupValue(dataObjectStr, dataStatusPattern, 1);
        String description = RegexUtils.getGroupValue(dataObjectStr, dataDescriptionPattern, 1);

        CaptchaEntity captcha = new CaptchaEntity();
        captcha.setCaptchaType(CaptchaType.MAILRU);
        captcha.setKey(id);
        captcha.setUrl(IMAGE_URI);
        captcha.setVerified(isVerified);
        captcha.setIsError(!StringUtils.areEqual(status, "ok"));
        if (captcha.isError()) {
            captcha.setErrorMessage(description);
        }
        return captcha;
    }
}
