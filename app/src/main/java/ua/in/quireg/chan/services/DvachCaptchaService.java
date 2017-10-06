package ua.in.quireg.chan.services;

import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.CaptchaEntity;
import ua.in.quireg.chan.models.domain.CaptchaType;

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
