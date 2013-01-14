package com.vortexwolf.dvach.interfaces;

import com.vortexwolf.dvach.services.domain.HtmlCaptchaChecker.CaptchaResult;

import android.net.Uri;

public interface IHtmlCaptchaChecker {

    /** Возвращает ответ нужно ли вводить капчу */
    public abstract CaptchaResult canSkipCaptcha(Uri refererUri);

}