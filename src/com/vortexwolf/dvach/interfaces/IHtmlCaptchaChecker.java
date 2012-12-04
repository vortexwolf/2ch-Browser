package com.vortexwolf.dvach.interfaces;

import android.net.Uri;

public interface IHtmlCaptchaChecker {

	/** Возвращает ответ нужно ли вводить капчу */
	public abstract boolean canSkipCaptcha(Uri refererUri);

}