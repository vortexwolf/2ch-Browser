package com.vortexwolf.chan.interfaces;

import android.graphics.Bitmap;

import com.vortexwolf.chan.models.domain.CaptchaEntity;
import com.vortexwolf.chan.services.Recaptcha2;

public interface ICaptchaView {

    void showCaptchaLoading();

    void skipCaptcha(boolean successPasscode, boolean failPasscode);

    void showCaptcha(CaptchaEntity captcha, Bitmap captchaImage);
    
    void showCaptcha(Recaptcha2 mRecaptcha2);

    void showCaptchaError(String errorMessage);
}
