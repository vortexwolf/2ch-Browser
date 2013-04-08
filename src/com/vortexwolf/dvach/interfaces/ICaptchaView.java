package com.vortexwolf.dvach.interfaces;

import android.graphics.Bitmap;

import com.vortexwolf.dvach.models.domain.CaptchaEntity;

public interface ICaptchaView {

    void showCaptchaLoading();

    void skipCaptcha(boolean withPasscode);

    void showCaptcha(CaptchaEntity captcha, Bitmap captchaImage);

    void showCaptchaError(String errorMessage);
}
