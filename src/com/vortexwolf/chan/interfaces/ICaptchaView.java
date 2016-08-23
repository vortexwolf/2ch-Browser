package com.vortexwolf.chan.interfaces;

import android.graphics.Bitmap;

import com.vortexwolf.chan.models.domain.CaptchaEntity;

public interface ICaptchaView {

    void showCaptchaLoading();

    void skipCaptcha(boolean successPasscode, boolean failPasscode);

    void showCaptcha(CaptchaEntity captcha, Bitmap captchaImage);

    void showCaptchaError(String errorMessage);

    void appCaptcha(CaptchaEntity captcha);
}
