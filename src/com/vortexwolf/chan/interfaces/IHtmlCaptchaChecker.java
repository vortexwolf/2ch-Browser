package com.vortexwolf.chan.interfaces;

import android.net.Uri;

import com.vortexwolf.chan.services.HtmlCaptchaChecker.CaptchaResult;

public interface IHtmlCaptchaChecker {

    /** Возвращает ответ нужно ли вводить капчу */
    public abstract CaptchaResult canSkipCaptcha(Uri refererUri, boolean usePasscode);

}