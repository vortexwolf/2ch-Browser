package com.vortexwolf.dvach.services.domain;

import com.vortexwolf.dvach.models.domain.CaptchaEntity;

public class YandexCaptchaService {
    public static final String IMAGE_URI = "http://i.captcha.yandex.net/image?key=";

    public static CaptchaEntity loadCaptcha(String key) {
        CaptchaEntity captcha = new CaptchaEntity();
        captcha.setKey(key);
        captcha.setUrl(IMAGE_URI + key);
        return captcha;
    }
}
