package com.vortexwolf.chan.interfaces;

import com.vortexwolf.chan.models.domain.BoardModel;

public interface IBoardsListView extends IListView<BoardModel[]> {

    /**
     * Вызывается в случае ошибки при частичном обновлении
     *
     * @param error
     *            Текст ошибки
     */
    void showUpdateError(String error);

    /**
     * Показывает индикатор загрузки при частичном обновлении
     */
    void showUpdateLoading();

    /**
     * Прячет индикатор загрузки при частичном обновлении
     */
    void hideUpdateLoading();
}
