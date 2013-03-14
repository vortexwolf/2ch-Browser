package com.vortexwolf.dvach.interfaces;

import android.net.Uri;

import com.vortexwolf.dvach.services.domain.HtmlCaptchaChecker.CaptchaResult;

public interface IHtmlCaptchaChecker {

    /** Возвращает ответ нужно ли вводить капчу */
    public abstract CaptchaResult canSkipCaptcha(Uri refererUri);

}