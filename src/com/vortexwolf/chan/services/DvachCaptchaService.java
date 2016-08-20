package com.vortexwolf.chan.services;

import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.interfaces.IUrlBuilder;
import com.vortexwolf.chan.interfaces.IWebsite;
import com.vortexwolf.chan.models.domain.CaptchaEntity;
import com.vortexwolf.chan.models.domain.CaptchaType;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public class DvachCaptchaService {
    public static final String IMAGE_URI = "/api/captcha/2chaptcha/image/";

    public CaptchaEntity loadCaptcha(String key, IWebsite website) {
        IUrlBuilder urlBuilder = website.getUrlBuilder();
        String imageUrl = urlBuilder.makeAbsolute(IMAGE_URI + key);

        CaptchaEntity captcha = new CaptchaEntity();
        captcha.setCaptchaType(CaptchaType.DVACH);
        captcha.setKey(key);
        captcha.setUrl(imageUrl);
        return captcha;
    }
}
