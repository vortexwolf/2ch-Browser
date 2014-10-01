package com.vortexwolf.chan.interfaces;

import com.vortexwolf.chan.models.domain.CaptchaEntity;

import android.content.Context;

public interface IListView<T> {

    /**
     * Возвращает контекст приложения
     */
    Context getApplicationContext();

    /**
     * Отображает прогресс при загрузке
     * 
     * @param value
     *            Величина от 0 до 10000
     */
    void setWindowProgress(int value);

    /**
     * Вызывается в случае успешной загрузки данных
     * 
     * @param list
     *            Загруженные данные
     */
    void setData(T list);

    /**
     * Вызывается в случае ошибки загрузки данных
     * 
     * @param error
     *            Текст ошибки
     */
    void showError(String error);
    
    void showCaptcha(CaptchaEntity captcha);

    /**
     * Показывает индикатор загрузки
     */
    void showLoadingScreen();

    /**
     * Прячет индикатор загрузки
     */
    void hideLoadingScreen();
}
