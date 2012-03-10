package com.vortexwolf.dvach.interfaces;

public interface IHtmlCaptchaChecker {

	/** Возвращает ответ нужно ли вводить капчу */
	public abstract boolean canSkipCaptcha(String boardName, String threadNumber);

}