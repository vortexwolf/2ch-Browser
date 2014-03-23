package com.vortexwolf.chan.services.domain;

import com.vortexwolf.chan.models.domain.CaptchaEntity;

public class YandexCaptchaService {
    public static final String IMAGE_URI = "http://captcha.yandex.net/image?key=";

    public static CaptchaEntity loadCaptcha(String key) {
        CaptchaEntity captcha = new CaptchaEntity();
        captcha.setKey(key);
        captcha.setUrl(IMAGE_URI + key);
        return captcha;
    }
}
